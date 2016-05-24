package org.apache.lucene.search.tests;

import java.util.Vector;

import org.apache.lucene.search.features.querydependent.PwaMinSpan;

import junit.framework.*;


/**
 * Test MinSpan
 * @author Miguel Costa
 */
public class PwaMinSpanTest extends TestCase {

	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}

	public void testScore() {				
		PwaMinSpan ranker=new PwaMinSpan(0);	
		System.out.println(ranker.score());
		ranker=new PwaMinSpan(1);	
		System.out.println(ranker.score());
		ranker=new PwaMinSpan(2);	
		System.out.println(ranker.score());
		ranker=new PwaMinSpan(3);	
		System.out.println(ranker.score());
		ranker=new PwaMinSpan(4);	
		System.out.println(ranker.score());
		ranker=new PwaMinSpan(5);	
		System.out.println(ranker.score());
		ranker=new PwaMinSpan(6);	
		System.out.println(ranker.score());
		ranker=new PwaMinSpan(8);	
		System.out.println(ranker.score());
		ranker=new PwaMinSpan(10);	
		System.out.println(ranker.score());
		assertTrue(true);
	}

}
