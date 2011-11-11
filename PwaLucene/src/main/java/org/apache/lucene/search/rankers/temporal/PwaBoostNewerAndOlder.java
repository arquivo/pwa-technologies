package org.apache.lucene.search.rankers.temporal;

import org.apache.lucene.search.rankers.PwaIRankingFunction;


/**
 * Scores based on the time span combining PwaBoostNewer and PwaBoostOlder. It boosts documents with a newer and older timestamp.
 * @author Miguel Costa
 */
public class PwaBoostNewerAndOlder implements PwaIRankingFunction {

	private double score;
	
	
	/**
	 * Constructor
	 * @param docTimestamp document's timestamp	(millisec) 
	 * @param maxTimestamp timestamp of the newest document in the collection
	 * @param minTimestamp timestamp of the oldest document in the collection
	 */
	public PwaBoostNewerAndOlder(long docTimestamp, long maxTimestamp, long minTimestamp) {	
		long middle=(maxTimestamp-minTimestamp)/2;		
		if (docTimestamp<middle) {
			PwaBoostOlder ranker=new PwaBoostOlder(docTimestamp, maxTimestamp, minTimestamp);
			score=ranker.score();
		}
		else {
			PwaBoostNewer ranker=new PwaBoostNewer(docTimestamp, maxTimestamp, minTimestamp);
			score=ranker.score();
		}				
	}
	
	/**
	 * Return score
	 */
	public double score() {
		return score;
	}		
}
