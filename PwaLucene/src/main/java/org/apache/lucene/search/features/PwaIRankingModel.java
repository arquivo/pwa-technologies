package org.apache.lucene.search.features;

import org.apache.lucene.search.PwaFunctionsWritable;


/**
 * Ranking functions interface
 * @author Miguel Costa
 */
public interface PwaIRankingModel {
			
	/**
	 * Returns the computed score
	 * @return score
	 */
	public float score(PwaFunctionsWritable functions, PwaScores scores);	
}
