package org.apache.lucene.search.features.temporal;

import org.apache.lucene.search.features.PwaIRankingFunction;


/**
 * Scores based on the number of versions of a URL 
 * @author Miguel Costa
 */
public class PwaNumberVersions implements PwaIRankingFunction {
	
	private float score;
	
	
	/**
	 * Constructor
	 * @param numberVersions number of versions of this document/URL
	 * @param maxNumberVersions maximum number of versions of a document/URL in the collection
	 */
	public PwaNumberVersions(long numberVersions, long maxNumberVersions) {			
		score = (float)Math.log10(numberVersions) / (float)Math.log10(maxNumberVersions);									
	}	
	
	/**
	 * Return score
	 */
	public float score() {
		return score;
	}		
}
