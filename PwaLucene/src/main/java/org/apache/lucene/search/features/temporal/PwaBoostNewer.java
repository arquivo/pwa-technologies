package org.apache.lucene.search.features.temporal;

import org.apache.lucene.search.features.PwaIRankingFunction;


/**
 * Scores based on the time span with a normalized exponential decay. It boosts documents with a more recent timestamp. 
 * @author Miguel Costa
 */
public class PwaBoostNewer implements PwaIRankingFunction {

	private float score;
	
	
	/**
	 * Constructor
	 * @param docTimestamp document's timestamp	(millisec) 
	 * @param maxTimestamp timestamp of the newest document in the collection
	 * @param minTimestamp timestamp of the oldest document in the collection
	 */
	public PwaBoostNewer(long docTimestamp, long maxTimestamp, long minTimestamp) {
		float maxSpan=maxTimestamp-minTimestamp;
		maxSpan/= DAY_MILLISEC; // span in days
		float span=maxTimestamp-docTimestamp;
		span/= DAY_MILLISEC; // span in days
		score=(float)Math.pow(Math.E, -1*span/maxSpan);				
	}	
	
	/**
	 * Return score
	 */
	public float score() {
		return score;
	}		
}
