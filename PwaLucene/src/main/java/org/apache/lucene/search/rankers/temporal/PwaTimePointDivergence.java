package org.apache.lucene.search.rankers.temporal;

import org.apache.lucene.search.rankers.PwaIRankingFunction;


/**
 * Scores based on the divergence between a time point in two distributions 
 * @author Miguel Costa
 */
public class PwaTimePointDivergence implements PwaIRankingFunction {

	private double score;
	
	
	/**
	 * Constructor
	 * @param nQuertMatchesInT number of query matches in time point t
	 * @param nQueryMatches number of query matches
	 * @param nDocumentsInT number of documents in time point t
	 * @param nDocuments number of documents	 
	 */
	public PwaTimePointDivergence(double nQuertMatchesInT, double nQueryMatches, double nDocumentsInT, double nDocuments) {				
		score=Math.log10((nQuertMatchesInT/nQueryMatches)/(nDocumentsInT/nDocuments));				
	}
	
	/**
	 * Return score
	 */
	public double score() {
		return score;
	}		
}
