package org.apache.lucene.search.rankers.querydependent;

import java.util.Vector;

import org.apache.lucene.search.rankers.PwaIRankingFunction;


/**
 * TFxIDF IR function (@see function description in http://en.wikipedia.org/wiki/Tf-idf)
 * @author Miguel Costa
 */
public class PwaTFxIDF implements PwaIRankingFunction {

	private double score;
	
	
	/**
	 * Constructor
	 * @param tf number of matches of terms from query in document
	 * @param idf number of documents where the terms in query appear
	 * @param nTerms number of all terms in field
	 * @param nDocs number of all documents in collection	 
	 */
	public PwaTFxIDF(Vector<Integer> tf, Vector<Integer> idf, int nTerms, int nDocs) {
		double tfNorm;
		double idfNorm;
		
		for (int i=0;i<tf.size();i++) { // for all query terms
			if (tf.get(i)!=0) {
				tfNorm=(double)tf.get(i)/nTerms;
				// idfNorm=Math.log10((double)nDocs/idf.get(i));
				idfNorm=Math.log((double)nDocs/idf.get(i)); 
				score+= tfNorm*idfNorm;
			}
		}				
	}
	
	/**
	 * Return score
	 */
	public double score() {
		return score;
	}
		
}
