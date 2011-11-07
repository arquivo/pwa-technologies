package org.apache.lucene.search.rankers.queryindependent;

import org.apache.lucene.search.rankers.PwaIRankingFunction;


/**
 * Linearize inlinks IR function based on inlinks distribution that have a 2.1 power
 * @author Miguel Costa
 */
public class PwaLinInlinks implements PwaIRankingFunction {

	private final static double POWER=2.1;
	private double score;
	
	
	/**
	 * Constructor
	 * @param nInlinks number of inlinks
	 */
	public PwaLinInlinks(int nInlinks) {
		if (nInlinks==0) {
			score=0;
		}
		else {
			score=Math.log10(Math.pow(nInlinks, POWER));
		}
	}
	
	/**
	 * Return score
	 */
	public double score() {
		return score;
	}
		
}
