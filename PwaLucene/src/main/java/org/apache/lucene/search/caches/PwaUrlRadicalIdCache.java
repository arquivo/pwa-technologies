package org.apache.lucene.search.caches;

import org.apache.lucene.index.IndexReader;

import java.io.IOException;


/**
 * Caches url radical id, indicating the version group
 * @author Miguel Costa
 * @deprecated  this data is not used anymore. Now, the Broker filters the excess of versions.  
 */
public class PwaUrlRadicalIdCache extends PwaVersionsCache {

	private static String fieldName="radicalId";	

		
	/**
	 * Constructor
	 * @param searchable
	 * @param reader
	 * @throws IOException
	 */
	public PwaUrlRadicalIdCache(IndexReader reader) throws IOException {	
		super(reader);			
	}
		
	/**
	 * Get field name cached
	 * @return
	 */
	public String getFieldName() {
		return fieldName;
	}	
	
	/**
	 * Indicates if it is a new document
	 * @param doc document id
	 * @return
	 */
	public Object getValue(int doc) { 	
		return Long.valueOf(urlRadicalId[doc]);
	}
			
}
