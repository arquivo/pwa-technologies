package org.apache.lucene.search.features;

import java.util.Enumeration;

import org.apache.lucene.search.PwaFunctionsWritable;


/**
 * Combines linearly the scores produced by ranking features
 * @author Miguel Costa
 */
public class PwaLinearRankingModel implements PwaIRankingModel {
			
	/**
	 * Returns the computed score
	 * @return score
	 */
	public float score(PwaFunctionsWritable functions, PwaScores scores) {
		Integer key=null;		
		float score=0;
		for (Enumeration<Integer> e = functions.keys(); e.hasMoreElements();) {
    		key=e.nextElement();            
            score += functions.getBoost(key) * scores.getScore(key);
        }
		return score;
	}
}
