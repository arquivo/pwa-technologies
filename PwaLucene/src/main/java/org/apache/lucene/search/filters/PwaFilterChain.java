package org.apache.lucene.search.filters;

import java.io.IOException;
import java.util.Vector;

import org.apache.lucene.search.PwaSearchableCommon;


/**
 * Chain of filters
 * @author Miguel Costa
 */
public class PwaFilterChain implements PwaSearchableCommon {

	/** first filter of the chain or source */
	private PwaSearchableCommon firstFilter=null;	
	
		
	/**
	 * Constructor
	 */
	public PwaFilterChain(Vector<PwaFilter> filters, PwaSearchableCommon searchable) {
		if (filters.size()==0) {
			firstFilter=searchable;
			return;
		}
		
		filters.get(0).setSource(searchable);
		firstFilter=filters.get(0);				
		
		for (int i=1;i<filters.size();i++) {			
			filters.get(i).setSource(firstFilter);
			firstFilter=filters.get(i);	
		}
	}		
	
	/**
	 * Get document id
	 * @return document id
	 */
	public int doc() {
		return firstFilter.doc();
	}
	
	/**
	 * Move to next document
	 * @return true if has more documents; false otherwise
	 */
	public boolean next() throws IOException {
		return firstFilter.next();
	}
	
	/**
	 * Indicates if has more document
	 * @return true if has more documents; false otherwise
	 */
	public boolean hasDoc() {
		return firstFilter.hasDoc();
	}
}
