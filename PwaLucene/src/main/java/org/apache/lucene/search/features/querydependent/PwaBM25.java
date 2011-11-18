package org.apache.lucene.search.features.querydependent;

import java.util.Vector;

import org.apache.lucene.search.features.PwaIRankingFunction;


/**
 * BM25 IR function (@see function description in http://en.wikipedia.org/wiki/Tf-idf or paper "Simple BM25 Extension to Multiple Weighted Fields"
 * @author Miguel Costa
 */
public class PwaBM25 implements PwaIRankingFunction {

	private final static double K1=2.0; // normalizing value
	private final static double B=0.75; // normalizing value
	private float score;
	
		
	/**
	 * Constructor
	 * @param tf number of matches of terms from query in document
	 * @param idf number of documents where the terms in query appear
	 * @param nTerms number of all terms in document	 	
	 * @param avgNTerms average number of terms per document
	 * @param nDocs number of all documents in field
	 */
	public PwaBM25(Vector<Integer> tf, Vector<Integer> idf, int nTerms, double avgNTerms, int nDocs) {
		this(tf,idf,nTerms,avgNTerms,nDocs,K1,B);		
	}
	
	/**
	 * Constructor
	 * @param tf number of matches of terms from query in document
	 * @param idf number of documents where the terms in query appear
	 * @param nTerms number of all terms in document	 	
	 * @param avgNTerms average number of terms per document
	 * @param nDocs number of all documents in field
	 * @param k1Parameter normalizing value
	 * @param bParameter normalizing value
	 */
	public PwaBM25(Vector<Integer> tf, Vector<Integer> idf, int nTerms, double avgNTerms, int nDocs, double k1Parameter, double bParameter) {
		double tfNorm;		
		double idfNorm;
				
		for (int i=0;i<tf.size();i++) {
			if (tf.get(i)!=0) {
				idfNorm=Math.log10((double)(nDocs-idf.get(i)+0.5) / (idf.get(i)+0.5));
				tfNorm=(double)(tf.get(i)*(k1Parameter+1)) / (tf.get(i)+k1Parameter*((1-bParameter)+bParameter*((double)nTerms/avgNTerms)));			
				score+= idfNorm*tfNorm;
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
