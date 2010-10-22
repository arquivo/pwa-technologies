package org.apache.lucene.search;

import java.io.IOException;


/**
 * Generic interface to get next document
 * @author Miguel Costa
 */
public interface PwaSearchableCommon {
	
	/**
	 * Get document id
	 * @return document id
	 */
	public int doc();
	
	/**
	 * Move to next document
	 * @return true if has more documents; false otherwise
	 */
	public boolean next() throws IOException;
	
	/**
	 * Indicates if has more documents
	 * @return true if has more documents; false otherwise
	 */
	public boolean hasDoc();

}
