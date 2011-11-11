package org.apache.lucene.search.rankers;

/**
 * Ranking functions interface
 * @author Miguel Costa
 */
public interface PwaIRankingFunction {
	
	/* milliseconds in a day */
	public final static double DAY_MILLISEC =  24 * 60 * 60 * 1000;
	
	/**
	 * Returns the computed score
	 * @return score
	 */
	public double score();	
}
