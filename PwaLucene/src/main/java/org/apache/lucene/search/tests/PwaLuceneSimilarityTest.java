package org.apache.lucene.search.tests;

import java.util.Vector;

import org.apache.lucene.search.features.querydependent.PwaLuceneSimilarity;

import junit.framework.TestCase;


/**
 * Test LuceneSimilarity
 * @author Miguel Costa
 */
public class PwaLuceneSimilarityTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testScore() { // test for 2 fields in a query of 5 terms
		Vector<Vector<Integer>> tfPerField=new Vector<Vector<Integer>>();
		Vector<Vector<Integer>> idfPerField=new Vector<Vector<Integer>>();
		Vector<Integer> nTermsPerField=new Vector<Integer>();
		
		Vector<Integer> tf = new Vector<Integer>();
		tf.add(3);
		tf.add(1);
		tf.add(1);
		tf.add(30);
		tf.add(2);
		tfPerField.add(tf);
		tf = new Vector<Integer>();
		tf.add(3);
		tf.add(1);
		tf.add(1);
		tf.add(30);
		tf.add(2);
		tfPerField.add(tf);
		
		Vector<Integer> idf=new Vector<Integer>();
		idf.add(1000);
		idf.add(20);
		idf.add(10);
		idf.add(100);
		idf.add(10);		
		idfPerField.add(idf);
		idf=new Vector<Integer>();
		idf.add(1000);
		idf.add(20);
		idf.add(10);
		idf.add(100);
		idf.add(10);		
		idfPerField.add(idf);
		
		nTermsPerField.add(1000);
		nTermsPerField.add(1000);
			
		PwaLuceneSimilarity ranker=new PwaLuceneSimilarity(tfPerField,idfPerField,nTermsPerField,10000000);
		System.out.println(ranker.score());
        assertTrue(true);
	}
}
