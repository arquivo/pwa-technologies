package org.apache.lucene.search.tests;

import org.apache.lucene.store.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;    
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.search.caches.PwaCacheManager;
import org.apache.lucene.search.caches.PwaIndexStats;
import org.apache.lucene.search.rankers.*;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

import junit.framework.TestCase;


/**
 * Get the distribution of the timestamps of documents matching a query
 * @author Miguel Costa
 */
public class PwaDateDistributionSearchTest extends TestCase {
	
	private Directory idx;
	private org.apache.lucene.search.Searcher searcher;
	private org.apache.lucene.index.IndexReader reader;
	private PwaFunctionsWritable functions;
	private PwaCacheManager cache;
	
	
	protected void setUp() throws Exception {
		try {
			idx = FSDirectory.getDirectory("/data/arcs/outputsIA/index", false);
			//idx = FSDirectory.getDirectory("/data/arcs/GOV1/outputs/index", false);			
			searcher = new IndexSearcher(idx);
			reader=IndexReader.open(idx);
			cache=PwaCacheManager.getInstance(reader); // start all caches
		}
		catch (IOException e) {
			e.printStackTrace();
			throw new IOException(e);
		}
					
		// ranking functions
		functions=new PwaFunctionsWritable();
		functions.addFunction(10, 0.023322608715538286f);
		functions.addFunction(24, 0.59394816155096997f);
		functions.addFunction(12, 0.34503713094903127f);
		functions.addFunction(20, 1.2592823789368244f);			
	}

	protected void tearDown() throws Exception {
		// close all
		searcher.close();
		reader.close();
	}
	

	/**
	 * Counting tests
	 */
	public void testSearchDistribution() {
		int max=10000;
		Document doc=null;
		Date d=null;
		String day=null;
		Integer times=null;
		HashMap<String,Integer> daysMap=new HashMap<String,Integer>();					
		
		try {																																																			
			BooleanQuery queryx=doQuery(new String[]{"cavaco","silva"},new boolean[]{false,false});
			PwaScorer scorerx=new PwaScorer(queryx, searcher, reader, functions);
			int i=0;
			while (i<max && scorerx.next()) {										
				day=(String)cache.getValue("tstamp", scorerx.doc());
				day=day.substring(0,8);
				times=daysMap.get(day);
				if (times==null) {
					times=new Integer(1);
				}
				else {
					times++;
				}
				daysMap.put(day,times);
									
				i++;
			}
			assertTrue(true);										
		}
		catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		
		TreeMap<String,Integer> treeMap = new TreeMap<String,Integer>(daysMap); // sort entries by key				
		for(Map.Entry<String,Integer> entry : treeMap.entrySet()) {
			System.out.println(entry.getKey()+" "+entry.getValue());			
		}	
	}
		
	/**
	 * Create query
	 * @param termsQuery query terms
	 * @param exclude flag indicating if it is to exclude the term
	 * @return
	 */
	private BooleanQuery doQuery(String termsQuery[], boolean exclude[]) {
		BooleanQuery query=new BooleanQuery();
		BooleanQuery out=null;
		for (int i=0;i<termsQuery.length;i++) {
			out=new BooleanQuery();		
			for (int f=0; f<PwaIndexStats.FIELDS.length; f++) {	
				
				String ssplit[]=termsQuery[i].split(" ");
				if (ssplit.length>1) { // is phrase
					PhraseQuery exactPhrase = new PhraseQuery();
					for (int k=0; k<ssplit.length; k++) {
					  exactPhrase.add(new Term(PwaIndexStats.FIELDS[f], ssplit[k]));
					}
					out.add(exactPhrase, BooleanClause.Occur.SHOULD);
				}
				else { // is term
					TermQuery termQuery=new TermQuery(new Term(PwaIndexStats.FIELDS[f], termsQuery[i]));
					out.add(termQuery, BooleanClause.Occur.SHOULD);				
				}				  				  					
			}
			if (exclude[i]) {
				query.add(out, BooleanClause.Occur.MUST_NOT);
			}
			else {
				query.add(out, BooleanClause.Occur.MUST);
			}					
		}										
	
		return query;
	}		
}
