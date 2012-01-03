package pt.arquivo.processor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ObjectWritable;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.FileAlreadyExistsException;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.ToolBase;
import org.apache.nutch.fetcher.FetcherOutputFormat;
import org.apache.nutch.metadata.Nutch;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;
import org.apache.nutch.parse.ParseUtil;
import org.apache.nutch.util.mime.MimeType;
import org.apache.nutch.util.mime.MimeTypeException;
import org.apache.nutch.util.mime.MimeTypes;
import org.archive.access.nutch.NutchwaxConfiguration;
import org.archive.io.ArchiveRecordHeader;
import org.archive.io.arc.ARCRecord;
import org.archive.io.arc.ARCRecordMetaData;
import org.archive.mapred.ARCMapRunner;
import org.archive.mapred.ARCRecordMapper;
import org.archive.mapred.ARCReporter;
import org.archive.util.MimetypeUtils;


/**
 * Process sequentially all files from arcs  
 */
public abstract class ProcessArcs extends ToolBase implements ARCRecordMapper, Reducer {
	protected final Log LOG = LogFactory.getLog(ProcessArcs.class);

	private static final String HTMLCONTENTTYPE = "text/html";
	private MimeTypes mimeTypes;
	private boolean indexRedirects;	
	protected enum Counter {
         HTMLFILES, NOTHTMLTYPE, TOTALFILES, NOTMIMETYPE, ISINDEX
	}

	/**
	 * Buffer to reuse on each ARCRecord indexing.
	 */
	private final byte[] buffer = new byte[1024 * 16];

	private final ByteArrayOutputStream contentBuffer = new ByteArrayOutputStream(
			1024 * 16);

	/**
	 * Constructor
	 */
	public ProcessArcs() {
		super();
	}

	/**
	 * Constructor
	 * @param conf configuration
	 */
	public ProcessArcs(Configuration conf) {
		setConf(conf);
	}

	/**
	 * Verifies if it is the first block of the ARC file
	 * @param rec ARC Record to test.
	 * @return True if we are to index this record.
	 */
	protected boolean isIndex(final ARCRecord rec) {
		return ((rec.getStatusCode() >= 200) && (rec.getStatusCode() < 300))
				|| (this.indexRedirects && ((rec.getStatusCode() >= 300) && (rec
						.getStatusCode() < 400)));
	}
	
	/**
	 * Processment for a file	
	 * @param in file after processing
	 * @param out output
	 * @param err errors detected
	 * @param header metadata of file
	 */
	protected abstract void processor(InputStream in, OutputStream out, OutputStream err, ArchiveRecordHeader header, ARCReporter reporter);
	
	/**
	 * Filter for a file	 
	 * @param mimetype mimetype
	 */
	protected abstract boolean filter(String mimetype);
	
	
	/**
	 * Process an ARC file	 
	 * @param arcDir input directory containing file with arc list
	 * @param outputDir output directory
	 * @throws IOException
	 */
	public void process(final Path arcDir, final Path outputDir) throws IOException {
		LOG.info("process: input: " + arcDir + " output: " + outputDir);

		final JobConf job = new JobConf(getConf(), this.getClass());
		job.setInputPath(arcDir);
		job.setOutputPath(outputDir);		
		job.setMapRunnerClass(job.getClass("wax.import.maprunner",	ARCMapRunner.class));
		job.setMapperClass(this.getClass());		
		job.setReducerClass(this.getClass());
		job.setInputFormat(TextInputFormat.class);
		
		JobClient.runJob(job);
		LOG.info("process: done");
	}

	/**
	 * Configures the job
	 * @param job job to configure
	 */
	public void configure(final JobConf job) {
		setConf(job);
		
		this.mimeTypes = MimeTypes.get(job.get("mime.types.file"));
		this.indexRedirects = job.getBoolean("wax.index.redirects", false); 
	}

	/**
	 * Get configuration
	 */
	public Configuration getConf() {
		return this.conf;
	}

	/**
	 * Set configuration
	 * @param conf configuration
	 */
	public void setConf(Configuration conf) {
		this.conf = conf;
	}

	public void onARCOpen() {
		// Nothing to do.
	}

	public void onARCClose() {
		// Nothing to do.
	}

	/**
	 * Map	
	 */
	public void map(final WritableComparable key, final Writable value,
			final OutputCollector output, final Reporter r) throws IOException {
		// Assumption is that this map is being run by ARCMapRunner.
		// Otherwise, the below casts fail.
		String url = key.toString();
		ARCRecord rec = (ARCRecord) ((ObjectWritable) value).get();
		ARCReporter reporter = (ARCReporter) r;
		
		reporter.incrCounter(Counter.TOTALFILES, 1);		
        if (!isIndex(rec)) {  // If it is the first record skip it so there are no errors from this record.
        	reporter.incrCounter(Counter.ISINDEX, 1);
            return;
        }
        
		final ARCRecordMetaData arcData = rec.getMetaData();
		// Look at ARCRecord meta data line mimetype. It can be empty. If so,
		// two more chances at figuring it either by looking at HTTP headers or
		// by looking at first couple of bytes of the file. See below.
		String mimetype = getMimetype(arcData.getMimetype(), this.mimeTypes, url);
		rec.skipHttpHeader();
		reporter.setStatusIfElapse("read headers on " + url);

		// Read in first block. If mimetype still null, look for MAGIC.
		int len = rec.read(this.buffer, 0, this.buffer.length);
		// check mimetype
		if (mimetype == null) {
			MimeType mt = this.mimeTypes.getMimeType(this.buffer);

			if (mt == null || mt.getName() == null) {
				LOG.warn("ProcessArcs" + "Failed to get mimetype for: " + url);

				return;
			}

			mimetype = mt.getName();
		}
		
		// filter documents
		if (filter(mimetype)) {
			return;
		}		
		
		// Reset our contentBuffer so can reuse. Over the life of an ARC
		// processing will grow to maximum record size.
		this.contentBuffer.reset();

		int total = 0;
		while ((len != -1)) {
			total += len;
			this.contentBuffer.write(this.buffer, 0, len);
			len = rec.read(this.buffer, 0, this.buffer.length);
			reporter.setStatusIfElapse("reading " + url);
		}

		// Close the Record. We're done with it. Side-effect is calculation
		// of digest -- if we're digesting.
		rec.close();
		reporter.setStatusIfElapse("closed " + url);

		final byte[] contentBytes = this.contentBuffer.toByteArray();

		// Html file from ARC
		ByteArrayInputStream in = new ByteArrayInputStream(contentBytes);
		// HTML file with correct formating is to put here
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		// HTML errors are put here
		ByteArrayOutputStream err = new ByteArrayOutputStream();
		// Process file
		processor(in, out, err, rec.getHeader(), reporter);
	}
	
	/**
	 * Reducer
	 */
	public void reduce(WritableComparable arg0, Iterator arg1,
			OutputCollector arg2, Reporter arg3) throws IOException {
		// TODO Auto-generated method stub
		return;
	}


	/**
	 * Get mimetype
	 * @param mimetype mimetype
	 * @param mts mimetypes known
	 * @param url url
	 * @return mimetype
	 */
	protected String getMimetype(final String mimetype, final MimeTypes mts,
			final String url) {
		if (mimetype != null && mimetype.length() > 0) {
			return checkMimetype(mimetype.toLowerCase());
		}

		if (mts != null && url != null) {
			final MimeType mt = mts.getMimeType(url);

			if (mt != null) {
				return checkMimetype(mt.getName().toLowerCase());
			}
		}

		return null;
	}

	/**
	 * Check mimetype 
	 * @param mimetype mimetype
	 * @return mimetype
	 */
	protected static String checkMimetype(String mimetype) {
		if ((mimetype == null) || (mimetype.length() <= 0)
				|| mimetype.startsWith(MimetypeUtils.NO_TYPE_MIMETYPE)) {
			return null;
		}

		// Test the mimetype makes sense. If not, clear it.
		try {
			new MimeType(mimetype);
		} catch (final MimeTypeException e) {
			mimetype = null;
		}

		return mimetype;
	}

	public void close() {
		// Nothing to close.
	}

	/**
	 * Display usage information
	 * @param message
	 * @param exitCode
	 */
	public static void usage(final String message, final int exitCode) {
		if (message != null && message.length() > 0) {
			System.out.println(message);
		}

		System.out.println("Usage: hadoop jar pwaprocessor.jar <input> <output>");
		System.out.println("Arguments:");
		System.out.println(" input\tDirectory of files"
				+ " listing ARC URLs to import");
		System.out.println(" output\tDirectory for output files");		
		System.exit(exitCode);
	}

	/**
	 * Run hadoop process
	 * @param args
	 * @return status of run
	 * @throws Exception
	 */
	public int run(final String[] args) throws Exception {
		if (args.length != 2) {
			usage("ERROR: Wrong number of arguments passed.", 2);
		}

		// Assume list of ARC urls is first arg and output dir the second.
		try {
			process(new Path(args[0]), new Path(args[1]));
			return 0;
		} 
		catch (FileAlreadyExistsException e) {
			LOG.fatal("ERROR: choose a different output directory.");
			return -1;
		}
		catch (Exception e) {
			LOG.fatal("ProcessArcs: " + StringUtils.stringifyException(e));
			return -1;
		}
	}
}