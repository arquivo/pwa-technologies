package org.apache.lucene.search.tests;

import org.apache.lucene.search.features.PwaIRankingFunction;
import org.apache.lucene.search.features.temporal.PwaSpanVersions;

import junit.framework.*;


/**
 * Test SpanVersions
 * @author Miguel Costa
 */
public class PwaSpanVersionsTest extends TestCase {

	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}

	public void testScore() {	
		long maxTimestamp=100 * (long)PwaIRankingFunction.DAY_MILLISEC;
		long minTimestamp=0 * (long)PwaIRankingFunction.DAY_MILLISEC;;	
		long maxSpan=1000;
		
		PwaSpanVersions ranker=new PwaSpanVersions(maxTimestamp, minTimestamp, maxSpan);			
		System.out.println(""+ranker.score());
		assertEquals(ranker.score(),(float)2/(float)3);
			
		maxTimestamp=1000 * (long)PwaIRankingFunction.DAY_MILLISEC;
		minTimestamp=0 * (long)PwaIRankingFunction.DAY_MILLISEC;		
		ranker=new PwaSpanVersions(maxTimestamp, minTimestamp, maxSpan);			
		System.out.println(""+ranker.score());
		assertEquals(ranker.score(),(float)3/(float)3);
		

		maxTimestamp=10 * (long)PwaIRankingFunction.DAY_MILLISEC;
		minTimestamp=10 * (long)PwaIRankingFunction.DAY_MILLISEC;		
		ranker=new PwaSpanVersions(maxTimestamp, minTimestamp, maxSpan);			
		System.out.println(""+ranker.score());
		assertEquals(ranker.score(),(float)0);
		
		maxTimestamp=10 * (long)PwaIRankingFunction.DAY_MILLISEC;
		minTimestamp=10 * (long)PwaIRankingFunction.DAY_MILLISEC;	
		maxSpan=0;
		ranker=new PwaSpanVersions(maxTimestamp, minTimestamp, maxSpan);			
		System.out.println(""+ranker.score());
		assertEquals(ranker.score(),(float)0);
		
		maxSpan=1000;
		maxTimestamp=110 * (long)PwaIRankingFunction.DAY_MILLISEC;
		minTimestamp=10 * (long)PwaIRankingFunction.DAY_MILLISEC;		
		ranker=new PwaSpanVersions(maxTimestamp, minTimestamp, maxSpan);			
		System.out.println(""+ranker.score());
		assertEquals(ranker.score(),(float)2/(float)3);		
	}	
}
