package org.archive.nutch.trec;

import java.util.*;
import java.io.*;

import org.apache.nutch.searcher.*;
import org.apache.lucene.search.ArquivoWebFunctionsWritable;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.parse.ParseData;
import org.apache.hadoop.io.LongWritable;


public class TRECBean extends NutchBean {
	
	private final static String BOOST_LABEL="BOOST";
	//private static ArquivoWebWritable boostParams=null;
	private static ArquivoWebFunctionsWritable functions=null;
	
	/**
	 * Constructor
	 * @param conf
	 * @param path search-servers.txt dir
	 * @param boostsFile function/boost entries file
	 * @throws IOException
	 */
	public TRECBean(Configuration conf, Path path, File boostsFile) throws IOException {
		super(conf,path);			
	    
	    Properties props=new Properties();
	    props.load(new FileInputStream(boostsFile));
	    	   
	    functions=new ArquivoWebFunctionsWritable();
	    String key=null;
	    int index;
	    float boost;
	    for (Enumeration e = props.keys(); e.hasMoreElements();) {
    		key=(String)e.nextElement();
    		index=Integer.parseInt(key.substring(BOOST_LABEL.length()));
    		boost=Float.parseFloat(props.getProperty(key));
    		functions.addFunction(index, boost);
        }
	    
	    /* TODO remove
	    boostParams=new ArquivoWebWritable();
		boostParams.setUrlBoost(Float.parseFloat(props.getProperty("url")));
		boostParams.setAnchorBoost(Float.parseFloat(props.getProperty("anchor")));
		boostParams.setContentBoost(Float.parseFloat(props.getProperty("content")));
		boostParams.setTitleBoost(Float.parseFloat(props.getProperty("title")));
		boostParams.setHostBoost(Float.parseFloat(props.getProperty("host")));
		boostParams.setPhraseBoost(Float.parseFloat(props.getProperty("phrase")));
		boostParams.setSlop(Integer.parseInt(props.getProperty("slop")));
		*/
	    
	}
	
	  public static void main(String[] args) throws Exception {
		    String usage = "org.archive.nutch.trec.TRECBean <inputfile (trecTopics.txt)> <runid> <topDocsReturned (1000)> <maxMatches (-1=all|-2=default)> <maxDups> <search-servers.txt dir> <ranking file> <debug (true|false)>";
            
		    if (args.length < 8) {
		      System.err.println(usage);
		      System.exit(-1);
		    }
		    int topDocsReturned = 20;
		    int maxMatches = 1000;
		    int maxDups = 2;
		    try {
		    	topDocsReturned = Integer.parseInt(args[2]);
		    	maxMatches = Integer.parseInt(args[3]);
		    	maxDups = Integer.parseInt(args[4]);
		    } catch (ArrayIndexOutOfBoundsException e) {
		    	throw e;
		    }
		    
		    boolean debug=Boolean.parseBoolean(args[7]);
	        
		    Configuration conf = NutchConfiguration.create();
		    if (debug) {
		    	System.out.println("Conf: "+conf.toString());
		    	System.out.println("args: "+args[0]+" "+args[1]+" "+args[2]+" "+args[3]+" "+args[4]+" "+args[5]+" "+args[6]+" "+args[7]);
		    }
		    //NutchBean bean = new NutchBean(conf);
		    
		    try {
		    	TRECBean bean = new TRECBean(conf,new Path(args[5]),new File(args[6]));
			    
		        BufferedReader in = new BufferedReader(new FileReader(args[0]));
		        String str;
		        String qid=null;
		        String query_str=null;
		        while ((str = in.readLine()) != null) {
		        	
		        	if (str.startsWith("<num> Number:")) {
		        		qid = str.substring("<num> Number: WT04-:".length()-1);
		        	}
		        	else if (str.startsWith("<title>")) {		        	
		        		query_str = str.substring("<title>".length()+1);		        		
		        		
					    Query query = Query.parse(query_str, conf);		
					    int maxHitsPerVersion=100;
					    
					    //Hits hits = bean.search(query, topDocsReturned);
					    //Hits hits = bean.search(query, topDocsReturned, maxMatches, maxDups, "site", null, false, null); 			
					    Hits hits = bean.search(query, topDocsReturned, maxMatches, maxDups, "site", null, false, functions, maxHitsPerVersion); 
					    int length = (int)Math.min(hits.getLength(), topDocsReturned);					       
					    Hit[] show = hits.getHits(0, length);
					    HitDetails[] details = bean.getDetails(show);
					    
					    if (debug) {
		        			System.out.println();
		        			System.out.println(qid+":"+query_str);
		        			System.out.println("topDocsReturned:"+topDocsReturned+" hits:"+length);
		        		}
					    
					    for (int i = 0; i < length; i++) {
					      //String docno = bean.getParseData(details[i]).getMeta("DOCNO");
					      String docno = details[i].getValue("DOCNO");
					      
					      float sim = Float.parseFloat(show[i].getSortValue().toString());
					      System.out.println(qid+"\tQ0\t"+ docno+ "\t" + i + "\t" +(sim*1000)+
					    		  "\t"+ args[1]);
					    }
					    
					    qid=null;
					    query_str=null;
		        	}
		        }
		        in.close();
		    } 
		    catch (IOException e) {
		    	System.err.println("Problem reading query file: "+e.getMessage());
		    	e.printStackTrace();
		    	System.err.println(usage);
		    }
		    catch (Exception e) {
		    	System.err.println("Problem: "+e.getMessage());
		    	e.printStackTrace();
		    	System.err.println(usage);
		    }
	  }
}
