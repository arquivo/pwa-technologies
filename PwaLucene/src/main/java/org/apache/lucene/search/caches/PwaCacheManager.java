package org.apache.lucene.search.caches;

import java.io.IOException;
import java.util.Hashtable;

import org.apache.lucene.index.IndexReader;


/**
 * Initialize and manages caches
 * @author Miguel Costa
 */
public class PwaCacheManager {
	
	/** multiple caches */
	private static Hashtable<String,PwaCache> caches=null;
	private static Object lockObj=new Object();
	private static PwaCacheManager instance=null; // singleton class
	
	
	/**
	 * Constructor
	 */
	private PwaCacheManager(IndexReader reader) throws IOException {
		System.out.println("Initializing caches at "+this.getClass().getSimpleName()+" class.");
		caches=new Hashtable<String,PwaCache>();
		
		// blacklist of documents
		PwaCache cache=new PwaBlacklistCache(reader); 		
		caches.put(cache.getFieldName(),cache);		
		// indicates if it is a new version
		cache=new PwaDigestDiffCache(reader); 
		caches.put(cache.getFieldName(),cache);		
		// version group
		cache=new PwaUrlRadicalIdCache(reader); 	
		caches.put(cache.getFieldName(),cache);				
		// timestamp ranges
		cache=new PwaDateCache(reader);
		caches.put(cache.getFieldName(),cache);	
		// searching types			  		  	
		//cache=new ArquivoWebSearchTypesCache(reader,includeExtensions);
		//caches.put(cache.getFieldName(),cache);	
		
		// intialize index stats
		PwaIndexStats.getInstance(reader);
		// initialize stopwords
		PwaStopwords.getInstance(reader);
		
		System.out.println("Initializing caches at "+this.getClass().getSimpleName()+" class ended.");
	}	
	
	/**
	 * Get instance
	 * @param reader index reader
	 * @return
	 * @throws IOException
	 */
	public static PwaCacheManager getInstance(IndexReader reader) throws IOException {
		if (instance!=null) {
			return instance;
		}
		
		synchronized(lockObj) {
			if (instance!=null) {
				return instance;
			}
			instance=new PwaCacheManager(reader);			
		}
		return instance;
	}
	
	/**
	 * Get value from cache
	 * @param fieldName field name cached
	 * @param doc document id
	 * @return
	 */
	public Object getValue(String fieldName, int doc) {
		if (caches.get(fieldName)==null) {
			return null;
		}
		return caches.get(fieldName).getValue(doc);
	}		
}
