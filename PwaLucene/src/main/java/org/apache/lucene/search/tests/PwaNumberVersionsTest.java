package org.apache.lucene.search.tests;

import org.apache.lucene.search.features.PwaIRankingFunction;
import org.apache.lucene.search.features.temporal.PwaNumberVersions;

import junit.framework.*;


/**
 * Test SpanVersions
 * @author Miguel Costa
 */
public class PwaNumberVersionsTest extends TestCase {

	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}

	public void testScore() {	
		long nVersions=100;		
		long maxVersions=1000;		
		PwaNumberVersions ranker=new PwaNumberVersions(nVersions, maxVersions);			
		System.out.println(""+ranker.score());
		assertEquals(ranker.score(),(float)2/(float)3);
			
		nVersions=1000;		
		maxVersions=1000;		
		ranker=new PwaNumberVersions(nVersions, maxVersions);			
		System.out.println(""+ranker.score());
		assertEquals(ranker.score(),(float)3/(float)3);
		
	}	
}
