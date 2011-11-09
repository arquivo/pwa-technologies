package org.apache.lucene.search.tests;

import org.apache.lucene.search.rankers.*;
import org.apache.lucene.search.rankers.temporal.PwaBoostOlder;

import junit.framework.*;


/**
 * Test BoostNewer
 * @author Miguel Costa
 */
public class PwaBoostOlderTest extends TestCase {

	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}

	public void testScore() {						
		double maxSpan=10000;
		for (double i=0;i<10;i++) {
			PwaBoostOlder ranker=new PwaBoostOlder(i,maxSpan);
			System.out.println(i+" "+ranker.score());	
		}
		for (double i=10;i<100;i+=10) {
			PwaBoostOlder ranker=new PwaBoostOlder(i,maxSpan);
			System.out.println(i+" "+ranker.score());	
		}
		
		assertTrue(true);
	}

}
