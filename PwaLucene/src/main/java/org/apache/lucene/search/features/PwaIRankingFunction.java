package org.apache.lucene.search.features;

/**
 * Ranking functions interface
 * @author Miguel Costa
 */
public interface PwaIRankingFunction {
	
	/* milliseconds in a day */
	public final static float DAY_MILLISEC =  24 * 60 * 60 * 1000;
	
	/**
	 * Returns the computed score
	 * @return score
	 */
	public float score();	
}
