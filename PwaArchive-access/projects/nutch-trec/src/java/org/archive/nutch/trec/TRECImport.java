/* Import TREC collections like .GOV .GOV2 into nutch
 */

package org.archive.nutch.trec;

import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPInputStream;
import java.util.Iterator;
import java.util.Map;
import org.apache.hadoop.io.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.mapred.*;
import org.apache.nutch.fetcher.FetcherOutput;
import org.apache.nutch.fetcher.FetcherOutputFormat;
import org.apache.nutch.fetcher.Fetcher;
import org.apache.nutch.parse.ParseUtil;
import org.apache.nutch.util.NutchJob;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.crawl.CrawlDatum;
import org.apache.nutch.crawl.Generator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//TODO MC
import org.apache.nutch.metadata.Nutch; 
import org.apache.nutch.net.URLNormalizers;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.TextInputFormat;
import org.archive.access.nutch.jobs.ImportArcs;
import org.archive.access.nutch.jobs.ImportArcs.WaxFetcherOutputFormat;
import org.apache.nutch.net.URLFilters;
import org.apache.nutch.net.URLNormalizers;
//TODO MC


public class TRECImport extends MapReduceBase implements Mapper/*, Reducer*/ {
  public final Log LOG = LogFactory.getLog(TRECImport.class);
  private static final File TMPDIR =
      new File(System.getProperty("java.io.tmpdir", "/data/tmp")); // TODO MC - change from /tmp
  
  private String segmentName;
  private String collectionName; // TODO MC
  private URLNormalizers urlNormalizers; // TODO MC 
  private URLFilters filters; // TODO MC
  private JobConf conf;
  
  public void map(final WritableComparable key, final Writable value,
      final OutputCollector output, final Reporter reporter) {

    InputStream inputFile = null;
    TRECParser trec = null;
    ParseUtil pu = null;
    
    LOG.info("Getting: " + value.toString());
    try {
        inputFile = getTrec(value.toString());    
    } catch(MalformedURLException e) {
    	LOG.error(e.getMessage());
    } catch(IOException e) {
    	LOG.error(e.getMessage());
    }

    //Get parser for nutch
    pu = new ParseUtil(this.conf);
    //Get a javacc parser
    trec = new TRECParser(inputFile);

    // Run the parser
    try {
      trec.Input(pu, output, this.conf, this.segmentName, this.collectionName, value.toString(), urlNormalizers, filters);
    } catch (ParseException e) {
    	LOG.error("ParseException " + e.getMessage());
    } catch (IOException e) {
    	LOG.error("IOException " + e.getMessage());
    } catch (TokenMgrError e) {
    	LOG.error("TokenMgrError " + e.getMessage());
    } catch (StackOverflowError e) {
    	LOG.error("StackOverflowError " + e.getMessage());
    }
  }  

  public void configure(final JobConf job) {
    this.conf = job;
    this.segmentName = job.get(Nutch.SEGMENT_NAME_KEY);
    this.collectionName = job.get(ImportArcs.WAX_SUFFIX + ImportArcs.ARCCOLLECTION_KEY); // TODO MC
    this.urlNormalizers = new URLNormalizers(job, URLNormalizers.SCOPE_FETCHER); // TODO MC
    this.filters = new URLFilters(job); // TODO MC
  }
  
  public void close() {
	  //empty close method
  }

  
  /*
   * Methods to get local copies of the trec file
   */
  
  public InputStream getTrec(String trecFileOrUrl)
  throws MalformedURLException, IOException {
      return trecFileOrUrl.startsWith("http://")?
          getTrec(new URL(trecFileOrUrl)): getTrec(new File(trecFileOrUrl));
  }
  
  public InputStream getTrec(final File trecFile) throws IOException {
	  FileInputStream f = new FileInputStream(trecFile);
	  // Uber-simple method to detect gzipped
	  if (trecFile.getName().endsWith(".gz")) {
		  return new GZIPInputStream(f);
	  } else {
		  return f;
	  }
  }
  
  public InputStream getTrec(final URL trecUrl)
  throws IOException {

      // If url represents a local file then return file it points to.
      if (trecUrl.getPath() != null) {
          // TODO: Add scheme check and host check.
          File f = new File(trecUrl.getPath());
          if (f.exists()) {
              return getTrec(f);
          }
      }
      
      // Else bring the AC local.
      return makeTRECLocal(trecUrl.openConnection());
  }
  
  protected InputStream makeTRECLocal(final URLConnection connection)
  throws IOException {
      if (connection instanceof HttpURLConnection) {
          connection.connect();
          if (connection.getURL().toString().endsWith(".gz")) {
        	  return new GZIPInputStream(connection.getInputStream());
          } else {
        	  return connection.getInputStream();
          }
      }
      throw new UnsupportedOperationException("No support for " +
              connection);
  }

  /* Main method
   */ 
   
  public static void main(String[] args) throws IOException {
    if ( args.length < 3) {
      doTRECUsage("ERROR: Wrong number of arguments passed.", 3);
    }
    JobConf conf = new NutchJob(NutchConfiguration.create());
    conf.setJobName("TRECImport");
    String segmentName =  Generator.generateSegmentName();
    conf.set(Nutch.SEGMENT_NAME_KEY, segmentName);
    conf.setInputPath(new Path(args[0]));
    conf.setOutputPath(new Path(args[1] + "/segments/" +
    		 segmentName));
        
    //conf.setOutputKeyClass(UTF8.class); TODO MC - deprecated
    conf.setOutputKeyClass(Text.class); // TODO MC
    conf.setOutputValueClass(FetcherOutput.class);
    //conf.setOutputFormat(FetcherOutputFormat.class); // TODO MC
    conf.setOutputFormat(WaxFetcherOutputFormat.class); // TODO MC    
    
    conf.setMapperClass(TRECImport.class);
//    conf.setReducerClass(TRECImport.class); // TODO MC
    
    conf.set(ImportArcs.WAX_SUFFIX + ImportArcs.ARCCOLLECTION_KEY, args[2]); // TODO MC - set collection name
    
    JobClient.runJob(conf);
  }
 
  public static void doTRECUsage(final String message,
  	 final int exitCode) {
    if (message != null && message.length() > 0) {
      System.out.println(message);
    }
    System.out.println("Usage: hadoop jar nutch*.jar " +
      "org.apache.nutch.TrecImport.TRECImport <input> <output> <collection>");
    System.out.println("Arguments:");
    System.out.println(" input       Directory of files" +
      " listing files/URLs to import.");
    System.out.println(" output      Directory to import to. Inport is " +
       "written to a subdir named");
    System.out.println("             for current date under " +
        "'<output>/segments/'.");
    System.out.println(" collection  Collection name.");
    System.exit(exitCode);
  }

  
  /* TODO MC */
  /*
  public void reduce(final WritableComparable key, Iterator values,
		    final OutputCollector output, Reporter reporter)
		    throws IOException
  {
	  Writable o=null;
	  System.out.println("instance key:"+key.getClass().getName());
	  System.out.println("key:"+key);
	  while (values.hasNext()) {
		 o=(Writable)values.next();		 
		 System.out.println("instance value:"+o.getClass().getName());
	     System.out.println("value:"+o.toString());
	     output.collect(key, o);
	  }	 
  }
*/
  
}

