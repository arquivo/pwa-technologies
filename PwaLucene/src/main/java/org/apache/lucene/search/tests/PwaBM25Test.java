package org.apache.lucene.search.tests;

import java.util.Vector;

import org.apache.lucene.search.features.querydependent.PwaBM25;

import junit.framework.TestCase;

/**
 * Test BM25
 * @author Miguel Costa
 */
public class PwaBM25Test extends TestCase {

	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}

	public void testScore() {
		Vector<Integer> tf=new Vector<Integer>();
		tf.add(3);
		
		Vector<Integer> idf=new Vector<Integer>();
		idf.add(1000);
			
		PwaBM25 ranker=new PwaBM25(tf,idf,100,1000.0,10000000);
		System.out.println(ranker.score());
        assertTrue(ranker.score()==9.862371362773622);
	}

}
