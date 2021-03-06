/*
 * $Id: ImportWarcs.java 1521 2018-03-00
 *
 * Copyright (C) 2003 Internet Archive.
 *
 * This file is part of the archive-access tools project
 * (http://sourceforge.net/projects/archive-access).
 *
 * The archive-access tools are free software; you can redistribute them and/or
 * modify them under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or any
 * later version.
 *
 * The archive-access tools are distributed in the hope that they will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License along with
 * the archive-access tools; if not, write to the Free Software Foundation,
 * Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 */

package org.archive.access.nutch.jobs;

import org.apache.commons.httpclient.ChunkedInputStream;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.StatusLine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.util.Progressable;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.ToolBase;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.MapWritable;
import org.apache.nutch.fetcher.Fetcher;
import org.apache.nutch.fetcher.FetcherOutput;
import org.apache.nutch.fetcher.FetcherOutputFormat;
import org.apache.nutch.global.Global;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.parse.*;
import org.apache.nutch.protocol.Content;
import org.apache.nutch.scoring.ScoringFilterException;
import org.apache.nutch.scoring.ScoringFilters;
import org.apache.nutch.util.StringUtil;
import org.apache.nutch.util.mime.MimeType;
import org.apache.nutch.util.mime.MimeTypeException;
import org.apache.nutch.util.mime.MimeTypes;
import org.archive.access.nutch.Nutchwax;
import org.archive.access.nutch.NutchwaxConfiguration;
import org.archive.access.nutch.jobs.sql.SqlSearcher;
import org.archive.io.ArchiveRecordHeader;
import org.archive.format.warc.WARCConstants;
import org.archive.io.warc.WARCRecord;
import org.archive.mapred.WARCMapRunner;
import org.archive.mapred.WARCRecordMapper;
import org.archive.mapred.WARCReporter;
import org.archive.util.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.StringTokenizer;


/**
 * Ingests WARCs writing WARC Record parse as Nutch FetcherOutputFormat.
 * FOF has five outputs:
 * <ul><li>crawl_fetch holds a fat CrawlDatum of all vitals including metadata.
 * Its written below by our {@link WaxFetcherOutputFormat} (innutch by
 * {@link FetcherOutputFormat}).  Here is an example CD: <pre>  Version: 4
 *  Status: 5 (fetch_success)
 *  Fetch time: Wed Mar 15 12:38:49 PST 2006
 *  Modified time: Wed Dec 31 16:00:00 PST 1969
 *  Retries since fetch: 0
 *  Retry interval: 0.0 days
 *  Score: 1.0
 *  Signature: null
 *  Metadata: collection:test arcname:IAH-20060315203614-00000-debord arcoffset:5127
 * </pre></li>
 * <li>crawl_parse has CrawlDatum of MD5s.  Used making CrawlDB.
 * Its obtained from above fat crawl_fetch CrawlDatum and written
 * out as part of the parse output done by {@link WaxParseOutputFormat}.
 * This latter class writes three files.  This crawl_parse and both
 * of the following parse_text and parse_data.</li>
 * <li>parse_text has text from parse.</li>
 * <li>parse_data has other metadata found by parse (Depends on
 * parser).  This is only input to linkdb.  The html parser
 * adds found out links here and content-type and discovered
 * encoding as well as advertised encoding, etc.</li>
 * <li>cdx has a summary line for every record processed.</li>
 * </ul>
 */
public class ImportWarcs extends ToolBase implements WARCRecordMapper
{
    public  final Log LOG = LogFactory.getLog(ImportWarcs.class);
    private final NumberFormat numberFormatter = NumberFormat.getInstance();

    private static final String WHITESPACE = "\\s+";

    public static final String ARCFILENAME_KEY = "arcname";
    public static final String ARCFILEOFFSET_KEY = "arcoffset";
    private static final String CONTENT_TYPE_KEY = "content-type";
    private static final String TRANSFER_ENCODING_KEY = "Transfer-Encoding";
    private static final String TEXT_TYPE = "text/";
    private static final String APPLICATION_TYPE = "application/";
    public static final String ARCCOLLECTION_KEY = "collection";
    public static final String WAX_SUFFIX = "wax.";
    public static final String WAX_COLLECTION_KEY = WAX_SUFFIX + ARCCOLLECTION_KEY;

    private static final String PDF_TYPE = "application/pdf";

    private boolean indexAll;
    private int contentLimit;
    private int pdfContentLimit;
    private MimeTypes mimeTypes;
    private String segmentName;
    private String collectionName;
    private int parseThreshold = -1;
    private boolean indexRedirects;
    private boolean sha1 = false;
    private boolean arcNameFromFirstRecord = true ;
    private String arcName;
    private String collectionType;
    private int timeoutIndexingDocument;

    /**
     * Buffer to reuse on each ARCRecord indexing.
     */
    private final byte[] buffer = new byte[1024 * 16];

    private final ByteArrayOutputStream contentBuffer =
            new ByteArrayOutputStream(1024 * 16);

    private URLNormalizers urlNormalizers;
    private URLFilters filters;

    private ParseUtil parseUtil;

    private static final Text CDXKEY = new Text("cdx");

    private TimeoutParsingThreadPool threadPool=new TimeoutParsingThreadPool(); // this is one pool of only one thread; it is not necessary to be static



    public ImportWarcs()
    {
        super();
    }

    public ImportWarcs(Configuration conf)
    {
        setConf(conf);
    }

    public void importWarcs(final Path arcUrlsDir, final Path segment,
                            final String collection)
            throws IOException
    {
        LOG.info("ImportWarcsSegment: " + segment + ", src: " + arcUrlsDir);
        System.out.println( "ImportWarcs segment: " + segment + ", src: " + arcUrlsDir );

        final JobConf job = new JobConf(getConf(), this.getClass());

        job.set(Nutch.SEGMENT_NAME_KEY, segment.getName());

        job.setInputPath(arcUrlsDir);

        job.setMapRunnerClass( WARCMapRunner.class ); // compatible with hadoop 0.14 TODO MC
        job.setMapperClass( this.getClass() );

        job.setInputFormat(TextInputFormat.class);

        job.setOutputPath(segment);
        job.setOutputFormat(WaxFetcherOutputFormat.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(FetcherOutput.class);

        // Pass the collection name out to the tasks IF non-null.
        if ((collection != null) && (collection.length() > 0))
        {
            job.set(ImportWarcs.WAX_SUFFIX + ImportWarcs.ARCCOLLECTION_KEY,
                    collection);
        }
        job.setJobName("import " + arcUrlsDir + " " + segment);

        JobClient.runJob(job);
        LOG.info("ImportWarcs: done");
    }

    public void configure(final JobConf job)
    {
        setConf(job);
        this.indexAll = job.getBoolean("wax.index.all", false);

        this.contentLimit = job.getInt("http.content.limit", 1024 * 100);
        final int pdfMultiplicand = job.getInt("wax.pdf.size.multiplicand", 10);
        this.pdfContentLimit = (this.contentLimit == -1) ? this.contentLimit
                : pdfMultiplicand * this.contentLimit;
        this.mimeTypes = MimeTypes.get(job.get("mime.types.file"));
        this.segmentName = job.get(Nutch.SEGMENT_NAME_KEY);

        // Get the rsync protocol handler into the mix.
        System.setProperty("java.protocol.handler.pkgs", "org.archive.net");

        // Format numbers output by parse rate logging.
        this.numberFormatter.setMaximumFractionDigits(2);
        this.numberFormatter.setMinimumFractionDigits(2);
        this.parseThreshold = job.getInt("wax.parse.rate.threshold", -1);

        this.indexRedirects = job.getBoolean("wax.index.redirects", false);

        this.sha1 = job.getBoolean("wax.digest.sha1", false);

        this.urlNormalizers = new URLNormalizers(job, URLNormalizers.SCOPE_FETCHER);
        this.filters = new URLFilters(job);

        this.parseUtil = new ParseUtil(job);

        this.collectionName = job.get(ImportWarcs.WAX_SUFFIX + ImportWarcs.ARCCOLLECTION_KEY);

        // Get ARCName by reading first record in ARC?  Otherwise, we parse
        // the name of the file we've been passed to find an ARC name.
        this.arcNameFromFirstRecord = job.getBoolean("wax.arcname.from.first.record", true);

        this.collectionType = job.get(Global.COLLECTION_TYPE);
        this.timeoutIndexingDocument = job.getInt(Global.TIMEOUT_INDEXING_DOCUMENT, -1);

        LOG.info("ImportWarcs collectionType: " + collectionType);
    }

    public Configuration getConf()
    {
        return this.conf;
    }

    public void setConf(Configuration c)
    {
        this.conf = c;
    }

    public void onWARCOpen()
    {
        // Nothing to do.
    }

    public void onWARCClose()
    {
        threadPool.closeAll(); // close the only thread created for this map
    }

    public void map(final WritableComparable key, final Writable value,
                    final OutputCollector output, final Reporter r)
            throws IOException
    {

        LOG.info( "MAP WARC" );
        // Assumption is that this map is being run by WARCMapRunner.
        // Otherwise, the below casts fail.
        String url = key.toString();

        WARCRecord rec = (WARCRecord)((ObjectWritable)value).get();
        WARCReporter reporter = (WARCReporter)r;

        // Its null the first time map is called on an ARC.
        checkWArcName(rec);
        checkCollectionName();

        final ArchiveRecordHeader warcData = rec.getHeader();
        String oldUrl = url;

        try
        {
            url = urlNormalizers.normalize(url, URLNormalizers.SCOPE_FETCHER);
            url = filters.filter(url); // filter the url
        }
        catch (Exception e)
        {
            LOG.warn("Skipping record. Didn't pass normalization/filter " +
                    oldUrl + ": " + e.toString());

            return;
        }

        final long b = warcData.getContentBegin();
        final long l = warcData.getLength();
        final long recordLength = (l > b)? (l - b): l;

        String warcRecordMimetype = warcData.getMimetype();
        LOG.info("WARC Record Payload MIME TYPE: " + warcRecordMimetype);

        String warcRecordType = (String) warcData.getHeaderValue(WARCConstants.HEADER_KEY_TYPE);
        LOG.info("WARC Record Type: " + warcRecordType);

        if (!isValidRecordToIndex(warcRecordType, warcRecordMimetype)){
            LOG.info("Skipping WARC: "+ warcData.getHeaderValue(WARCConstants.HEADER_KEY_TYPE) + " MimeType " + warcRecordMimetype );
            return;
        }

        // Copy http headers to nutch metadata.
        final Metadata metaData = new Metadata();
        String mimetype = null;

        // if WARC-TYPE is resource skip HTTP header parsing
        if (warcRecordType.equalsIgnoreCase(WARCConstants.WARCRecordType.resource.toString())){
            LOG.info("Parsing WARC-TYPE: resource...");
            mimetype = warcRecordMimetype;

            if (skip(mimetype)){
                LOG.info("Skipping Mimetype: " + mimetype);
                return;
            }

            metaData.set(WARCConstants.CONTENT_LENGTH, String.valueOf(warcData.getContentLength()));
            metaData.set(WARCConstants.CONTENT_TYPE , mimetype);

        }
        else {
            LOG.info("Parsing WARC-TYPE: response...");
            // handle as if it was a ARCRecordMetaData here
            // parse HTTP Header to get metadata
            String statusLinestr = LaxHttpParser.readLine(rec, WARCRecord.WARC_HEADER_ENCODING);

            StatusLine statusLine;
            int statusCode = -1;

            try{
                statusLine = new StatusLine(statusLinestr);
                statusCode= statusLine.getStatusCode();
            } catch (HttpException e ){
                LOG.error("HttpException parsing statusCode isIndex " , e);
            } catch (Exception e){
                LOG.error("Exception parsing statusCode isIndex " , e);
            }

            if (!isIndex(statusCode)){
                return;
            }


            // TODO read the first line before feeding to LaxHttpParser
            Header[] headers = LaxHttpParser.parseHeaders(rec, WARCRecord.WARC_HEADER_ENCODING);

            for (int j = 0; j < headers.length; j++)
            {
                LOG.info("HTTP Header: " + headers[j].getName());
                final Header header = headers[j];

                // Special handling. If mimetype is still null, try getting it
                // from the http header. I've seen arc record lines with empty
                // content-type and a MIME unparseable file ending; i.e. .MID.
                if (header.getName().toLowerCase().equals(ImportWarcs.CONTENT_TYPE_KEY))
                {
                    LOG.info("Validating Content-type header value: " + header.getValue());
                    mimetype = getMimetype(header.getValue(), null, null);

                    if (skip(mimetype)){
                        LOG.info("Skipping Mimetype: " + mimetype);
                        return;
                    }
                }

                metaData.set(header.getName(), header.getValue());
            }
        }


        // This call to reporter setStatus pings the tasktracker telling it our
        // status and telling the task tracker we're still alive (so it doesn't
        // time us out).
        final String noSpacesMimetype = TextUtils.replaceAll(ImportWarcs.WHITESPACE, ((mimetype == null || mimetype.length() <= 0)? "TODO": mimetype), "-");
        final String recordLengthAsStr = Long.toString(recordLength);

        reporter.setStatus(getStatus(url, oldUrl, recordLengthAsStr, noSpacesMimetype));

        // This is a nutch 'more' field.
        metaData.set("contentLength", recordLengthAsStr);
        reporter.setStatusIfElapse("read headers on " + url);

        // verify transfer-encoding for chuncked content
        InputStream payloadInputStream;
        try {
            payloadInputStream = wrapchunkedContent(rec, metaData);
        }
        catch (IOException e){
            // something wrong skip record
            LOG.error("Something wrong trying to wrap around chunked content. skipping record.");
            return;
        }

        // TODO: Skip if unindexable type.
        int total = 0;

        // Read in first block. If mimetype still null, look for MAGIC.
        int len = payloadInputStream.read(this.buffer, 0, this.buffer.length);

        if (mimetype == null)
        {
            MimeType mt = this.mimeTypes.getMimeType(this.buffer);

            if (mt == null || mt.getName() == null)
            {
                LOG.warn("Failed to get mimetype for: " + url);

                return;
            }

            mimetype = mt.getName();
        }

        metaData.set(ImportWarcs.CONTENT_TYPE_KEY, mimetype);

        // How much do we read total? If pdf, we will read more. If equal to -1,
        // read all.
        int readLimit = (ImportWarcs.PDF_TYPE.equals(mimetype))?
                this.pdfContentLimit : this.contentLimit;

        // Reset our contentBuffer so can reuse.  Over the life of an ARC
        // processing will grow to maximum record size.
        this.contentBuffer.reset();

        while ((len != -1) && ((readLimit == -1) || (total < readLimit)))
        {
            total += len;
            this.contentBuffer.write(this.buffer, 0, len);
            len = payloadInputStream.read(this.buffer, 0, this.buffer.length);
            reporter.setStatusIfElapse("reading " + url);
        }

        // Close the Record.  We're done with it.  Side-effect is calculation
        // of digest -- if we're digesting.
        payloadInputStream.close();
        reporter.setStatusIfElapse("closed " + url);

        final byte[] contentBytes = this.contentBuffer.toByteArray();
        final CrawlDatum datum = new CrawlDatum();
        datum.setStatus(CrawlDatum.STATUS_FETCH_SUCCESS);

        // Calculate digest or use precalculated sha1.
        String digest = (this.sha1)? rec.getDigestStr():
                MD5Hash.digest(contentBytes).toString();
        metaData.set(Nutch.SIGNATURE_KEY, digest);

        LOG.info("Digest: " + digest);
        metaData.set(Nutch.SIGNATURE_KEY, digest);

        metaData.set(Nutch.SEGMENT_NAME_KEY, this.segmentName);

        // Score at this stage is 1.0f.
        metaData.set(Nutch.SCORE_KEY, Float.toString(datum.getScore()));

        final long startTime = System.currentTimeMillis();
        final Content content = new Content(url, url, contentBytes, mimetype, metaData, getConf());

        datum.setFetchTime(Nutchwax.getDate(getTs(warcData.getDate())));

        MapWritable mw = datum.getMetaData();

        if (mw == null)
        {
            mw = new MapWritable();
        }

        if (collectionType.equals(Global.COLLECTION_TYPE_MULTIPLE)) {
            mw.put(new Text(ImportWarcs.ARCCOLLECTION_KEY), new Text(SqlSearcher.getCollectionNameWithTimestamp(collectionName,getTs(warcData.getDate()))));
        }
        else {
            mw.put(new Text(ImportWarcs.ARCCOLLECTION_KEY), new Text(collectionName));
        }
        mw.put(new Text(ImportWarcs.ARCFILENAME_KEY), new Text(arcName));
        mw.put(new Text(ImportWarcs.ARCFILEOFFSET_KEY), new Text(Long.toString(warcData.getOffset())));
        datum.setMetaData(mw);

        TimeoutParsingThread tout=threadPool.getThread(Thread.currentThread().getId(),timeoutIndexingDocument);
        tout.setUrl(url);
        tout.setContent(content);
        tout.setParseUtil(parseUtil);
        tout.wakeupAndWait();

        ParseStatus parseStatus=tout.getParseStatus();
        Parse parse=tout.getParse();
        reporter.setStatusIfElapse("parsed " + url);

        LOG.info("Content parsed: " + parse.getText());

        if (!parseStatus.isSuccess()) {
            final String status = formatToOneLine(parseStatus.toString());
            LOG.warn("Error parsing: " + mimetype + " " + url + ": " + status);
            parse = null;
        }
        else {
            // Was it a slow parse?
            final double kbPerSecond = getParseRate(startTime,
                    (contentBytes != null) ? contentBytes.length : 0);

            if (LOG.isDebugEnabled())
            {
                LOG.debug(getParseRateLogMessage(url,
                        noSpacesMimetype, kbPerSecond));
            }
            else if (kbPerSecond < this.parseThreshold)
            {
                LOG.warn(getParseRateLogMessage(url, noSpacesMimetype,
                        kbPerSecond));
            }
        }

        Writable v = new FetcherOutput(datum, null,
                parse != null ? new ParseImpl(parse) : null);
        if (collectionType.equals(Global.COLLECTION_TYPE_MULTIPLE)) {
            LOG.info("multiple: "+SqlSearcher.getCollectionNameWithTimestamp(this.collectionName,getTs(warcData.getDate()))+" "+url);
            output.collect(Nutchwax.generateWaxKey(url,SqlSearcher.getCollectionNameWithTimestamp(this.collectionName,getTs(warcData.getDate()))), v);
        }
        else {
            output.collect(Nutchwax.generateWaxKey(url, this.collectionName), v);
        }
    }

    private boolean isValidRecordToIndex(String warcRecordType, String warcRecordMimetype){
        // Check if WARC-TYPE=response and if this record is an http response OR if it is a resource record
        // Replacing string in WARCConstants.HTTP_RESPONSE_MIMETYPE
        // because brozzler is writing WARCs mimetype this way: application/http;msgtype=response (no space)
        if ( WARCConstants.WARCRecordType.response.toString().equalsIgnoreCase(warcRecordType.trim()) &&
                (warcRecordMimetype.trim().equals(WARCConstants.HTTP_RESPONSE_MIMETYPE) ||
                warcRecordMimetype.trim().equals(WARCConstants.HTTP_RESPONSE_MIMETYPE.replaceAll("\\s", ""))) ||
                WARCConstants.WARCRecordType.resource.toString().equalsIgnoreCase(warcRecordType.trim())){
            return true;
        }
        return false;
    }

    private InputStream wrapchunkedContent(InputStream rec, Metadata metadata) throws IOException {
        // verify transfer-encoding for chuncked content
        String transferEncoding = metadata.get(ImportWarcs.TRANSFER_ENCODING_KEY);
        if (transferEncoding != null && transferEncoding.equalsIgnoreCase("chunked")){
            // wrap InputStream on a ChunkedInputStream
            LOG.info("Chunked content found. Wrapping up InputStream..");
            ChunkedInputStream payloadInputStream = new ChunkedInputStream(rec);
            return payloadInputStream;
        }
        else {
            return rec;
        }
    }

    public void setCollectionName(String collectionName)
    {
        this.collectionName = collectionName;
        checkCollectionName();
    }

    public String getArcName()
    {
        return this.arcName;
    }

    public String getTs(String dateWarc){
        /*dateWarc in Format 2018-04-03T12:53:43Z */
        String year = "";
        String month = "";
        String day = "";
        String hour = "";
        String minute = "";
        String second = "";

        LOG.info("WARC getTs - received: " + dateWarc);
        try{
            SimpleDateFormat thedate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", new Locale("pt", "PT"));
            thedate.parse(dateWarc);
            Calendar mydate = thedate.getCalendar();

            year +=  mydate.get(Calendar.YEAR);
            int monthInt = mydate.get(Calendar.MONTH) + 1;
            int dayInt = mydate.get(Calendar.DAY_OF_MONTH);
            int hourInt = mydate.get(Calendar.HOUR_OF_DAY);
            int minuteInt = mydate.get(Calendar.MINUTE);
            int secondInt = mydate.get(Calendar.SECOND);
            month = monthInt < 10 ? "0" + monthInt : ""+monthInt;
            day = dayInt < 10 ? "0" + dayInt : ""+dayInt;
            hour = hourInt < 10 ? "0" + hourInt : ""+hourInt;
            minute = minuteInt < 10 ? "0" + minuteInt : ""+minuteInt;
            second = secondInt < 10 ? "0" + secondInt : ""+secondInt;

            LOG.info("WARC getTs: " + year + month + day + hour + minute + second );
        } catch (Exception e ){
            LOG.info("WARC getTS: error parsing date");
            return null;
        }
        return year + month + day + hour + minute + second;
    }

    public void checkWArcName(WARCRecord rec)
    {
        this.arcName= rec.getHeader().getReaderIdentifier(); /*url with path to arc*/
        this.arcName = arcName.substring(arcName.lastIndexOf('/')+1, arcName.length()); /*filename*/
        this.arcName = arcName.replace(".warc.gz", "");   /*filename without extension*/
        LOG.info("WARCNAME: " + this.arcName);
    }

    protected boolean checkCollectionName()
    {
        if ((this.collectionName != null) && this.collectionName.length() > 0)
        {
            return true;
        }

        throw new NullPointerException("Collection name can't be empty");
    }

    /**
     * @param bytes Array of bytes to examine for an EOL.
     * @return Count of end-of-line characters or zero if none.
     */
    private int getEolCharsCount(byte [] bytes) {
        int count = 0;
        if (bytes != null && bytes.length >=1 &&
                bytes[bytes.length - 1] == '\n') {
            count++;
            if (bytes.length >=2 && bytes[bytes.length -2] == '\r') {
                count++;
            }
        }
        return count;
    }


    /**
     * @param statusCode ARC Record to test.
     * @return True if we are to index this record.
     */
    protected boolean isIndex( int statusCode)
    {
        return ((statusCode >= 200) && (statusCode < 300))
                || (this.indexRedirects && ((statusCode >= 300) &&
                (statusCode < 400)));
    }

    private byte[] getByteArrayFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[inputStream.available()];
        int read = 0;
        while ((read = inputStream.read(buffer, 0, buffer.length)) != -1) {
            baos.write(buffer, 0, read);
        }
        baos.flush();
        return  baos.toByteArray();
    }


    private static String getStringFromInputStream(InputStream is) {

        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();

        String line;
        try {

            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    private static byte[] getByteArrayFromInputStreamChunked(InputStream is, Log LOG) {
        ChunkedInputStream cis = null;
        int i = 0;
        int currentChar;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        byte[] unchunkedData = null;

        try {
            cis = new ChunkedInputStream(is);

            // read till the end of the stream
            while((currentChar = cis.read(buffer))!= -1) {
                bos.write(buffer, 0, currentChar);
            }
            unchunkedData = bos.toByteArray();
            bos.close();
        } catch(IOException e) {
            // if any I/O error occurs
            e.printStackTrace();
        } finally {
            // releases any system resources associated with the stream
            if(is!=null){
                try{
                    is.close();
                } catch (IOException e){
                    LOG.error("ERROR closing InputStream chunked record" , e);
                }
            }
            if(cis!=null){
                try{
                    cis.close();
                } catch (IOException e){
                    LOG.error("ERROR closing ChunkedInputStream chunked record", e);
                }
            }
        }
        return unchunkedData;
    }


    private static String getStringFromInputStreamChunked(InputStream is, Log LOG) {
        ChunkedInputStream cis = null;
        int i = 0;
        int currentChar;
        String result ="";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        byte[] unchunkedData = null;

        try {
            cis = new ChunkedInputStream(is);

            // read till the end of the stream
            while((currentChar = cis.read(buffer))!= -1) {
                bos.write(buffer, 0, currentChar);
            }
            unchunkedData = bos.toByteArray();
            bos.close();
            result = new String(unchunkedData, "UTF-8");
        } catch(IOException e) {
            // if any I/O error occurs
            e.printStackTrace();
        } finally {
            // releases any system resources associated with the stream
            if(is!=null){
                try{
                    is.close();
                } catch (IOException e){
                    LOG.error("ERROR closing InputStream chunked record" , e);
                }
            }
            if(cis!=null){
                try{
                    cis.close();
                } catch (IOException e){
                    LOG.error("ERROR closing ChunkedInputStream chunked record", e);
                }
            }
        }
        LOG.info("WARC Chunked Result: " + result);
        return result;
    }


    protected String getStatus(final String url, String oldUrl,
                               final String recordLengthAsStr, final String noSpacesMimetype)
    {
        // If oldUrl is same as url, don't log.  Otherwise, log original so we
        // can keep url originally imported.
        if (oldUrl.equals(url))
        {
            oldUrl = "-";
        }

        StringBuilder sb = new StringBuilder(128);
        sb.append("adding ");
        sb.append(url);
        sb.append(" ");
        sb.append(oldUrl);
        sb.append(" ");
        sb.append(recordLengthAsStr);
        sb.append(" ");
        sb.append(noSpacesMimetype);

        return sb.toString();
    }

    protected String formatToOneLine(final String s)
    {
        final StringBuffer sb = new StringBuffer(s.length());

        for (final StringTokenizer st = new StringTokenizer(s, "\t\n\r");
             st.hasMoreTokens(); sb.append(st.nextToken()))
        {
            ;
        }

        return sb.toString();
    }


    protected String getParseRateLogMessage(final String url,
                                            final String mimetype, final double kbPerSecond)
    {
        return url + " " + mimetype + " parse KB/Sec "
                + this.numberFormatter.format(kbPerSecond);
    }

    protected double getParseRate(final long startTime, final long len)
    {
        // Get indexing rate:
        long elapsedTime = System.currentTimeMillis() - startTime;
        elapsedTime = (elapsedTime == 0) ? 1 : elapsedTime;

        return (len != 0) ? ((double) len / 1024)
                / ((double) elapsedTime / 1000) : 0;
    }

    protected boolean skip(final String mimetype)
    {
        boolean decision = false;

        /*We Are only indexing text* and application* mimetypes  */
        /*We are also excluding CSS, javascript and XML */

        if ((mimetype == null)
                || (!mimetype.startsWith(ImportWarcs.TEXT_TYPE) && !mimetype.startsWith(ImportWarcs.APPLICATION_TYPE))
                || (mimetype.startsWith(ImportWarcs.TEXT_TYPE) && mimetype.toLowerCase().contains("css"))
                || (mimetype.toLowerCase().contains("xml"))
                || (mimetype.toLowerCase().contains("javascript")))
        {
            // Skip any but basic types.
            decision = true;
        }
        return decision;
    }

    protected String getMimetype(final String mimetype, final MimeTypes mts,
                                 final String url)
    {
        if (mimetype != null && mimetype.length() > 0)
        {
            return checkMimetype(mimetype.toLowerCase());
        }

        if (mts != null && url != null)
        {
            final MimeType mt = mts.getMimeType(url);

            if (mt != null)
            {
                return checkMimetype(mt.getName().toLowerCase());
            }
        }

        return null;
    }

    protected static String checkMimetype(String mimetype)
    {
        MimeType aux = null;
        if ((mimetype == null) || (mimetype.length() <= 0) ||
                mimetype.startsWith(MimetypeUtils.NO_TYPE_MIMETYPE))
        {
            return null;
        }

        // Test the mimetype makes sense. If not, clear it.
        try{
            aux = new MimeType(mimetype);
        } catch ( final MimeTypeException e ) {
            mimetype = null;
        }

        return mimetype;
    }

    /**
     * Override of nutch FetcherOutputFormat so I can substitute my own
     * ParseOutputFormat, {@link WaxParseOutputFormat}.  While I'm here,
     * removed content references.  NutchWAX doesn't save content.
     * @author stack
     */
    public static class WaxFetcherOutputFormat extends FetcherOutputFormat
    {
        public RecordWriter getRecordWriter(final FileSystem fs,
                                            final JobConf job, final String name, Progressable progress)
                throws IOException
        {
            Path f = new Path(job.getOutputPath(), CrawlDatum.FETCH_DIR_NAME);
            final Path fetch = new Path(f, name);
            final MapFile.Writer fetchOut = new MapFile.Writer(job, fs,
                    fetch.toString(), Text.class, CrawlDatum.class);

            // Write a cdx file.  Write w/o compression.
            Path cdx = new Path(new Path(job.getOutputPath(), "cdx"), name);
            final SequenceFile.Writer cdxOut = SequenceFile.createWriter(fs,
                    job, cdx, Text.class, Text.class,
                    SequenceFile.CompressionType.NONE);

            return new RecordWriter()
            {
                private RecordWriter parseOut;

                // Initialization
                {
                    if (Fetcher.isParsing(job))
                    {
                        // Here is nutchwax change, using WaxParseOutput
                        // instead of ParseOutputFormat.
                        this.parseOut = new WaxParseOutputFormat().
                                getRecordWriter(fs, job, name, null);
                    }
                }

                public void write(WritableComparable key, Writable value)
                        throws IOException
                {
                    FetcherOutput fo = (FetcherOutput)value;
                    MapWritable mw = fo.getCrawlDatum().getMetaData();
                    Text cdxLine = (Text)mw.get(ImportWarcs.CDXKEY);

                    if (cdxLine != null)
                    {
                        cdxOut.append(key, cdxLine);
                    }

                    mw.remove(ImportWarcs.CDXKEY);
                    fetchOut.append(key, fo.getCrawlDatum());

                    if (fo.getParse() != null)
                    {
                        parseOut.write(key, fo.getParse());
                    }
                }

                public void close(Reporter reporter) throws IOException
                {
                    fetchOut.close();
                    cdxOut.close();

                    if (parseOut != null)
                    {
                        parseOut.close(reporter);
                    }
                }
            };
        }
    }

    /**
     * Copy so I can add collection prefix to produced signature and link
     * CrawlDatums.
     * @author stack
     */
    public static class WaxParseOutputFormat extends ParseOutputFormat
    {
        public final Log LOG = LogFactory.getLog(WaxParseOutputFormat.class);

        private URLNormalizers urlNormalizers;
        private URLFilters filters;
        private ScoringFilters scfilters;

        public RecordWriter getRecordWriter(FileSystem fs, JobConf job,
                                            String name, Progressable progress)
                throws IOException
        {
            // Extract collection prefix from key to use later when adding
            // signature and link crawldatums.

            this.urlNormalizers =
                    new URLNormalizers(job, URLNormalizers.SCOPE_OUTLINK);
            this.filters = new URLFilters(job);
            this.scfilters = new ScoringFilters(job);

            final float interval =
                    job.getFloat("db.default.fetch.interval", 30f);
            final boolean ignoreExternalLinks =
                    job.getBoolean("db.ignore.external.links", false);
            final boolean sha1 = job.getBoolean("wax.digest.sha1", false);

            Path text = new Path(new Path(job.getOutputPath(),
                    ParseText.DIR_NAME), name);
            Path data = new Path(new Path(job.getOutputPath(),
                    ParseData.DIR_NAME), name);
            Path crawl = new Path(new Path(job.getOutputPath(),
                    CrawlDatum.PARSE_DIR_NAME), name);

            final MapFile.Writer textOut = new MapFile.Writer(job, fs,
                    text.toString(), Text.class, ParseText.class,
                    CompressionType.RECORD);

            final MapFile.Writer dataOut = new MapFile.Writer(job, fs,
                    data.toString(), Text.class, ParseData.class);

            final SequenceFile.Writer crawlOut = SequenceFile.createWriter(fs,
                    job, crawl, Text.class, CrawlDatum.class);

            return new RecordWriter()
            {
                public void write(WritableComparable key, Writable value)
                        throws IOException
                {
                    // Test that I can parse the key before I do anything
                    // else. If not, write nothing for this record.
                    String collection = null;
                    String fromUrl = null;
                    String fromHost = null;
                    String toHost = null;

                    try
                    {
                        collection = Nutchwax.getCollectionFromWaxKey(key);
                        fromUrl = Nutchwax.getUrlFromWaxKey(key);
                    }
                    catch (IOException ioe)
                    {
                        LOG.warn("Skipping record. Can't parse " + key, ioe);

                        return;
                    }

                    if (fromUrl == null || collection == null)
                    {
                        LOG.warn("Skipping record. Null from or collection " +
                                key);

                        return;
                    }

                    Parse parse = (Parse)value;

                    textOut.append(key, new ParseText(parse.getText()));
                    ParseData parseData = parse.getData();


                    // recover the signature prepared by Fetcher or ParseSegment
                    String sig = parseData.getContentMeta().get(
                            Nutch.SIGNATURE_KEY);

                    if (sig != null)
                    {
                        byte[] signature = (sha1)?
                                Base32.decode(sig): StringUtil.fromHexString(sig);

                        if (signature != null)
                        {
                            // append a CrawlDatum with a signature
                            CrawlDatum d = new CrawlDatum(
                                    CrawlDatum.STATUS_SIGNATURE, 0.0f);
                            d.setSignature(signature);
                            crawlOut.append(key, d);
                        }
                    }

                    // collect outlinks for subsequent db update
                    Outlink[] links = parseData.getOutlinks();
                    if (ignoreExternalLinks)
                    {
                        try
                        {
                            fromHost = new URL(fromUrl).getHost().toLowerCase();
                        }
                        catch (MalformedURLException e)
                        {
                            fromHost = null;
                        }
                    }
                    else
                    {
                        fromHost = null;
                    }

                    String[] toUrls = new String[links.length];
                    int validCount = 0;

                    for (int i = 0; i < links.length; i++)
                    {
                        String toUrl = links[i].getToUrl();

                        try
                        {
                            toUrl = urlNormalizers.normalize(toUrl,URLNormalizers.SCOPE_OUTLINK);
                            toUrl = filters.filter(toUrl); // filter the url
                            if (toUrl==null) {
                                LOG.warn("Skipping url (target) because is null."); // TODO MC remove
                            }
                        }
                        catch (Exception e)
                        {
                            toUrl = null;
                        }

                        // ignore links to self (or anchors within the page)
                        if (fromUrl.equals(toUrl))
                        {
                            toUrl = null;
                        }

                        if (toUrl != null)
                        {
                            validCount++;
                        }

                        toUrls[i] = toUrl;
                    }

                    CrawlDatum adjust = null;

                    // compute score contributions and adjustment to the
                    // original score
                    for (int i = 0; i < toUrls.length; i++)
                    {
                        if (toUrls[i] == null)
                        {
                            continue;
                        }

                        if (ignoreExternalLinks)
                        {
                            try
                            {
                                toHost = new URL(toUrls[i]).getHost().
                                        toLowerCase();
                            }
                            catch (MalformedURLException e)
                            {
                                toHost = null;
                            }

                            if (toHost == null || ! toHost.equals(fromHost))
                            {
                                // external links
                                continue; // skip it
                            }
                        }

                        CrawlDatum target = new CrawlDatum(
                                CrawlDatum.STATUS_LINKED, interval);
                        Text fromURLUTF8 = new Text(fromUrl);
                        Text targetUrl = new Text(toUrls[i]);
                        adjust = null;

                        try
                        {
                            // Scoring now expects first two arguments to be
                            // URLs (More reason to do our own scoring).
                            // St.Ack
                            adjust = scfilters.distributeScoreToOutlink(
                                    fromURLUTF8, targetUrl, parseData,
                                    target, null, links.length, validCount);
                        }
                        catch (ScoringFilterException e)
                        {
                            if (LOG.isWarnEnabled())
                            {
                                LOG.warn("Cannot distribute score from " + key
                                        + " to " + target + " - skipped ("
                                        + e.getMessage());
                            }

                            continue;
                        }

                        Text targetKey =
                                Nutchwax.generateWaxKey(targetUrl, collection);
                        crawlOut.append(targetKey, target);
                        if (adjust != null)
                        {
                            crawlOut.append(key, adjust);
                        }
                    }

                    dataOut.append(key, parseData);
                }

                public void close(Reporter reporter) throws IOException
                {
                    textOut.close();
                    dataOut.close();
                    crawlOut.close();
                }
            };
        }
    }

    public void close()
    {
        // Nothing to close.
    }

    public static void doImportUsage(final String message,
                                     final int exitCode)
    {
        if (message != null && message.length() > 0)
        {
            System.out.println(message);
        }

        System.out.println("Usage: hadoop jar nutchwax.jar import <input>" +
                " <output> <collection>");
        System.out.println("Arguments:");
        System.out.println(" input       Directory of files" +
                " listing ARC URLs to import");
        System.out.println(" output      Directory to import to. Inport is " +
                "written to a subdir named");
        System.out.println("             for current date plus collection " +
                "under '<output>/segments/'");
        System.out.println(" collection  Collection name. Added to" +
                " each resource.");
        System.exit(exitCode);
    }

    public static void main(String[] args) throws Exception
    {
        int res = new ImportWarcs().
                doMain(NutchwaxConfiguration.getConfiguration(), args);

        System.exit(res);
    }

    public int run(final String[] args) throws Exception
    {
        if (args.length != 3)
        {
            doImportUsage("ERROR: Wrong number of arguments passed.", 2);
        }

        // Assume list of ARC urls is first arg and output dir the second.
        try
        {
            importWarcs(new Path(args[0]), new Path(args[1]), args[2]);
            return 0;
        }
        catch(Exception e)
        {
            LOG.fatal("ImportWARCsFAILURE: " + StringUtils.stringifyException(e));

            return -1;
        }
    }
}
