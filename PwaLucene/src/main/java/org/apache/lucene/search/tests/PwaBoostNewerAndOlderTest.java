package org.apache.lucene.search.tests;

import org.apache.lucene.search.features.temporal.PwaBoostNewerAndOlder;

import junit.framework.*;


/**
 * Test BoostNewer
 * @author Miguel Costa
 */
public class PwaBoostNewerAndOlderTest extends TestCase {

	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}

	public void testScore() {								
		long maxTimestamp=100;
		long minTimestamp=0;	
		
		for (long i=0;i<10;i++) {
			PwaBoostNewerAndOlder ranker=new PwaBoostNewerAndOlder(i,maxTimestamp,minTimestamp);
			System.out.println(i+" "+ranker.score());	
		}
		for (long i=10;i<=90;i+=10) {
			PwaBoostNewerAndOlder ranker=new PwaBoostNewerAndOlder(i,maxTimestamp,minTimestamp);
			System.out.println(i+" "+ranker.score());	
		}
		for (long i=91;i<=100;i++) {
			PwaBoostNewerAndOlder ranker=new PwaBoostNewerAndOlder(i,maxTimestamp,minTimestamp);
			System.out.println(i+" "+ranker.score());	
		}	
		
		assertTrue(true);
	}

}
