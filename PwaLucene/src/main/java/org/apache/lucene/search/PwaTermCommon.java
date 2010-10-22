package org.apache.lucene.search;

import java.io.IOException;

import org.apache.lucene.index.*;


/**
 * Common methods to read and store data associated to term
 * @author Miguel Costa
 */
public class PwaTermCommon extends PwaSearchable {

	private final static int SIZE_BUFFER=32;
		
	protected Term term;
	protected IndexReader reader;
	protected TermDocs termDocs;
	protected int pointer;
	protected int pointerMax;    
	protected int docs[]=new int[SIZE_BUFFER];	
	protected int freqs[]=new int[SIZE_BUFFER]; 
	
	
	/**
	 * Constructor
	 * @param term term
	 * @param reader index reader
	 * @throws IOException
	 */
	public PwaTermCommon(Term term, IndexReader reader) throws IOException {
		this.term=term;
		this.reader=reader;
		this.termDocs=reader.termDocs(term);				
		this.pointer=-2;
		this.pointerMax=-1;
	}
	
	/**
	 * Indicates if has more documents
	 * @return true if has more documents; false otherwise
	 */
	public boolean hasDoc() {
		return (pointer>-1);
	}
	
	/**
	 * Move to next document
	 * @return true if has more documents; false otherwise
	 */
	public boolean next() throws IOException {		
		if (pointer==-1) {
			return false;
		}
			
		if (pointer==pointerMax-1) { // refill buffer			
			pointerMax = termDocs.read(docs, freqs);    // refill buffer
			pointer=-1;
			if (pointerMax==0) { // if docs do not exist, exit 	
				termDocs.close();
				return false;
			}
		}

		pointer++;				
		return true;
	}
	

	/** 
	 * Skips to the first match beyond the current whose document number is
	 * greater than or equal to a given target. 
	 * <br>The implementation uses {@link TermDocs#skipTo(int)}.
	 * @param doc The target document number.
	 * @return true iff there is such a match.
	 * @note method similar to method of TermScore
	 * @note does not go backwards
	 */
	public boolean skipTo(int doc) throws IOException {		
		
		// find in cache first (TODO binary search)
		if (pointerMax>0 && doc<=docs[pointerMax-1]) { // within cache 			
			for (int i=Math.max(0,pointer);i<pointerMax;i++) {
				if (docs[i]>=doc) {
					pointer=i;												
					return true;
				}
			}							
		}
		
	    // not found in cache, so read from index start	
	    boolean result = termDocs.skipTo(doc);
	    if (result) {
	    	pointerMax=1; // buffer size=1
	        pointer=0;
	        docs[pointer]=termDocs.doc();
	        freqs[pointer]=termDocs.freq();	        	      
			return true;
	    }
	    pointer=-1;
	    return false;	    
	}			
	
	/**
	 * Skips to document from start
	 */
	public boolean skipToFromStart(int doc) throws IOException {
		this.termDocs=reader.termDocs(this.term);
		this.pointer=-2;
		this.pointerMax=-1;
		return skipTo(doc);
	}
			
	/**
	 * Get document id
	 * @return document id
	 */
	public int doc() {
		return docs[pointer];	
	}
	
	/**
	 * Get term frequency
	 * @return term frequency
	 */
	public int tf() {
		return freqs[pointer];
	}
	
	/**
	 * Get term 
	 * @return term 
	 */
	public Term term() {
		return term;	
	}
	
	/**
	 * Collect ranking features from this document
	 * @param doc document id
	 * @param collector features collector 
	 */
	public void collectFeatures(int doc, PwaRawFeatureCollector collector) throws IOException {
		// do nothing
	}	
	
	/**
	 * Set ranking features with empty values for this document
	 * @param doc document id
	 * @param collector features collector
	 */
	public void collectEmptyFeatures(int doc, PwaRawFeatureCollector collector) throws IOException {
		// do nothing
	}		
	
	/**
	 * Indicates if this document should be excluded
	 * @return true if this document should be excluded; false otherwise
	 */
	public boolean isExclude() {	
		throw new RuntimeException("this methods should not be called!");
	}	
}
