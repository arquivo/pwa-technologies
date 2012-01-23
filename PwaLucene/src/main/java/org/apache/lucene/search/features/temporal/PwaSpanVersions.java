package org.apache.lucene.search.features.temporal;

import org.apache.lucene.search.features.PwaIRankingFunction;


/**
 * Scores based on the span between the first and last version of a URL 
 * @author Miguel Costa
 */
public class PwaSpanVersions implements PwaIRankingFunction {
	
	private float score;
	
	
	/**
	 * Constructor
	 * @param maxTimestamp timestamp of the newest version in the collection
	 * @param minTimestamp timestamp of the oldest version in the collection
	 * @param maxSpan maximum of days between first and last archived versions of a document
	 */
	public PwaSpanVersions(long maxTimestamp, long minTimestamp, long maxSpan) {			
		long diff = (maxTimestamp-minTimestamp) / (long)DAY_MILLISEC; // difference in days
		if (maxSpan==0)  { // in case of just one collection, the first and last version are always the same and the span is 0		
			score=0;
		}
		else if (diff==0) { // only one version
			score=0;
		}
		else {
			score = (float)Math.log10(diff) / (float)Math.log10(maxSpan);
		}								
	}	
	
	/**
	 * Return score
	 */
	public float score() {
		return score;
	}		
}
