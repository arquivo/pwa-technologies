package org.apache.lucene.search.caches;

import org.apache.lucene.index.IndexReader;

import java.io.IOException;


/**
 * Cache flag indicating if it is a new document version, using digest difference
 * @author Miguel Costa
 * @deprecated  this data is not used anymore in the user interface
 */
public class PwaDigestDiffCache extends PwaVersionsCache {

	private static String fieldName="digestDiff";	

		
	/**
	 * Constructor
	 * @param searchable documents stream
	 * @param reader index reader
	 * @throws IOException
	 */
	public PwaDigestDiffCache(IndexReader reader) throws IOException {	
		super(reader);			
	}
		
	/**
	 * Get field name cached
	 * @return field name cached
	 */
	public String getFieldName() {
		return fieldName;
	}	
	
	/**
	 * Indicates if it is a new document version
	 * @param doc document id
	 * @return
	 */
	public Object getValue(int doc) { 	
		return Boolean.valueOf(newVersion.get(doc));
	}
			
}
