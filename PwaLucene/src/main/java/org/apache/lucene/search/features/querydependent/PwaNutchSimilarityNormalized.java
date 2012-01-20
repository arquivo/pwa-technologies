package org.apache.lucene.search.features.querydependent;

import java.util.Vector;

import org.apache.lucene.search.features.PwaIRankingFunction;


/**
 * Scores with a normalized exponential decay. 
 * @note this type of normalization is better for comparing values between different queries than dividing all scores by the best one, which would be always 1 independently of its performance
 * @author Miguel Costa
 */
public class PwaNutchSimilarityNormalized implements PwaIRankingFunction {

	private final static float MAX_SCORE=113; // max score within 1000 queries extracted from query logs and submitted to the system
	private float score;
	private PwaNutchSimilarity similarity;
	
	
	/**
	 * Constructor
	 * @param tfPerField number of matches of terms from query in document per field
	 * @param idfPerField number of documents where the terms in query appear per field
	 * @param nTermsPerField number of all terms per field 
	 * @param nDocs number of all documents in collection	 
	 */
	public PwaNutchSimilarityNormalized(Vector<Vector<Integer>> tfPerField, Vector<Vector<Integer>> idfPerField, Vector<Integer> nTermsPerField, int nDocs) {
		similarity=new PwaNutchSimilarity(tfPerField, idfPerField, nTermsPerField, nDocs);
		score=similarity.score();
		score=MAX_SCORE-score;
		score=(float)Math.pow(Math.E, -1*score/MAX_SCORE);				
	}		
	
	/**
	 * Return score
	 */
	public float score() {
		return score;
	}		
}
