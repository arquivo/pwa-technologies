package org.apache.lucene.search.rankers.temporal;

import org.apache.lucene.search.rankers.PwaIRankingFunction;


/**
 * Scores based on the time span with a normalized exponential grow. It boosts documents with an older timestamp. 
 * @author Miguel Costa
 */
public class PwaBoostOlder implements PwaIRankingFunction {

	private double score;
	
	
	/**
	 * Constructor
	 * @param span distance in days
	 * @param maxSpan maximum span in days
	 */
	public PwaBoostOlder(double span, double maxSpan) {			
		span=maxSpan-span;		
		score=Math.pow(Math.E, -1*span/maxSpan);				
	}
	
	/**
	 * Return score
	 */
	public double score() {
		return score;
	}		
}
