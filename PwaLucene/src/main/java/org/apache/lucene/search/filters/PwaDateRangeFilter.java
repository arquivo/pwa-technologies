package org.apache.lucene.search.filters;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.PwaSearchableCommon;
import org.apache.lucene.search.caches.PwaDateCache;

import java.io.IOException;


/**
 * Filter that restricts search results to timestamp range.
 * @author Miguel Costa
 * 
 * @note handle BUG wayback 0000153 
 */
public class PwaDateRangeFilter extends PwaFilter {

	private long minTimestamp;
	private long maxTimestamp;
	private PwaDateCache cache;


	/**
	 * Constructor
	 * @param reader index reader
	 * @param minTimestamp the minimum timestamp      
	 * @param maxTimestamp the maximum timestamp
	 */	
	public PwaDateRangeFilter(IndexReader reader, String minTimestamp, String maxTimestamp) throws IOException {
		this(null,reader,minTimestamp,maxTimestamp);		
	}
	
	/**
	 * Constructor
	 * @param searchable documents stream 
	 * @param reader index reader
	 * @param minTimestamp the minimum timestamp      
	 * @param maxTimestamp the maximum timestamp    
	 */	
	public PwaDateRangeFilter(PwaSearchableCommon searchable, IndexReader reader, String minTimestamp, String maxTimestamp) throws IOException {
		super(searchable);
		this.cache = new PwaDateCache(reader);
		this.minTimestamp = Long.parseLong(minTimestamp)*1000;		      
		this.maxTimestamp = Long.parseLong(maxTimestamp)*1000;
	}

	/**
	 * Get document id	
	 * @return document id
	 */
	public int doc() {
		return searchable.doc();
	}

	/**
	 * Indicates if has more documents
	 * @return true if has more documents; false otherwise
	 */
	public boolean hasDoc() {
		return searchable.hasDoc();
	}
	
	/**
	 * Move to next document
	 * @return true if has more documents; false otherwise
	 */
	public boolean next() throws IOException {  
		
		while (searchable.next()) {
			if (cache.getTimestamp(searchable.doc())>=minTimestamp && cache.getTimestamp(searchable.doc())<=maxTimestamp) {
				return true;
			}
		}
		return false;
	}

}

