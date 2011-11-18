package org.apache.lucene.search.features.temporal;

import org.apache.lucene.search.features.PwaIRankingFunction;


/**
 * Scores based on the span between the first and last version of a URL 
 * @author Miguel Costa
 */
public class PwaSpanVersions implements PwaIRankingFunction {
	
	private float score;
	
	
	/**
	 * Constructor
	 * @param maxTimestamp timestamp of the newest document in the collection
	 * @param minTimestamp timestamp of the oldest document in the collection
	 */
	public PwaSpanVersions(long maxTimestamp, long minTimestamp) {			
		float diff = maxTimestamp-minTimestamp;
		score = diff / DAY_MILLISEC; // difference in days		
	}	
	
	/**
	 * Return score
	 */
	public float score() {
		return score;
	}		
}
