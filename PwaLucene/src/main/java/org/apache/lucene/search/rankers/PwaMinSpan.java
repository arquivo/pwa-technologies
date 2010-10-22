package org.apache.lucene.search.rankers;


/**
 * Computes distance values
 * Supports MinSpanCov(ord) & MinSpanCov(unord) & MinPairDist
 * @author Miguel Costa
 */
public class PwaMinSpan implements PwaIRankingFunction {

	private double score;
	
	
	/**
	 * Constructor
	 * @param span distance between terms (it depends of the function)	 
	 */
	public PwaMinSpan(int span) {		
		score=Math.log(1+Math.pow(Math.E, -1*span));		
	}
	
	/**
	 * Return score
	 */
	public double score() {
		return score;
	}
		
}
