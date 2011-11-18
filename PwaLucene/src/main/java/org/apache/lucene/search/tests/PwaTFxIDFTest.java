package org.apache.lucene.search.tests;

import java.util.Vector;

import org.apache.lucene.search.features.querydependent.PwaTFxIDF;

import junit.framework.*;


/**
 * Test TFxIDF
 * @author Miguel Costa
 */
public class PwaTFxIDFTest extends TestCase {

	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}

	public void testScore() {
		Vector<Integer> tf=new Vector<Integer>();
		tf.add(3);
		
		Vector<Integer> idf=new Vector<Integer>();
		idf.add(1000);
			
		PwaTFxIDF ranker=new PwaTFxIDF(tf,idf,100,10000000);	
		System.out.println(ranker.score());
        assertTrue(ranker.score()==0.2763102111592855);		              
	}

}
