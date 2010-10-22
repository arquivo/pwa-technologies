package org.apache.lucene.search;

import java.util.Vector;
import java.io.IOException;


/**
 * Read and filter data associated to phrase
 * @author Miguel Costa
 */
public class PwaExtraPhrase extends PwaPhrase {
				
	/**
	 * Constructor
	 * @param terms sequential terms belonging to a field
	 * @param offsetTerms offset of terms in query
	 */
	public PwaExtraPhrase(Vector<PwaTerm> terms, Vector<Integer> offsetTerms) throws IOException {		
		super(terms,offsetTerms);
	}
	
	public void collectFeatures(int doc, PwaRawFeatureCollector collector) throws IOException {
		// do nothing
	}	
	
	public void collectEmptyFeatures(int doc, PwaRawFeatureCollector collector) throws IOException {
		// do nothing
	}
	
}
