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
	 * @param docTimestamp document's timestamp	(millisec) 
	 * @param maxTimestamp timestamp of the newest document in the collection
	 * @param minTimestamp timestamp of the oldest document in the collection
	 */
	public PwaBoostOlder(long docTimestamp, long maxTimestamp, long minTimestamp) {
		double maxSpan=maxTimestamp-minTimestamp;
		maxSpan/= DAY_MILLISEC; // span in days
		double span=docTimestamp-minTimestamp;
		span/= DAY_MILLISEC; // span in days
		score=Math.pow(Math.E, -1*span/maxSpan);				
	}	
	
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
