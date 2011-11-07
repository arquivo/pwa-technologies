package org.apache.lucene.search.tests;

import org.apache.lucene.search.rankers.queryindependent.PwaLinInlinks;

import junit.framework.*;


/**
 * Test LinInlinks
 * @author Miguel Costa
 */
public class PwaLinInlinksTest extends TestCase {

	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}

	public void testScore() {				
		PwaLinInlinks ranker=new PwaLinInlinks(0);	
		System.out.println(ranker.score());
		ranker=new PwaLinInlinks(1);	
		System.out.println(ranker.score());
		ranker=new PwaLinInlinks(3);	
		System.out.println(ranker.score());
		ranker=new PwaLinInlinks(5);	
		System.out.println(ranker.score());
		ranker=new PwaLinInlinks(10);	
		System.out.println(ranker.score());
		ranker=new PwaLinInlinks(100);		
		System.out.println(ranker.score());
		ranker=new PwaLinInlinks(1000);	
		System.out.println(ranker.score());
		ranker=new PwaLinInlinks(10000);	
		System.out.println(ranker.score());
        assertTrue(true);		
	}

}
