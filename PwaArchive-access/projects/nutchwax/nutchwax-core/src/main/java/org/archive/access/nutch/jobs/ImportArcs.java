/*
 * $Id: ImportArcs.java 1521 2007-02-27 18:01:29Z stack-sf $
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.Header;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.MD5Hash;
import org.apache.hadoop.io.MapFile;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.SequenceFile.CompressionType;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RecordWriter;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.util.Progressable;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.ToolBase;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.MapWritable;
import org.apache.nutch.fetcher.Fetcher;
import org.apache.nutch.fetcher.FetcherOutput;
import org.apache.nutch.fetcher.FetcherOutputFormat;
import org.apache.nutch.metadata.Metadata;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.parse.Outlink;
import org.apache.nutch.parse.Parse;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.parse.ParseImpl;
import org.apache.nutch.parse.ParseOutputFormat;
import org.apache.nutch.parse.ParseStatus;
import org.apache.nutch.parse.ParseText;
import org.apache.nutch.parse.ParseUtil;
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
import org.archive.io.arc.ARCRecord;
import org.archive.io.arc.ARCRecordMetaData;
import org.archive.mapred.ARCMapRunner;
import org.archive.mapred.ARCRecordMapper;
import org.archive.mapred.ARCReporter;
import org.archive.util.Base32;
import org.archive.util.MimetypeUtils;
import org.archive.util.TextUtils;
import org.apache.nutch.global.Global;


/**
 * Ingests ARCs writing ARC Record parse as Nutch FetcherOutputFormat.
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
public class ImportArcs extends ToolBase implements ARCRecordMapper
{
  public  final Log LOG = LogFactory.getLog(ImportArcs.class);
  private final NumberFormat numberFormatter = NumberFormat.getInstance();

  private static final String WHITESPACE = "\\s+";

  public static final String ARCFILENAME_KEY = "arcname";
  public static final String ARCFILEOFFSET_KEY = "arcoffset";
  private static final String CONTENT_TYPE_KEY = "content-type";
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
  * Usually the URL in first record looks like this:
  * filedesc://IAH-20060315203614-00000-debord.arc.  But in old
  * ARCs, it can look like this: filedesc://19961022/IA-000001.arc.
  */
  private static final Pattern FILEDESC_PATTERN =
   Pattern.compile("^(?:filedesc://)(?:[0-9]+\\/)?(.+)(?:\\.arc)$");

  private static final Pattern TAIL_PATTERN =
    Pattern.compile("(?:.*(?:/|\\\\))?(.+)(?:\\.arc|\\.arc\\.gz)$");

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
  
  

  public ImportArcs()
  {
    super();
  }

  public ImportArcs(Configuration conf)
  {
    setConf(conf);
  }

  public void importArcs(final Path arcUrlsDir, final Path segment,
    final String collection)
    throws IOException
  {
    LOG.info("ImportArcs segment: " + segment + ", src: " + arcUrlsDir);

    final JobConf job = new JobConf(getConf(), this.getClass());

    job.set(Nutch.SEGMENT_NAME_KEY, segment.getName());

    job.setInputPath(arcUrlsDir);

    //job.setMapRunnerClass(job.getClass("wax.import.maprunner", ARCMapRunner.class));
    //job.setMapperClass(job.getClass("wax.import.mapper", this.getClass()));
    job.setMapRunnerClass( ARCMapRunner.class ); // compatible with hadoop 0.14 TODO MC
    job.setMapperClass( this.getClass() );

    job.setInputFormat(TextInputFormat.class);

    job.setOutputPath(segment);
    job.setOutputFormat(WaxFetcherOutputFormat.class);
    job.setOutputKeyClass(Text.class);
    job.setOutputValueClass(FetcherOutput.class);
    
    // Pass the collection name out to the tasks IF non-null.
    if ((collection != null) && (collection.length() > 0))
    {
      job.set(ImportArcs.WAX_SUFFIX + ImportArcs.ARCCOLLECTION_KEY,
        collection);
    }    
    job.setJobName("import " + arcUrlsDir + " " + segment);

    JobClient.runJob(job);
    LOG.info("ImportArcs: done");
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

    this.collectionName = job.get(ImportArcs.WAX_SUFFIX + ImportArcs.ARCCOLLECTION_KEY);

    // Get ARCName by reading first record in ARC?  Otherwise, we parse
    // the name of the file we've been passed to find an ARC name.
    this.arcNameFromFirstRecord = job.getBoolean("wax.arcname.from.first.record", true);
    
    this.collectionType = job.get(Global.COLLECTION_TYPE);
    this.timeoutIndexingDocument = job.getInt(Global.TIMEOUT_INDEXING_DOCUMENT, -1);   
    
    LOG.info("ImportArcs collectionType: " + collectionType);
  }

  public Configuration getConf()
  {
    return this.conf;
  }

  public void setConf(Configuration c)
  {
    this.conf = c;
  }

  public void onARCOpen()
  {
    // Nothing to do.
  }

  public void onARCClose()
  {	
	threadPool.closeAll(); // close the only thread created for this map
  }   

  public void map(final WritableComparable key, final Writable value,
    final OutputCollector output, final Reporter r)
    throws IOException
  {
    // Assumption is that this map is being run by ARCMapRunner.
    // Otherwise, the below casts fail.
    String url = key.toString();
        
    ARCRecord rec = (ARCRecord)((ObjectWritable)value).get();
    ARCReporter reporter = (ARCReporter)r;       

    // Its null first time map is called on an ARC.
    checkArcName(rec);   
    if (! isIndex(rec))
    {
      return;
    }
    checkCollectionName();
    
    final ARCRecordMetaData arcData = rec.getMetaData();
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

    final long b = arcData.getContentBegin();
    final long l = arcData.getLength();
    final long recordLength = (l > b)? (l - b): l;

    // Look at ARCRecord meta data line mimetype. It can be empty.  If so,
    // two more chances at figuring it either by looking at HTTP headers or
    // by looking at first couple of bytes of the file.  See below.
    String mimetype =
      getMimetype(arcData.getMimetype(), this.mimeTypes, url);
    
    if (skip(mimetype))
    {
      return;
    }

    // Copy http headers to nutch metadata.
    final Metadata metaData = new Metadata();
    final Header[] headers = rec.getHttpHeaders();
    for (int j = 0; j < headers.length; j++)
    {
      final Header header = headers[j];
      
      if (mimetype == null)
      {
        // Special handling. If mimetype is still null, try getting it
        // from the http header. I've seen arc record lines with empty
        // content-type and a MIME unparseable file ending; i.e. .MID.
        if ((header.getName() != null) &&
          header.getName().toLowerCase().equals(ImportArcs.CONTENT_TYPE_KEY))
        {
          mimetype = getMimetype(header.getValue(), null, null);
          
          if (skip(mimetype))
          {
            return;
          }
        }
      }
      
      metaData.set(header.getName(), header.getValue());
    }

    // This call to reporter setStatus pings the tasktracker telling it our
    // status and telling the task tracker we're still alive (so it doesn't
    // time us out).
    final String noSpacesMimetype =
      TextUtils.replaceAll(ImportArcs.WHITESPACE,
      ((mimetype == null || mimetype.length() <= 0)?
      "TODO": mimetype),
      "-");
    final String recordLengthAsStr = Long.toString(recordLength);
    
    reporter.setStatus(getStatus(url, oldUrl, recordLengthAsStr, noSpacesMimetype));

    // This is a nutch 'more' field.
    metaData.set("contentLength", recordLengthAsStr);

    rec.skipHttpHeader();
    reporter.setStatusIfElapse("read headers on " + url);

    // TODO: Skip if unindexable type.
    int total = 0;
    
    // Read in first block. If mimetype still null, look for MAGIC.
    int len = rec.read(this.buffer, 0, this.buffer.length);
    
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
    
    metaData.set(ImportArcs.CONTENT_TYPE_KEY, mimetype);

    // How much do we read total? If pdf, we will read more. If equal to -1,
    // read all.
    int readLimit = (ImportArcs.PDF_TYPE.equals(mimetype))?
      this.pdfContentLimit : this.contentLimit;
    
    // Reset our contentBuffer so can reuse.  Over the life of an ARC
    // processing will grow to maximum record size.
    this.contentBuffer.reset();
 
    while ((len != -1) && ((readLimit == -1) || (total < readLimit)))
    {
      total += len;
      this.contentBuffer.write(this.buffer, 0, len);
      len = rec.read(this.buffer, 0, this.buffer.length);
      reporter.setStatusIfElapse("reading " + url);
    }

    // Close the Record.  We're done with it.  Side-effect is calculation
    // of digest -- if we're digesting.
    rec.close();
    reporter.setStatusIfElapse("closed " + url);

    final byte[] contentBytes = this.contentBuffer.toByteArray();
    final CrawlDatum datum = new CrawlDatum();
    datum.setStatus(CrawlDatum.STATUS_FETCH_SUCCESS);

    // Calculate digest or use precalculated sha1.
    String digest = (this.sha1)? rec.getDigestStr():
    MD5Hash.digest(contentBytes).toString();
    metaData.set(Nutch.SIGNATURE_KEY, digest);
    
    // Set digest back into the arcData so available later when we write
    // CDX line.
    arcData.setDigest(digest);

    metaData.set(Nutch.SEGMENT_NAME_KEY, this.segmentName);
    
    // Score at this stage is 1.0f.
    metaData.set(Nutch.SCORE_KEY, Float.toString(datum.getScore()));

    final long startTime = System.currentTimeMillis();
    final Content content = new Content(url, url, contentBytes, mimetype,
      metaData, getConf());
    datum.setFetchTime(Nutchwax.getDate(arcData.getDate()));

    MapWritable mw = datum.getMetaData();
    
    if (mw == null)
    {
      mw = new MapWritable();
    }
            
    if (collectionType.equals(Global.COLLECTION_TYPE_MULTIPLE)) {
    	mw.put(new Text(ImportArcs.ARCCOLLECTION_KEY), new Text(SqlSearcher.getCollectionNameWithTimestamp(collectionName,arcData.getDate()))); 	
    }
    else {
    	mw.put(new Text(ImportArcs.ARCCOLLECTION_KEY), new Text(collectionName));
    }    
    mw.put(new Text(ImportArcs.ARCFILENAME_KEY), new Text(arcName));
    mw.put(new Text(ImportArcs.ARCFILEOFFSET_KEY),
      new Text(Long.toString(arcData.getOffset())));
    datum.setMetaData(mw);
          
	TimeoutParsingThread tout=threadPool.getThread(Thread.currentThread().getId(),timeoutIndexingDocument);	
	tout.setUrl(url);
    tout.setContent(content);
    tout.setParseUtil(parseUtil);          
    tout.wakeupAndWait();        
	
	ParseStatus parseStatus=tout.getParseStatus();
	Parse parse=tout.getParse();		 
	reporter.setStatusIfElapse("parsed " + url);
	   
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
    	LOG.info("multiple: "+SqlSearcher.getCollectionNameWithTimestamp(this.collectionName,arcData.getDate())+" "+url); 
    	output.collect(Nutchwax.generateWaxKey(url,SqlSearcher.getCollectionNameWithTimestamp(this.collectionName,arcData.getDate())), v); 
    }
    else {
    	output.collect(Nutchwax.generateWaxKey(url, this.collectionName), v);
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

  public void checkArcName(ARCRecord rec)
  {  
      this.arcName = rec.getMetaData().getArcFile().getName();
      this.arcName = this.arcName.replace(".arc.gz", "");   
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
  * @param rec ARC Record to test.
  * @return True if we are to index this record.
  */
  protected boolean isIndex(final ARCRecord rec)
  {
    return ((rec.getStatusCode() >= 200) && (rec.getStatusCode() < 300))
      || (this.indexRedirects && ((rec.getStatusCode() >= 300) &&
      (rec.getStatusCode() < 400)));
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
    
    // Are we to index all content?
    if (!this.indexAll)
    {
      if ((mimetype == null)
        || (!mimetype.startsWith(ImportArcs.TEXT_TYPE) && !mimetype
        .startsWith(ImportArcs.APPLICATION_TYPE)))
      {
        // Skip any but basic types.
        decision = true;
      }
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
    if ((mimetype == null) || (mimetype.length() <= 0) ||
      mimetype.startsWith(MimetypeUtils.NO_TYPE_MIMETYPE))
    {
      return null;
    }

    // Test the mimetype makes sense. If not, clear it.
    try
    {
      new MimeType(mimetype);
    }
    catch (final MimeTypeException e)
    {
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
          Text cdxLine = (Text)mw.get(ImportArcs.CDXKEY);
          
          if (cdxLine != null)
          {
            cdxOut.append(key, cdxLine);
          }
          
          mw.remove(ImportArcs.CDXKEY);
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
    int res = new ImportArcs().
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
      importArcs(new Path(args[0]), new Path(args[1]), args[2]);
      return 0;
    }
    catch(Exception e)
    {
      LOG.fatal("ImportARCs: " + StringUtils.stringifyException(e));
      
      return -1;
    }
  }
}
