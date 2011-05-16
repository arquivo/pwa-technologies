package org.archive.nutch.trec;

import org.apache.lucene.document.*;
import org.apache.lucene.queryParser.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.store.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.rankers.IRankingFunction;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.indexer.NutchSimilarity;
import org.apache.nutch.parse.ParseData;
import org.apache.hadoop.io.LongWritable;

/**
 * Validates if the docno in qrels files are indexed
 * @author Miguel Costa
 */
public class TRECDocValidator {
	
	  /**
	   * Main
	   * @param args
	   * @throws Exception
	   */
	  public static void main(String[] args) throws Exception {
		    String usage = "org.archive.nutch.trec.TRECDocValidator <qrels file> <index dir>";
            
		    if (args.length < 2) {
		      System.err.println(usage);
		      System.exit(-1);
		    }

		    // Configuration conf = NutchConfiguration.create();
		    //NutchBean bean = new NutchBean(conf);		    		  
		    
		    try {
		    	//RAMDirectory idx = new RAMDirectory(args[1]);
		    	Directory idx = FSDirectory.getDirectory(args[1], false);
		    	org.apache.lucene.search.Searcher searcher = new IndexSearcher(idx);
		    	org.apache.lucene.index.IndexReader reader=IndexReader.open(idx);
		    			    	
		    	ArquivoWebFunctionsWritable functions=new ArquivoWebFunctionsWritable();
				for (int i=0;i<IRankingFunction.functions.length;i++) { 
					functions.addFunction(i,1);
				}
			       	
		        BufferedReader in = new BufferedReader(new FileReader(args[0]));
		        String str;
		        String splitStr[]=null;
		        String qid=null;
		        String qidOld=null;
		        String docno=null;
		        String srel=null;		        
		        int totRel0=0, totRel1=0, totRel2=0;
		        int existRel0=0, existRel1=0, existRel2=0;
		        int allTotRel0=0, allTotRel1=0, allTotRel2=0;
		        int allExistRel0=0, allExistRel1=0, allExistRel2=0;
		        boolean firstTime=true;
		        while ((str = in.readLine()) != null) {
		        	
		        	qidOld=qid;
		        	splitStr=str.split(" ");
		        	qid = splitStr[0];
		        	docno = splitStr[2];
		        	srel = splitStr[3];
		        	
		        	if (!qid.equals(qidOld) && !firstTime) {		
		        		System.out.println("qid:"+qidOld+" 0:["+existRel0+" of "+totRel0+"] 1:["+existRel1+" of "+totRel1+"] 2:["+existRel2+" of "+totRel2+"]");
		        		totRel0=0;
		        		totRel1=0;
		        		totRel2=0;
		        		existRel0=0;
		        		existRel1=0;
		        		existRel2=0;
		        	}
		        	firstTime=false;
		        	
		        	if (srel.equals("0")) {
		        		totRel0++;
		        		allTotRel0++;
		        	}
		        	else if (srel.equals("1")) {
		        		totRel1++;
		        		allTotRel1++;
		        	}
		        	else if (srel.equals("2")) {
		        		totRel2++;
		        		allTotRel2++;
		        	}
		        	else {
		        		throw new Exception("Unexpected rel:"+srel+" for DOCNO:"+docno);
		        	}
		        	
		        	
		        	//QueryParser qparser=new QueryParser("DOCNO",new StandardAnalyzer());
		  		  	//Query query = qparser.parse("DOCNO:"+docno);
		        	
		        	BooleanQuery query=new BooleanQuery();
					query.add(new TermQuery(new Term("DOCNO", ""+docno)),BooleanClause.Occur.MUST);
		        	
		        	//Query query=new TermQuery(new Term("DOCNO", ""+docno));
//		  		    org.apache.lucene.search.Hits hits = searcher.search(query);				  		    		  		   		  		 
		        	/*
		        	Query query = Query.parse("DOCNO:"+docno, conf);					    
					Hits hits = bean.search(query, 10);
					// Hits hits = bean.search(query, topDocsReturned, maxMatches, maxDups, "site", null, false, null); // attention: maxHitsPerDup must be higher than 0 (eg. 2)
					*/		  		    
//					int length = (int)Math.min(hits.length(), 10);	
					
		        	int length=0;
					ArquivoWebScorer scorer=new ArquivoWebScorer(query, searcher, reader, functions);
		  			if (scorer.next()) {
						length=1;
					}

					
					if (length==0) {
						if (srel.equals("1") || srel.equals("2")) {
							System.out.println("Missing DOCNO:"+docno);
						}
					}
					else {
						if (length>1) {
							throw new Exception("Unexpected length:"+length);
						}
						
						if (srel.equals("0")) {
							existRel0++;
							allExistRel0++;
			        	}
			        	else if (srel.equals("1")) {
			        		existRel1++;
			        		allExistRel1++;
			        	}
			        	else if (srel.equals("2")) {
			        		existRel2++;
			        		allExistRel2++;
			        	}
						
						//Hit[] show = hits.getHits(0, length);
						//HitDetails[] details = bean.getDetails(show);
					    /*
						for (int i = 0; i < length; i++) {														
							Document doc = hits.doc(i);
							System.out.println(doc.get("url"));
						}
						*/					    					 		       
					}					
		        }
		        in.close();
		        
		        System.out.println("qid:"+qid+" 0:["+existRel0+" of "+totRel0+"] 1:["+existRel1+" of "+totRel1+"] 2:["+existRel2+" of "+totRel2+"]");
		        System.out.println("TOTAL 0:["+allExistRel0+" of "+allTotRel0+"] 1:["+allExistRel1+" of "+allTotRel1+"] 2:["+allExistRel2+" of "+allTotRel2+"]");		          
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
