package org.archive.access.nutch.jobs;

import it.unimi.dsi.webgraph.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.io.compress.*;
import org.apache.hadoop.io.IntWritable; 
import org.apache.hadoop.mapred.*;
import org.archive.access.nutch.NutchwaxConfiguration;
import org.archive.access.nutch.Nutchwax.OutputDirectories;
import org.archive.access.nutch.jobs.graph.ArcListASCIIGraphExt;
import org.archive.access.nutch.jobs.graph.GraphManager;
import org.archive.access.nutch.jobs.graph.Pagerank;
import org.archive.access.nutch.*;
import org.apache.nutch.parse.*;
import org.apache.nutch.util.*;


/**
 * Computes Pagerank and writes it to HDFS
 * @author Miguel Costa
 */
public class NutchwaxPagerank 
{
	private final static boolean DEBUG=true;
	private final static String GRAPH_FILE="graphList";
	private final static String GRAPH_BV_FILE="graphBv";
	public static final String SCORES_FILE_NAME = "scores.txt";
	private static final Log LOG = LogFactory.getLog(NutchwaxPagerank.class);	
	private static final String EXCLUDE_PATTERNS[] = {"^c=.*,u=(file|ftp|mailto|gopher).*","^c=.*,u=.*(\\.gif|\\.bmp|\\.jpg|\\.jpeg|\\.png|\\.tif)$"};
		
	private Configuration conf;
	private FileSystem fs;	
	private Pattern excludePatterns[];
	private String collection;
	private boolean ignoreInternalLinks;
	
	
	/**
	 * Constructor
	 */
	public NutchwaxPagerank() throws IOException {			
		// initialize patterns
		excludePatterns=new Pattern[EXCLUDE_PATTERNS.length];
		for (int i=0;i<EXCLUDE_PATTERNS.length;i++) {
			excludePatterns[i]=Pattern.compile(EXCLUDE_PATTERNS[i]);
		}					
		
		this.conf = new JobConf(NutchwaxConfiguration.getConfiguration());
		this.fs = FileSystem.get(conf);
		this.collection=null;
	}

	/**
	 * Constructor
	 */
	public NutchwaxPagerank(Configuration conf) throws IOException {
		this();
		this.conf = conf;
		this.fs = FileSystem.get(conf);
		this.ignoreInternalLinks = conf.getBoolean("db.ignore.internal.links", true); 
	}
	
	/**
	 * Get file system
	 * @return
	 */
	public FileSystem getFs() {
		return fs;
	}

	/**
	 * Read links from LinkDB structure
	 * @param inputSegments segments
	 * @param processor injection method
	 * @throws IOException
	 */
	public void readLinks(Path inputSegments[], ReadLinksProcessor processor) throws IOException {
		
		Text key=new Text();
		ParseData value=new ParseData();
		Outlink[] outlinks=null;
		Path parsedataPath=null;	
		String toUrl=null;
			
		for (int j=0;j<inputSegments.length;j++) {
		
			if (!fs.isDirectory(inputSegments[j])) {
				throw new IOException("ERROR: "+inputSegments[j]+" is not a directory.");
			}
													
			parsedataPath=new Path(inputSegments[j],ParseData.DIR_NAME);
			for (Path f : fs.listPaths(parsedataPath)) {
												
				if (f.getName().startsWith("part-")) {				
												
					LOG.info("reading dir "+f);
			
					MapFile.Reader reader=new MapFile.Reader(fs, (new Path(parsedataPath,f)).toString(), conf);
					//SequenceFile.Reader reader=new SequenceFile.Reader(fs, f, conf);
					while (reader.next(key,value)) {			
											
						outlinks = value.getOutlinks();
						
						for (int i=0; i<outlinks.length; i++) {
							Outlink outlink = outlinks[i];

							if (collection==null) { // something like "c=gov1,u="
								collection=key.toString().substring(0,key.toString().indexOf(",u=")+3); 
							}							
							toUrl=collection+outlink.getToUrl();

							//ignoreInternalLinks=true; TODO remove
							boolean filterLink=false;
							if (ignoreInternalLinks) {
								String fromHost = getHost(key.toString());
								String toHost = getHost(toUrl);		        
								if (toHost==null || fromHost==null || toHost.equals(fromHost)) { // internal link
									filterLink=true;
									LOG.info("pagerank filtered link: "+fromHost+" "+toHost);									
								}
							}
																					
							if (!filterLink && !filter(toUrl)) {								
								processor.run(key.toString(), toUrl);	// run abstract method 		
							}
						}												
					}	
					reader.close();
				}				
			}	
		}
	}
	
	/**
	 * Write file graph
	 * @param inputSegments
	 * @param outputPath
	 * @return number of nodes
	 * @throws IOException
	 */
	public int buildGraph(Path inputSegments[], Path outputPath) throws IOException {			  
	
		final SequenceFile.Writer writer=SequenceFile.createWriter(fs, conf, new Path(outputPath,GRAPH_FILE), ArquivoWebKeyValueWritable.class, NullWritable.class, SequenceFile.CompressionType.BLOCK, new DefaultCodec());		
		final GraphManager graph = new GraphManager();	
		
		readLinks(inputSegments, new ReadLinksProcessor() {			
			public void run(String fromUrl, String toUrl) throws IOException {								
				writer.append(new ArquivoWebKeyValueWritable(graph.getId(fromUrl),graph.getId(toUrl)),NullWritable.get());				
			}
		});

		writer.close();
		return graph.numNodes();		
	}
	
	/**
	 * Get host name
	 * @param url url
	 * @return
	 */
	private String getHost(String url) {
		url=url.substring(url.indexOf(",u=")+3);
		try {
			return new URL(url).getHost().toLowerCase();
	    }
	    catch (MalformedURLException e) {
	    	return null;
	    }
	}
	
	/**
	 * Write file with pagerank scores 	
	 */
	public void writeFileScores(Path inputSegments[], Path outputFile, final double scores[]) throws IOException {
		
		final SequenceFile.Writer writer=SequenceFile.createWriter(fs, conf, outputFile, Text.class, FloatWritable.class, SequenceFile.CompressionType.BLOCK, new DefaultCodec());
		final GraphManager graph = new GraphManager();

		readLinks(inputSegments, new ReadLinksProcessor() {			
			public void run(String fromUrl, String toUrl) throws IOException {  // read urls in the same order when it created the web graph, to write scores
				
				int id;
				if (!graph.hasId(fromUrl)) {
					id=graph.getId(fromUrl);
					writer.append(new Text(fromUrl), new FloatWritable( (float)scores[id] ));								
				}
				if (!graph.hasId(toUrl)) {
					id=graph.getId(toUrl);
					writer.append(new Text(toUrl), new FloatWritable( (float)scores[id] ));								
				}
			}	
		});
			
		writer.close();
	}
	
	
	
	/**
	 * Write text file with pagerank scores for debug
	 */
	public void writeFileScores2debug(Path inputSegments[], Path outputFile, final double scores[]) throws IOException {
		
		FSDataOutputStream out = fs.create(outputFile);
		final PrintWriter writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(out))); 		
		final GraphManager graph = new GraphManager();
		
		readLinks(inputSegments, new ReadLinksProcessor() {		
			public void run(String fromUrl, String toUrl) throws IOException {  // read urls in the same order when it created the web graph, to write scores
				
				int id;
				if (!graph.hasId(fromUrl)) {
					id=graph.getId(fromUrl);
					writer.println(fromUrl+" "+scores[id]);								
				}
				if (!graph.hasId(toUrl)) {
					id=graph.getId(toUrl);
					writer.println(toUrl+" "+scores[id]);								
				}
			}
		});
			
		writer.close();
	}
	
	

	/**
	 * Process scores
	 * @path inputSegments input segments
	 * @path output path
	 */
	public void process(Path inputSegments[], Path outputPath) throws IOException {
		
		LOG.info("Starting ");
		Path graphUnorderedPath=new Path(outputPath+"-unorderedgraph");		
		int numNodes=buildGraph(inputSegments,graphUnorderedPath);
		LOG.info("Graph created in file "+GRAPH_FILE+" with "+numNodes+" nodes.");		
					
		// sort keys,values and remove duplicates
		Path graphOrderedPath=new Path(outputPath+"-orderedgraph");	
		JobConf job = createSortKeysJob(conf, graphOrderedPath);							
		job.addInputPath(graphUnorderedPath);
		try
		{			
			JobClient.runJob(job);			
		}
		catch (IOException e)
		{			
			throw e;
		}
		LOG.info("Graph keys sorted.");			
				
		// delete graphUnorderedPath since it is not necessary anymore 
		fs.delete(graphUnorderedPath);
		
		
		FSDataInputStream in = fs.open(new Path(graphOrderedPath,"part-00000"));    
		ArcListASCIIGraphExt graphAscii = ArcListASCIIGraphExt.loadOnce(in);
		graphAscii.setNumNodes(numNodes);						
	    LOG.info("Text graph loaded to memory");	  
		ImmutableGraph.store(BVGraph.class, graphAscii, /*(new Path(outputPath,*/GRAPH_BV_FILE/*)).toString()*/);	// TODO BUG store in local system. Webgraph does not support to store to an outputstream in API	
		LOG.info("Text graph stored compressed in file "+GRAPH_BV_FILE);
		graphAscii=null;
				
		// delete graphOrderedPath since it is not necessary anymore 
		fs.delete(graphOrderedPath);
		
		
		BVGraph graphBv=BVGraph.load(/*new Path(outputPath,*/GRAPH_BV_FILE/*)).toString()*/);
		LOG.info("Compressed graph loaded to memory");
		double scores[]=Pagerank.compute(graphBv);	    
	    LOG.info("Pagerank computed");	    
	    writeFileScores(inputSegments, new Path(outputPath,SCORES_FILE_NAME), scores);
	    LOG.info("Scores written to file "+SCORES_FILE_NAME);
	    
	    // for debug
	    if (DEBUG) {
	    	writeFileScores2debug(inputSegments, new Path(outputPath,SCORES_FILE_NAME+".debug"), scores);
	    	LOG.info("Scores to debugging written to file "+SCORES_FILE_NAME+".debug");
	    }
	    
	    
	    // delete graph files	  
	    File f=new File(GRAPH_BV_FILE+".graph");
	    f.delete();
	    f=new File(GRAPH_BV_FILE+".offsets");
	    f.delete();
	    f=new File(GRAPH_BV_FILE+".properties");
	    f.delete();
	}

	/**
	 * Main
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
	    if (args.length != 1) {
	    	  System.out.println("Usage : NutchwaxPagerank <outputs>");
			  System.exit(1);
	    }	    	    	   	 
	
	    Nutchwax enclosureClass=new Nutchwax();
		Nutchwax.OutputDirectories od=enclosureClass.new OutputDirectories(new Path(args[0]));
	    
	    NutchwaxPagerank pgs=new NutchwaxPagerank();
	    pgs.process(pgs.getFs().listPaths(od.getSegments()),od.getPagerank());	    	
	}
	
	
	/**
	 * Filter link based on URL pattern
	 * @param url
	 * @return
	 */
	private boolean filter(String url) {
					
		// test patterns
		for (int i=0;i<excludePatterns.length;i++) {
			Matcher matcher = excludePatterns[i].matcher(url);
			if (matcher!=null && matcher.matches()) {				
				return true;
			}
		}		
		return false;	
	}
	
	
	/**
	 * Processor to pass to readLinks method
	 */
	public interface ReadLinksProcessor {
		public void run(String fromUrl, String toUrl) throws IOException;
	}
	
	
	
	
	/**
	 * 
	 * Reduce class to sort int keys and remove duplicated arcs
	 * 
	 */		
	public static class SortKeys extends MapReduceBase implements Reducer {
						
		public void reduce(WritableComparable key, Iterator values, OutputCollector output, Reporter reporter)
		  throws IOException {
			
			ArquivoWebKeyValueWritable keyvalue=(ArquivoWebKeyValueWritable)key;
			
			int prev=-1;
			while (values.hasNext()) { // equal keys with empty values
				if (prev==-1 || prev!=keyvalue.getValue()) { // remove duplicated arcs
					output.collect(new IntWritable(keyvalue.getKey()), new IntWritable(keyvalue.getValue()));
				}
				prev=keyvalue.getValue();
				values.next();				
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
		job.setJobName("sort key,values" + outputPath);

		job.setInputFormat(SequenceFileInputFormat.class);
		job.setOutputFormat(TextOutputFormat.class);		
		//job.setMapperClass(NutchwaxPagerank.class);   
		job.setReducerClass(SortKeys.class); 				
		job.setOutputPath(outputPath);							
		job.setMapOutputKeyClass(ArquivoWebKeyValueWritable.class);
		job.setOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(NullWritable.class);
		job.setOutputValueClass(IntWritable.class);
		job.setPartitionerClass(NutchwaxPagerankPartitioner.class); // to partition for one reducer only		
		//job.setOutputValueGroupingComparator(IntWritable.Comparator.class); // to sort values - NOT SUPPORTED IN THIS HADOOP VERSION
		
		return job;
	}	
	
}
