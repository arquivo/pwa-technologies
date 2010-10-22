package org.apache.lucene.search.filters;

import org.apache.lucene.search.PwaSearchableCommon;


/**
 * Results filter
 * @author Miguel Costa
 */
public abstract class PwaFilter implements PwaSearchableCommon {

	protected PwaSearchableCommon searchable;
	
	
	/**
	 * Constructor
	 * @param searchable the stream of documents to filter
	 */
	public PwaFilter(PwaSearchableCommon searchable) {	
		this.searchable=searchable;
	}
	
	/**
	 * Set the stream of documents to filter
	 * @param searchable the stream of documents to filter
	 */
	public void setSource(PwaSearchableCommon searchable) {	
		this.searchable=searchable;
	}
	
}
