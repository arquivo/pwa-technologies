package org.archive.access.nutch.jobs;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.io.compress.*;
import org.apache.nutch.parse.*;
import org.apache.nutch.util.*;
import org.archive.access.nutch.*;


/**
 * Sort urls by groups 
 * @author Miguel Costa
 */
public class NutchwaxCache 
{	
	private static final Log LOG = LogFactory.getLog(NutchwaxCache.class);	
		
	private Configuration conf;
	private FileSystem fs;	
	
	
	/**
	 * Constructor
	 */
	public NutchwaxCache() throws IOException {				
		this.conf = new JobConf(NutchwaxConfiguration.getConfiguration());
		this.fs = FileSystem.get(conf);		
	}

	/**
	 * Constructor
	 */
	public NutchwaxCache(Configuration conf) throws IOException {
		this();
		this.conf = conf;
		this.fs = FileSystem.get(conf);		
	}
	
	/**
	 * Get file system
	 * @return
	 */
	public FileSystem getFs() {
		return fs;
	}

	

	/**
	 * Process 
	 * @path indexPath index path
	 * @path outputPath output path
	 */
	public void process(Path indexPath, Path outputPath) throws IOException {
		
		LOG.info("Starting ");
					
		// sort keys,values and remove duplicates		
		JobConf job = createSortKeysJob(conf, outputPath);							
		job.addInputPath(indexPath);
		
		try	{			
			JobClient.runJob(job);			
		}
		catch (IOException e) {			
			throw e;
		}
		LOG.info("Sort ended.");									
	}

	/**
	 * Main
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
	    if (args.length != 2) {
	    	  System.out.println("Usage : NutchwaxCache <index dir> <output dir>");
			  System.exit(1);
	    }	    	    	   	 
		    
	    NutchwaxCache pgs=new NutchwaxCache();
	    pgs.process(new Path(args[0]),new Path(args[1]));	    	
	}
	
	

	
	
	/**	 
	 * Reduce class to sort int keys and remove duplicated arcs	
	 */		
	public static class SortKeys extends MapReduceBase implements Mapper, Reducer {
						
		public void map(WritableComparable key, Writable value, OutputCollector output, Reporter reporter) throws IOException {
			String parts[]=value.toString().split(" ");
			ArquivoWebComposedKeyWritable newKey=new ArquivoWebComposedKeyWritable(EntryPageExpansion.getRadical(parts[0]),parts[1]); 									
			output.collect(newKey,value);
		}
		
		public void reduce(WritableComparable key, Iterator values, OutputCollector output, Reporter reporter)
		  throws IOException {
						
			while (values.hasNext()) { // equal keys with empty values
				output.collect(key, (Text)values.next());
			}						
		}
	}	
	
	/**
	 * Create job
	 * @param config
	 * @param outputPath
	 * @return
	 */		
	private JobConf createSortKeysJob(Configuration config, Path outputPath) {								
		JobConf job = new NutchJob(config);
		job.setJobName("sort by url, timestamp " + outputPath);

		job.setInputFormat(TextInputFormat.class);
		job.setOutputFormat(TextOutputFormat.class);		
		
		job.setMapperClass(SortKeys.class);
		job.setReducerClass(SortKeys.class);		
		job.setOutputPath(outputPath);		
		
		job.setMapOutputKeyClass(ArquivoWebComposedKeyWritable.class);		
		job.setMapOutputValueClass(Text.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		job.setPartitionerClass(NutchwaxPagerankPartitioner.class); // to partition for one reducer only		
		//job.setOutputValueGroupingComparator(IntWritable.Comparator.class); // to sort values - NOT SUPPORTED IN THIS HADOOP VERSION
		
		return job;
	}	
	
}
