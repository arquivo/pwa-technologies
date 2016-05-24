package org.archive.nutch.trec;

import org.apache.lucene.document.*;
import org.apache.lucene.queryParser.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.rankers.IRankingFunction;

import java.io.*;
import java.util.*;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.parse.ParseData;
import org.apache.nutch.searcher.NutchBean;
import org.apache.nutch.searcher.Hits;
import org.apache.hadoop.io.LongWritable;

import org.apache.nutch.searcher.basic.BasicQueryFilter;
import org.apache.nutch.searcher.NutchBean;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.Query.Clause;
import org.apache.nutch.searcher.HitDetails;
import org.apache.nutch.indexer.NutchSimilarity;
import org.apache.nutch.analysis.CommonGrams;


/**
 * Extract TREC qrels and Lucene ranking results (from explanation)
 * @author Miguel Costa
 */
public class TRECFeaturesExtractor extends NutchBean {
	
	  private HashMap<String,String> hmap=null;
	
	  /**
		* Constructor
		* @param conf
		* @param path search-servers.txt dir
		* @param boostsFile boosts file
		* @throws IOException
		*/
	  public TRECFeaturesExtractor(Configuration conf, Path path) throws IOException {
		  super(conf,path);
		  
		  hmap=new HashMap<String,String>();		  		  		
	  }
	  	  
	  /**
	   * Read queries from TREC benchmark GOV1
	   */
	  private void readQueries(String filename) throws Exception {
		  BufferedReader in = new BufferedReader(new FileReader(filename));
		  String str;
		  String qid=null;
		  String queryStr=null;
		  while ((str = in.readLine()) != null) {
	        	
			  if (str.startsWith("<num> Number:")) {
				  qid = str.substring("<num> Number: WT04-:".length()-1);
			  }
	       	  else if (str.startsWith("<title>")) {		        	
	       		  queryStr = str.substring("<title>".length()+1);	
	       		  hmap.put(qid, queryStr);	       			   
	       			
	       		  qid=null;
	       		  queryStr=null;			    			   
	       	  }		    		
		  }        
		  in.close();
	  }	
	  
	  /**
	   * Get query
	   * @param qid query id
	   */
	  private String getQuery(String qid) {
		  return hmap.get(qid);
	  }
	  
 
	  
	  /**
	   * Get ranking values from explanation structure
	   * @param expl
	   * @param tab tabulator
	   * @param featuresBoosts boosts ofr each feature
	   */
	  /*
	  private static float[] getValuesFromExplanation(Explanation expl, String tab, float featuresBoosts[]) {
			   		   
		    if (expl.getDescription().startsWith("weight(url:")) {
		    	featuresBoosts[0]+=expl.getValue();
		    }
		    else if (expl.getDescription().startsWith("weight(anchor:")) {
		    	featuresBoosts[1]+=expl.getValue();
		    }
		    else if (expl.getDescription().startsWith("weight(content:")) {
		    	featuresBoosts[2]+=expl.getValue();
		    }
		    else if (expl.getDescription().startsWith("weight(title:")) {
		    	featuresBoosts[3]+=expl.getValue();
		    }
		    else if (expl.getDescription().startsWith("weight(host:")) {
		    	featuresBoosts[4]+=expl.getValue();
		    }
		    
		  	Explanation explDetails[]=expl.getDetails();
		  	if (explDetails!=null) {
		  		for (int i=0;i<explDetails.length;i++) {				
		  			getValuesFromExplanation(explDetails[i],tab+" ",featuresBoosts);
		  		}
		  	}
		  	
		  	return featuresBoosts;
	  }
	  */

	
	  /**
	   * Main
	   * @param args
	   * @throws Exception
	   */
	  public static void main(String[] args) throws Exception {
		    String usage = "org.archive.nutch.trec.TRECFeaturesExtractor <qrels file> <index dir> <queries file> <debug>";
            
		    if (args.length < 4) {
		      System.err.println(usage);
		      System.exit(-1);
		    }		    		   
		    
		    try {
		    	Configuration conf = NutchConfiguration.create();
		    	conf.set("arquivo.include.types","html|xhtml+xml|xml|pdf|postscript|text|msword|vnd.ms-powerpoint|rtf|richtext"); // at nutch-site.xml
		    
		    	TRECFeaturesExtractor extractorBean=new TRECFeaturesExtractor(conf,new Path(args[1]));
		    	Query queryInput = null;		    	
		    	BooleanQuery queryOutput = null;
		    	TermQuery queryDocno = null;
		    	boolean debug=Boolean.parseBoolean(args[3]);
		    	
		    	/*
		    	BasicQueryFilter basicFilter=new BasicQueryFilter();		    	
		    	basicFilter.setConf(conf); // must be before set boosts		    	
		    	basicFilter.setUrlBoost(1);
		    	basicFilter.setAnchorBoost(1);
		    	basicFilter.setContentBoost(1);
		    	basicFilter.setTitleBoost(1);
		    	basicFilter.setHostBoost(1);
		    	basicFilter.setPhraseBoost(1);
		    	basicFilter.setSlop(3);	// constant
		    	*/
		    			    	
		    	extractorBean.readQueries(args[2]);
		    			    		    	
		    	Directory idx = FSDirectory.getDirectory(args[1], false);
		    	org.apache.lucene.search.Searcher searcher = new IndexSearcher(idx);
		    	org.apache.lucene.index.IndexReader reader=IndexReader.open(idx);
		    	searcher.setSimilarity(new NutchSimilarity()); // line ignored; ranking is hard coded in lucene			    		    
		    	
		        BufferedReader in = new BufferedReader(new FileReader(args[0]));
		        String str;
		        String splitStr[]=null;
		        String qid=null;		   
		        String docno=null;
		        String srel=null;
		        
		        ArquivoWebFunctionsWritable functions=new ArquivoWebFunctionsWritable();		        
				for (int i=0;i<IRankingFunction.functions.length;i++) { // all functions with boost 1
					functions.addFunction(i,1);
				}
						        
		        while ((str = in.readLine()) != null) {		        			        	
		        	splitStr=str.split(" ");
		        	qid = splitStr[0];
		        	docno = splitStr[2];
		        	srel = splitStr[3];		        			                				   
		        	
		        	// build query
		        	queryInput = Query.parse(extractorBean.getQuery(qid), conf);
		        			           		        			       
		        	queryOutput=new BooleanQuery();		        			        			        			      
		        	//basicFilter.filter(queryInput, queryOutput);
		        	extractorBean.buildQuery(queryInput,queryOutput,conf);
		        	queryDocno=new TermQuery(new Term("DOCNO",docno));
		        	queryOutput.add(queryDocno, BooleanClause.Occur.MUST);
		        	
		        	if (debug) {
						System.out.println("Query:"+queryOutput);
					}
		        	
		        	/*
		        	org.apache.lucene.search.Hits hits = searcher.search(queryOutput);  // limited to 1000 		        	      			        			  		  
		        	int length = hits.length();
		        	*/
		        	
		        	int length=0;
		        	int firstDoc=0;
					ArquivoWebScorer scorer=new ArquivoWebScorer(queryOutput, searcher, reader, functions);
		  			while (scorer.next()) {
		  				if (length==0) {
		  					firstDoc=scorer.doc();
		  				}
						length++;
					}
		  			
		        	/*
		        	Hits hits = extractorBean.search(queryInput, 10, -1, 0, "site", null, false, boostParams);					
		  		    int length = (int)Math.min(hits.getLength(), 10);					       
					*/
					
					if (length>1) { // sanity check
						throw new Exception("Unexpected length:"+length);
					}				
					if (length<=0) {						
						System.out.println("rel:"+srel+" qid:"+qid+" docno:"+docno+" NOT FOUND");	
					}
					else if (length==1) {						
						//System.out.println(qid+" "+queryInput+" "+docno);
						//float featuresBoosts[]=new float[6];
						//featuresBoosts=getValuesFromExplanation(searcher.explain(queryOutput,hits.id(0)),"",featuresBoosts);											
					
						System.out.print("rel:"+srel+" qid:"+qid+" docno:"+docno+" ");
						//Explanation explDetails[]=searcher.explain(queryOutput,hits.id(0)).getDetails();
						Explanation expl[]=scorer.explain(firstDoc).getDetails();	
						Explanation expl2[];
						if (expl!=null) {
							for (int i=0;i<expl.length;i++) {				
								System.out.print(expl[i].getDescription()+":"+expl[i].getValue()+" ");
								expl2=expl[i].getDetails();
								if (expl2!=null) {
									for (int j=0;j<expl2.length;j++) {
										System.out.print(expl2[j].getDescription()+":"+expl2[j].getValue()+" ");
									}								
								}
							}
						}	
						System.out.println();
						
						/*
						for (int i=0;i<featuresBoosts.length;i++) {
							System.out.print((i+1)+":"+featuresBoosts[i]+" ");
						}
						*/
						//System.out.print("expl:"+searcher.explain(queryOutput,hits.id(0))+" ");					
						//System.out.println("#docid = "+docno);
						/*
						HitDetails details=extractorBean.getDetails(hits.getHit(0));
						System.out.println(details.toHtml());
						*/				
					}
					//System.out.println("length:"+length);
										
		        }
		        in.close();		        		    
		    } 
		    catch (IOException e) {
		    	System.err.println("Problem reading file: "+e.getMessage());
		    	e.printStackTrace();
		    	System.err.println(usage);
		    }
		    catch (Exception e) {
		    	System.err.println("Problem: "+e.getMessage());
		    	e.printStackTrace();
		    	System.err.println(usage);
		    }
	  }
	  
	  
	  
	  private static final String[] FIELDS = { "url", "anchor", "content", "title", "host" };		  
	  //private float[] FIELD_BOOSTS = new float[5];
	  
	  /**
	   * Build boolean query
	   * @param input
	   * @param output
	   */
	  private void buildQuery(Query input, BooleanQuery output, Configuration conf) {
		  	Clause[] clauses = input.getClauses();
		    for (int i = 0; i < clauses.length; i++) {
		    	Clause c = clauses[i];

		    	if (!c.getField().equals(Clause.DEFAULT_FIELD))
		    		continue;                                 // skip non-default fields
		    			    
		    	String[] sterms = null;
		    	if (c.isPhrase()) { 
	    			//sterms = new CommonGrams(conf).optimizePhrase(c.getPhrase(), FIELDS[f]);		    	
		    		sterms = new String[c.getPhrase().getTerms().length];
		    		for (int j=0; j<sterms.length; j++) {
		    			sterms[j]=c.getPhrase().getTerms()[j].toString();
		    		}
		    	}
		    	else {
		    		sterms = new String[1];
		    		sterms[0]=c.getTerm().toString();
		    	}

		    	for (int j=0; j<sterms.length; j++) {		
		    		BooleanQuery out = new BooleanQuery();
		    		for (int f=0; f<FIELDS.length; f++) {		    				            		    	
		    			out.add(new TermQuery(new Term(FIELDS[f], sterms[j])), BooleanClause.Occur.SHOULD);
		    		}
		    		output.add(out, (c.isProhibited()
				              ? BooleanClause.Occur.MUST_NOT
				              : (c.isRequired()
				                  ? BooleanClause.Occur.MUST
				                  : BooleanClause.Occur.SHOULD
				                )));
		    	}		    	
		    }		    
	  }
	 
}
