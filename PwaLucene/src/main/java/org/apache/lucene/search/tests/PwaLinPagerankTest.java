package org.apache.lucene.search.tests;

import org.apache.lucene.search.features.queryindependent.PwaLinPagerank;

import junit.framework.*;


/**
 * Test LinPagerank
 * @author Miguel Costa
 */
public class PwaLinPagerankTest extends TestCase {

	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}

	public void testScore() {			
		PwaLinPagerank ranker=new PwaLinPagerank(0);	
		System.out.println(ranker.score());
		ranker=new PwaLinPagerank(0.00001f);	
		System.out.println(ranker.score());
		ranker=new PwaLinPagerank(0.0001f);	
		System.out.println(ranker.score());
		ranker=new PwaLinPagerank(0.001f);		
		System.out.println(ranker.score());
		ranker=new PwaLinPagerank(0.01f);	
		System.out.println(ranker.score());
		ranker=new PwaLinPagerank(0.1f);	
		System.out.println(ranker.score());
		ranker=new PwaLinPagerank(1f);	
		System.out.println(ranker.score());
		ranker=new PwaLinPagerank(1.5f);	
		System.out.println(ranker.score());
		ranker=new PwaLinPagerank(2f);	
		System.out.println(ranker.score());
        assertTrue(true);		
	}

}
