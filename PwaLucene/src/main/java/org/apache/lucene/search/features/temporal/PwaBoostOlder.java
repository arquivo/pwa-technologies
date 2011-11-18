package org.apache.lucene.search.features.temporal;

import org.apache.lucene.search.features.PwaIRankingFunction;


/**
 * Scores based on the time span with a normalized exponential grow. It boosts documents with an older timestamp. 
 * @author Miguel Costa
 */
public class PwaBoostOlder implements PwaIRankingFunction {

	private float score;
	
		
	/**
	 * Constructor
	 * @param docTimestamp document's timestamp	(millisec) 
	 * @param maxTimestamp timestamp of the newest document in the collection
	 * @param minTimestamp timestamp of the oldest document in the collection
	 */
	public PwaBoostOlder(long docTimestamp, long maxTimestamp, long minTimestamp) {
		float maxSpan=maxTimestamp-minTimestamp;
		maxSpan/= DAY_MILLISEC; // span in days
		float span=docTimestamp-minTimestamp;
		span/= DAY_MILLISEC; // span in days
		score=(float)Math.pow(Math.E, -1*span/maxSpan);				
	}	
	
	public PwaBoostOlder(double span, double maxSpan) {			
		span=maxSpan-span;		
		score=(float)Math.pow(Math.E, -1*span/maxSpan);				
	}
	
	/**
	 * Return score
	 */
	public float score() {
		return score;
	}		
}
