package org.apache.lucene.search.filters;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.PwaSearchableCommon;
import org.apache.lucene.search.caches.PwaBlacklistCache;


/**
 * Filter based on blacklist
 * @author Miguel Costa
 */
public class PwaBlacklistFilter extends PwaFilter {
	
	private PwaBlacklistCache cache;


	/**
	 * Constructor  
	 * @param reader index reader
	 */	
	public PwaBlacklistFilter(IndexReader reader) throws IOException {
		this(null,reader);		
	}
	
	/**
	 * Constructor
	 * @param searchable documents stream
	 * @param reader index reader
	 */	
	public PwaBlacklistFilter(PwaSearchableCommon searchable, IndexReader reader) throws IOException {
		super(searchable);
		this.cache = new PwaBlacklistCache(reader,null); // the PwaBlacklistCache will use the blacklist configuration (file path) from initialization
	}
	
	
	/**
	 * Indicates if has document
	 * @return
	 */
	public boolean hasDoc() {
		return searchable.hasDoc();
	}
	
	/**
	 * Get document id	
	 */
	public int doc() {		
		return searchable.doc();
	}
	
	/**
	 * Move to next document
	 */
	public boolean next() throws IOException {  
		while (searchable.next()) {
			if (cache.isValid(searchable.doc())) { // return if it is not in blacklist
				return true;
			}
		}
		return false;
	}

}
