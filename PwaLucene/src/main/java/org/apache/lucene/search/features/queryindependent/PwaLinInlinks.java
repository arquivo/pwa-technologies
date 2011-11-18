package org.apache.lucene.search.features.queryindependent;

import org.apache.lucene.search.features.PwaIRankingFunction;


/**
 * Linearize inlinks IR function based on inlinks distribution that have a 2.1 power
 * @author Miguel Costa
 */
public class PwaLinInlinks implements PwaIRankingFunction {

	private final static double POWER=2.1;
	private float score;
	
	
	/**
	 * Constructor
	 * @param nInlinks number of inlinks
	 */
	public PwaLinInlinks(int nInlinks) {
		if (nInlinks==0) {
			score=0;
		}
		else {
			score=(float)Math.log10(Math.pow(nInlinks, POWER));
		}
	}
	
	/**
	 * Return score
	 */
	public float score() {
		return score;
	}
		
}
