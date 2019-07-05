package org.apache.lucene.search.tests;

import java.net.MalformedURLException;

import org.apache.lucene.search.features.queryindependent.PwaUrlDepth;

import junit.framework.*;


/**
 * Test UrlDepth
 * @author Miguel Costa
 */
public class PwaUrlDepthTest extends TestCase {

	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}

	public void testScore() {			
		PwaUrlDepth ranker;
		try {
			ranker = new PwaUrlDepth("http://www.sigir.org");
			assertEquals(3,(int)ranker.score());
			ranker=new PwaUrlDepth("http://www.sigir.org/");
			assertEquals(3,(int)ranker.score());
			ranker=new PwaUrlDepth("http://www.sigir");
			assertEquals(3,(int)ranker.score());
			ranker=new PwaUrlDepth("http://www.sigir.org/sigirlist");			
			assertEquals(2,(int)ranker.score());
			ranker=new PwaUrlDepth("http://www.sigir.org/sigirlist/");
			assertEquals(2,(int)ranker.score());			
			ranker=new PwaUrlDepth("http://www.sigir.org/sigirlist/issues");
			assertEquals(1,(int)ranker.score());
			ranker=new PwaUrlDepth("http://www.sigir.org/sigirlist/issues/");
			assertEquals(1,(int)ranker.score());
			ranker=new PwaUrlDepth("http://www.sigir.org/sigirlist/issues/otherdir");
			assertEquals(1,(int)ranker.score());
			ranker=new PwaUrlDepth("http://www.sigir.org/sigirlist/issues/otherdir/another/");
			assertEquals(1,(int)ranker.score());			
			ranker=new PwaUrlDepth("http://www.sigir.org/sigirlist/issues/otherdir/another/file.htm");
			assertEquals(0,(int)ranker.score());
			ranker=new PwaUrlDepth("http://www.sigir.org/sigirlist/file.");
			assertEquals(0,(int)ranker.score());
			ranker=new PwaUrlDepth("http://www.sigir.org/file.gif");
			assertEquals(0,(int)ranker.score());		
		} 
		catch (MalformedURLException e) {
			fail();
		}				
	}

}
