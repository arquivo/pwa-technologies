package org.apache.lucene.search.rankers.temporal;

import org.apache.lucene.search.rankers.PwaIRankingFunction;


/**
 * Scores based on the document's age 
 * @author Miguel Costa
 */
public class PwaAge implements PwaIRankingFunction {
	
	private double score;
	
	
	/**
	 * Constructor
	 * @param docTimestamp document's timestamp	(millisec) 
	 * @param queryTimestamp timestamp when the query was submitted (millisec)
	 */
	public PwaAge(long docTimestamp, long queryTimestamp) {			
		double diff = queryTimestamp-docTimestamp;
		score = diff / DAY_MILLISEC; // difference in days		
	}
	
	/**
	 * Return score
	 */
	public double score() {
		return score;
	}		
}
