package org.apache.lucene.search.caches;


/**
 * Generic interface for caches
 * @author Miguel Costa
 */
public interface PwaICache {
	
	/**
	 * Get field name cached
	 * @return
	 */
	public String getFieldName();
	
	/**
	 * Get value from document
	 * @return
	 */
	public Object getValue(int doc);
	
}
