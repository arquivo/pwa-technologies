package org.apache.lucene.search.filters;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.PwaSearchableCommon;
import org.apache.lucene.search.caches.PwaDateCache;

import java.io.IOException;


/**
 * Filter that restricts search results to the document with the closest timestamp.
 * @author Miguel Costa
 *  
 * @note handle BUG wayback 0000153 
 */
public class PwaDateClosestFilter extends PwaFilter {

	private long timestamp; // timestamp searched
	private int docClosest;
	private PwaDateCache cache;


	/**
	 * Constructor
	 * @param timestamp timestamp      
	 * @param reader index reader
	 */	
	public PwaDateClosestFilter(IndexReader reader, String timestamp) throws IOException {
		this(null,reader,timestamp);		
	}
	
	/**
	 * Constructor
	 * @param searchable documents stream
	 * @param timestamp timestamp      
	 * @param reader index reader
	 */	
	public PwaDateClosestFilter(PwaSearchableCommon searchable, IndexReader reader, String timestamp) throws IOException {
		super(searchable);
		this.cache = new PwaDateCache(reader);
		this.timestamp = Long.parseLong(timestamp)*1000;		      
		this.docClosest = -1;
	}

	/**
	 * Get document id	
	 */
	public int doc() {
		return docClosest;
	}

	/**
	 * Indicates if has more documents
	 * @return
	 */
	public boolean hasDoc() {
		return docClosest>=0;
	}
	
	/**
	 * Move to next document
	 */
	public boolean next() throws IOException {  
		
		if (docClosest!=-1) { // only one document is returned
			docClosest=-2;
			return false;
		}
		
		long minDiff=Long.MAX_VALUE;
		while (searchable.next()) {
			Long mindDiff_aux = Math.abs(cache.getTimestamp(searchable.doc())-timestamp);
			if (docClosest==-1 || mindDiff_aux<minDiff) {
				docClosest=searchable.doc();
				minDiff=mindDiff_aux;
			}		
		}	
		
		if (docClosest==-1) { // when no documents exist
			docClosest=-2;
			return false;
		}
		return true;
	}
	
}

