package org.apache.lucene.search.features.temporal;

import org.apache.lucene.search.features.PwaIRankingFunction;


/**
 * Scores based on the document's age 
 * @author Miguel Costa
 */
public class PwaAge implements PwaIRankingFunction {
	
	private float score;
	
	
	/**
	 * Constructor
	 * @param docTimestamp document's timestamp	(millisec) 
	 * @param queryTimestamp timestamp when the query was submitted (millisec)
	 */
	public PwaAge(long docTimestamp, long queryTimestamp) {			
		float diff = queryTimestamp-docTimestamp;
		score = diff / DAY_MILLISEC; // difference in days		
	}
	
	/**
	 * Return score
	 */
	public float score() {
		return score;
	}		
}
