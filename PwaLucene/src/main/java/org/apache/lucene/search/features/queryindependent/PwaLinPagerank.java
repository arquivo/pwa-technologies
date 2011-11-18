package org.apache.lucene.search.features.queryindependent;

import org.apache.lucene.search.features.PwaIRankingFunction;


/**
 * Linearize Pagerank IR function based on Pagerank distribution that have a 2.1 power
 * @author Miguel Costa
 */
public class PwaLinPagerank implements PwaIRankingFunction {

	private final static double POWER=2.1;
	private float score;
	
	
	/**
	 * Constructor
	 * @param pagerank pagerank value
	 */
	public PwaLinPagerank(float pagerank) {
		if (pagerank==0) {
			score=0;
		}
		else if (pagerank>=1) {
			score=1;
		}
		else {
			score=(float)-1/(float)Math.log10(Math.pow(pagerank, POWER));
		}
	}
	
	/**
	 * Return score
	 */
	public float score() {
		return score;
	}
		
}
