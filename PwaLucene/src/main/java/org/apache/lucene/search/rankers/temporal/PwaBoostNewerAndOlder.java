package org.apache.lucene.search.rankers.temporal;

import org.apache.lucene.search.rankers.PwaIRankingFunction;


/**
 * Scores based on the time span combining PwaBoostNewer and PwaBoostOlder. It boosts documents with a newer and older timestamp.
 * @author Miguel Costa
 */
public class PwaBoostNewerAndOlder implements PwaIRankingFunction {

	private double score;
	
	
	/**
	 * Constructor
	 * @param span distance in days	 
	 * @param maxSpan maximum span in days
	 */
	public PwaBoostNewerAndOlder(double span, double maxSpan) {
		double middle=maxSpan/2;
		if (span<middle) {
			PwaBoostNewer ranker=new PwaBoostNewer(span,maxSpan);
			score=ranker.score();
		}
		else {
			PwaBoostOlder ranker=new PwaBoostOlder(span,maxSpan);
			score=ranker.score();
		}				
	}
	
	/**
	 * Return score
	 */
	public double score() {
		return score;
	}		
}
