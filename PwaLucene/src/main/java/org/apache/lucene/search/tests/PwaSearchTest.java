package org.apache.lucene.search.tests;

import org.apache.lucene.store.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;    
import org.apache.lucene.search.caches.PwaCacheManager;
import org.apache.lucene.search.caches.PwaIndexStats;
import org.apache.lucene.search.features.*;

import java.io.*;

import junit.framework.TestCase;


/**
 * Test search
 * @author Miguel Costa
 */
public class PwaSearchTest extends TestCase {
	
	private Directory idx;
	private org.apache.lucene.search.Searcher searcher;
	private org.apache.lucene.index.IndexReader reader;
	private PwaFunctionsWritable functions;
	private PwaCacheManager cache;
	
	
	protected void setUp() throws Exception {
		try {
			// Directory idx = FSDirectory.getDirectory("/data/arcs/outputsIA/index", false);
			idx = FSDirectory.getDirectory("/data/arcs/GOV1/outputs/index", false);			
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
		for (int i=0;i<30;i++) {
			functions.addFunction(i,1);
		}		
	}

	protected void tearDown() throws Exception {
		// close all
		searcher.close();
		reader.close();
	}
	

	/**
	 * Counting tests
	 */
	public void testSearchCounting() {
		
		long timeStart=System.currentTimeMillis();	
		try {																																								
			BooleanQuery queryx=doQuery(new String[]{"petroleum resources"},new boolean[]{false});			
			System.out.println("queryx:"+queryx);
			PwaScorer scorerx=new PwaScorer(queryx, searcher, reader, functions);
			int z=0;
			while (scorerx.next()) {
				z++;
				scorerx.doc();
				scorerx.score();
			}
			assertTrue(z>0);							
			
			queryx=doQuery(new String[]{"petroleum","resources"},new boolean[]{false,false});
			scorerx=new PwaScorer(queryx, searcher, reader, functions);
			z=0;
			while (scorerx.next()) {
				z++;
				scorerx.doc();
				scorerx.score();
			}
			assertTrue(z>0);										
		}
		catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		
		long timeEnd=System.currentTimeMillis();
		System.out.println("Time spent (millisec): "+(timeEnd-timeStart));
	}
	
	/**
	 * Deterministic tests
	 */
	public void testSearchDeterministic() {
		
		long timeStart=System.currentTimeMillis();
		try {																																												
			BooleanQuery query1=doQuery(new String[]{"nsa","usa"},new boolean[]{false,false});
			BooleanQuery query2=doQuery(new String[]{"nsa","usa"},new boolean[]{false,false});								
			PwaScorer scorer1=new PwaScorer(query1, searcher, reader, functions);
			PwaScorer scorer2=new PwaScorer(query2, searcher, reader, functions);
			int i=0;
			while (scorer1.next()) {
				if (!scorer2.next()) {
					fail("Different number of documents.");
				}
				assertTrue(scorer1.doc()==scorer2.doc());			
				i++;
			}	
		}
		catch (IOException e) {
			e.printStackTrace();
			fail();
		}
			
		long timeEnd=System.currentTimeMillis();
		System.out.println("Time spent (millisec): "+(timeEnd-timeStart));
	}
			
			
	/**
	 * Change order tests
	 */
	public void testSearchChangeOrder() {
		
		long timeStart=System.currentTimeMillis();
		try {
			BooleanQuery query1=doQuery(new String[]{"nsa","usa","college"},new boolean[]{false,false,false});
			BooleanQuery query2=doQuery(new String[]{"usa","college","nsa"},new boolean[]{false,false,false});								
			//int doc=-1;
			PwaScorer scorer1=new PwaScorer(query1, searcher, reader, functions);
			PwaScorer scorer2=new PwaScorer(query2, searcher, reader, functions);
			int i=0;
			while (scorer1.next()) {
				if (!scorer2.next()) {
					fail("Different number of documents.");
				}
				assertTrue(scorer1.doc()==scorer2.doc());				
				i++;
			}			
			
			query1=doQuery(new String[]{"nsa","usa","college"},new boolean[]{false,false,false});
			query2=doQuery(new String[]{"college","usa","nsa"},new boolean[]{false,false,false});								
			scorer1=new PwaScorer(query1, searcher, reader, functions);
			scorer2=new PwaScorer(query2, searcher, reader, functions);
			i=0;
			while (scorer1.next()) {
				if (!scorer2.next()) {
					fail("Different number of documents.");
				}
				assertTrue(scorer1.doc()==scorer2.doc());			
				i++;
			}			
						
			query1=doQuery(new String[]{"nsa","usa","college","gov"},new boolean[]{false,false,false,false});
			query2=doQuery(new String[]{"college","usa","gov","nsa"},new boolean[]{false,false,false,false});								
			scorer1=new PwaScorer(query1, searcher, reader, functions);
			scorer2=new PwaScorer(query2, searcher, reader, functions);
			i=0;
			while (scorer1.next()) {
				if (!scorer2.next()) {
					fail("Different number of documents.");
				}
				assertTrue(scorer1.doc()==scorer2.doc());			
				i++;
			}	
		}
		catch (IOException e) {
			e.printStackTrace();
			fail();
		}
			
		long timeEnd=System.currentTimeMillis();
		System.out.println("Time spent (millisec): "+(timeEnd-timeStart));
	}
			
	/**
	 * Exclude tests
	 */
	public void testSearchExclude() {
		
		long timeStart=System.currentTimeMillis();
		try {				
			BooleanQuery query1=doQuery(new String[]{"nsa","nsa"},new boolean[]{false,true});										
			PwaScorer scorer1=new PwaScorer(query1, searcher, reader, functions);			
			assertFalse(scorer1.next()); // must return empty
			
			query1=doQuery(new String[]{"nsa","usa","nsa"},new boolean[]{false,false,true});										
			scorer1=new PwaScorer(query1, searcher, reader, functions);
			assertFalse(scorer1.next()); // must return empty
			
			query1=doQuery(new String[]{"nsa","usa","usa"},new boolean[]{false,false,true});										
			scorer1=new PwaScorer(query1, searcher, reader, functions);
			assertFalse(scorer1.next()); // must return empty
			
			query1=doQuery(new String[]{"college","gov","college","gov"},new boolean[]{false,false,true,true});										
			scorer1=new PwaScorer(query1, searcher, reader, functions);
			assertFalse(scorer1.next()); // must return empty
			
			query1=doQuery(new String[]{"college","gov","college","gov"},new boolean[]{true,true,false,false});										
			scorer1=new PwaScorer(query1, searcher, reader, functions);
			assertFalse(scorer1.next()); // must return empty
			
			query1=doQuery(new String[]{"college","gov","college","gov"},new boolean[]{true,true,false,false});
			TermQuery termQuery=new TermQuery(new Term("type","html"));			
			query1.add(termQuery, BooleanClause.Occur.MUST);
			scorer1=new PwaScorer(query1, searcher, reader, functions);
			assertFalse(scorer1.next()); // must return empty
			
			query1=doQuery(new String[]{"college","gov","college","gov"},new boolean[]{true,true,false,false});
			termQuery=new TermQuery(new Term("type","html"));			
			query1.add(termQuery, BooleanClause.Occur.MUST);
			termQuery=new TermQuery(new Term("type","html"));			
			query1.add(termQuery, BooleanClause.Occur.MUST_NOT);
			scorer1=new PwaScorer(query1, searcher, reader, functions);
			assertFalse(scorer1.next()); // must return empty
			
			query1=doQuery(new String[]{"college"},new boolean[]{true});
			termQuery=new TermQuery(new Term("type","html"));			
			query1.add(termQuery, BooleanClause.Occur.MUST);
			termQuery=new TermQuery(new Term("type","html"));			
			query1.add(termQuery, BooleanClause.Occur.MUST_NOT);
			scorer1=new PwaScorer(query1, searcher, reader, functions);
			assertFalse(scorer1.next()); // must return empty
		}
		catch (IOException e) {
			e.printStackTrace();
			fail();
		}
			
		long timeEnd=System.currentTimeMillis();
		System.out.println("Time spent (millisec): "+(timeEnd-timeStart));
	}
	
	/**
	 * Phrase tests
	 */
	public void testSearchPhrase() {
		
		long timeStart=System.currentTimeMillis();
		try {					
			BooleanQuery query1=doQuery(new String[]{"petroleum resources"},new boolean[]{false});
			PwaScorer scorer1=new PwaScorer(query1, searcher, reader, functions);
			assertTrue(scorer1.next());
			
			query1=doQuery(new String[]{"resources xpto"},new boolean[]{false});
			scorer1=new PwaScorer(query1, searcher, reader, functions);
			assertFalse(scorer1.next()); // must return empty
			
			query1=doQuery(new String[]{"resources of xpto"},new boolean[]{false});
			scorer1=new PwaScorer(query1, searcher, reader, functions);
			assertFalse(scorer1.next()); // must return empty
			
			query1=doQuery(new String[]{"ireland consular information sheet"},new boolean[]{false});
			scorer1=new PwaScorer(query1, searcher, reader, functions);
			assertTrue(scorer1.next()); 					
			
			query1=doQuery(new String[]{"ireland consular information sheet","ireland consular information sheet"},new boolean[]{false,true});
			scorer1=new PwaScorer(query1, searcher, reader, functions);
			assertFalse(scorer1.next()); // must return empty
		}
		catch (IOException e) {
			e.printStackTrace();
			fail();
		}
			
		long timeEnd=System.currentTimeMillis();
		System.out.println("Time spent (millisec): "+(timeEnd-timeStart));
	}
	
	/**
	 * Query plan generator tests
	 */
	public void testSearchQueryPlanGenerator() {
		
		long timeStart=System.currentTimeMillis();
		try {												
			String A="college";
			String B="gov";
			String C="nsa";
			
			// test if A = A&B + A\B
			BooleanQuery query1=doQuery(new String[]{A},new boolean[]{false});										
			PwaScorer scorer1=new PwaScorer(query1, searcher, reader, functions);
			int i=0;
			while (scorer1.next()) {
				i++;
			}
			BooleanQuery query2=doQuery(new String[]{A,B},new boolean[]{false,false});										
			PwaScorer scorer2=new PwaScorer(query2, searcher, reader, functions);
			int j=0;
			while (scorer2.next()) {
				j++;
			}			
			BooleanQuery query3=doQuery(new String[]{A,B},new boolean[]{false,true});										
			PwaScorer scorer3=new PwaScorer(query3, searcher, reader, functions);
			int k=0;
			while (scorer3.next()) {
				k++;
			}			
			assertTrue(i==j+k);
						
			// test if A = A&B + A&C - A&B&C + A\B\C
			query1=doQuery(new String[]{A},new boolean[]{false});										
			scorer1=new PwaScorer(query1, searcher, reader, functions);
			i=0;
			while (scorer1.next()) {
				i++;
			}
			query2=doQuery(new String[]{A,B},new boolean[]{false,false});										
			scorer2=new PwaScorer(query2, searcher, reader, functions);
			j=0;
			while (scorer2.next()) {
				j++;
			}
			query3=doQuery(new String[]{A,C},new boolean[]{false,false});										
			scorer3=new PwaScorer(query3, searcher, reader, functions);
			k=0;
			while (scorer3.next()) {
				k++;
			}						
			BooleanQuery query4=doQuery(new String[]{A,B,C},new boolean[]{false,false,false});										
			PwaScorer scorer4=new PwaScorer(query4, searcher, reader, functions);
			int l=0;
			while (scorer4.next()) {
				l++;
			}			
			BooleanQuery query5=doQuery(new String[]{A,B,C},new boolean[]{false,true,true});										
			PwaScorer scorer5=new PwaScorer(query5, searcher, reader, functions);
			int m=0;
			while (scorer5.next()) {
				m++;
			}						
			assertTrue(i==j+k-l+m);
					
			
			// test if A = A&B + A&C - A&B&C + A\B\C - with one extra field
			query1=doQuery(new String[]{A},new boolean[]{false});										
			scorer1=new PwaScorer(query1, searcher, reader, functions);
			i=0;
			while (scorer1.next()) {
				i++;
			}
			query2=doQuery(new String[]{A,B},new boolean[]{false,false});										
			scorer2=new PwaScorer(query2, searcher, reader, functions);
			j=0;
			while (scorer2.next()) {
				j++;
			}
			query3=doQuery(new String[]{A},new boolean[]{false});		
			TermQuery termQuery=new TermQuery(new Term("type","html"));			
			query3.add(termQuery, BooleanClause.Occur.MUST);
			scorer3=new PwaScorer(query3, searcher, reader, functions);
			k=0;
			while (scorer3.next()) {
				k++;
			}						
			query4=doQuery(new String[]{A,B},new boolean[]{false,false});
			termQuery=new TermQuery(new Term("type","html"));			
			query4.add(termQuery, BooleanClause.Occur.MUST);
			scorer4=new PwaScorer(query4, searcher, reader, functions);
			l=0;
			while (scorer4.next()) {
				l++;
			}			
			query5=doQuery(new String[]{A,B},new boolean[]{false,true});
			termQuery=new TermQuery(new Term("type","html"));			
			query5.add(termQuery, BooleanClause.Occur.MUST_NOT);
			scorer5=new PwaScorer(query5, searcher, reader, functions);
			m=0;
			while (scorer5.next()) {
				m++;
			}						
			assertTrue(i==j+k-l+m);
			
			
			// test if A = A&B + A&C - A&B&C + A\B\C - with two extra fields - the fields must be different because if not, they are aggregated 
			query1=doQuery(new String[]{A},new boolean[]{false});										
			scorer1=new PwaScorer(query1, searcher, reader, functions);
			i=0;
			while (scorer1.next()) {
				i++;
			}
			query2=doQuery(new String[]{A},new boolean[]{false});	
			termQuery=new TermQuery(new Term("type","html"));			
			query2.add(termQuery, BooleanClause.Occur.MUST);
			scorer2=new PwaScorer(query2, searcher, reader, functions);
			j=0;
			while (scorer2.next()) {
				j++;
			}
			query3=doQuery(new String[]{A},new boolean[]{false});		
			termQuery=new TermQuery(new Term("site","www.epa.gov"));		
			query3.add(termQuery, BooleanClause.Occur.MUST);
			scorer3=new PwaScorer(query3, searcher, reader, functions);
			k=0;
			while (scorer3.next()) {
				k++;
			}						
			query4=doQuery(new String[]{A},new boolean[]{false});
			termQuery=new TermQuery(new Term("type","html"));			
			query4.add(termQuery, BooleanClause.Occur.MUST);
			termQuery=new TermQuery(new Term("site","www.epa.gov"));			
			query4.add(termQuery, BooleanClause.Occur.MUST);
			scorer4=new PwaScorer(query4, searcher, reader, functions);
			l=0;
			while (scorer4.next()) {
				l++;
			}			
			query5=doQuery(new String[]{A},new boolean[]{false});
			termQuery=new TermQuery(new Term("type","html"));			
			query5.add(termQuery, BooleanClause.Occur.MUST_NOT);
			termQuery=new TermQuery(new Term("site","www.epa.gov"));		
			query5.add(termQuery, BooleanClause.Occur.MUST_NOT);
			scorer5=new PwaScorer(query5, searcher, reader, functions);
			m=0;
			while (scorer5.next()) {
				m++;
			}					
			assertTrue(i==j+k-l+m);																			
		}
		catch (IOException e) {
			e.printStackTrace();
			fail();
		}
		
		long timeEnd=System.currentTimeMillis();
		System.out.println("Time spent (millisec): "+(timeEnd-timeStart));
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
