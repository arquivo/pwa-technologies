package org.apache.nutch.searcher;
import org.apache.solr.common.SolrDocumentList;

public class ImageSearchResults {
	long numFound;
	int numShowing;
	long start;
	SolrDocumentList docs;
	
	public ImageSearchResults(long numFound,int numShowing, long start, SolrDocumentList docs){
		this.numFound = numFound;
		this.numShowing = numShowing;
		if( numFound < numShowing){ /*E.g. if numFound=0 than we are showing 0 images max*/
			this.numShowing = (int) numFound;
		}
		this.start = start;
		this.docs = docs;
	}
}
