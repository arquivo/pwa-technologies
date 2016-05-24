package org.apache.lucene.search.features.queryindependent;

import java.net.*;

import org.apache.lucene.search.features.PwaIRankingFunction;


/**
 * Gives a value according to the URL number of slashes
 * @author Miguel Costa
 */
public class PwaUrlSlashes implements PwaIRankingFunction {
	
	private float score;
	
	
	/**
	 * Constructor
	 * @param surl url
	 */
	public PwaUrlSlashes(String surl) throws MalformedURLException {
		score=0;
		for (int i=7; i<surl.length(); i++) {
			if (surl.charAt(i)=='/') {
				score++;
			}
		}						 
	}
	
	/**
	 * Return score
	 */
	public float score() {
		return score;
	}
		
}
