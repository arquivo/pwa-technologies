package org.apache.lucene.search.features.querydependent;

import org.apache.lucene.search.features.PwaIRankingFunction;


/**
 * Scores based on the distance values with a normalized exponential decay. Based on "An Exploration of Proximity Measures in Information Retrieval"
 * Supports MinSpanCov(ord) & MinSpanCov(unord) & MinPairDist
 * @author Miguel Costa
 */
public class PwaMinSpan implements PwaIRankingFunction {

	private float score;
	
	
	/**
	 * Constructor
	 * @param span distance between terms (it depends of the function)	 
	 */
	public PwaMinSpan(int span) {		
		score=(float)Math.log(1+Math.pow(Math.E, -1*span));		
	}
	
	/**
	 * Return score
	 */
	public float score() {
		return score;
	}
		
}
