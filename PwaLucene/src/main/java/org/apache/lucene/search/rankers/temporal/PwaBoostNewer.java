package org.apache.lucene.search.rankers.temporal;

import org.apache.lucene.search.rankers.PwaIRankingFunction;


/**
 * Scores based on the time span with a normalized exponential decay. It boosts documents with a more recent timestamp. 
 * @author Miguel Costa
 */
public class PwaBoostNewer implements PwaIRankingFunction {

	private double score;
	
	
	/**
	 * Constructor
	 * @param span distance in days	 
	 * @param maxSpan maximum span in days
	 */
	public PwaBoostNewer(double span, double maxSpan) {				
		score=Math.pow(Math.E, -1*span/maxSpan);				
	}
	
	/**
	 * Return score
	 */
	public double score() {
		return score;
	}		
}
