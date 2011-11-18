package org.apache.lucene.search.features.temporal;

import org.apache.lucene.search.features.PwaIRankingFunction;


/**
 * Scores based on the divergence between a time point in two distributions 
 * @author Miguel Costa
 */
public class PwaTimePointDivergence implements PwaIRankingFunction {

	private float score;
	
	
	/**
	 * Constructor
	 * @param nQuertMatchesInT number of query matches in time point t
	 * @param nQueryMatches number of query matches
	 * @param nDocumentsInT number of documents in time point t
	 * @param nDocuments number of documents	 
	 */
	public PwaTimePointDivergence(double nQuertMatchesInT, double nQueryMatches, double nDocumentsInT, double nDocuments) {				
		score=(float)Math.log10((nQuertMatchesInT/nQueryMatches)/(nDocumentsInT/nDocuments));				
	}
	
	/**
	 * Return score
	 */
	public float score() {
		return score;
	}		
}
