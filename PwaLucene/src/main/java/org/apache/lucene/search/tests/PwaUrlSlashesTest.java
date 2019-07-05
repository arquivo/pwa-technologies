package org.apache.lucene.search.tests;

import java.net.MalformedURLException;

import org.apache.lucene.search.features.queryindependent.PwaUrlSlashes;

import junit.framework.*;


/**
 * Test UrlDepth
 * @author Miguel Costa
 */
public class PwaUrlSlashesTest extends TestCase {

	protected void setUp() throws Exception {
	}

	protected void tearDown() throws Exception {
	}

	public void testScore() {			
		PwaUrlSlashes ranker;
		try {
			ranker = new PwaUrlSlashes("http://www.sigir.org");
			assertEquals(0,(int)ranker.score());
			ranker=new PwaUrlSlashes("http://www.sigir.org/");
			assertEquals(1,(int)ranker.score());
			ranker=new PwaUrlSlashes("www.sigir");
			assertEquals(0,(int)ranker.score());								
			ranker=new PwaUrlSlashes("http://www.sigir.org/sigirlist/");
			assertEquals(2,(int)ranker.score());			
			ranker=new PwaUrlSlashes("http://www.sigir.org/sigirlist/issues/otherdir/another/file.htm");
			assertEquals(5,(int)ranker.score());
		} 
		catch (MalformedURLException e) {
			fail();
		}				
	}

}
