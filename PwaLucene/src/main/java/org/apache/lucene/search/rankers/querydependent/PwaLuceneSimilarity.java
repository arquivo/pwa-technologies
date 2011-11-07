package org.apache.lucene.search.rankers.querydependent;

import java.util.Vector;

import org.apache.lucene.search.caches.PwaIndexStats;
import org.apache.lucene.search.rankers.PwaIRankingFunction;


/**
 * Lucene IR function (@see Similarity and NutchSimilarity class and API)
 * @author Miguel Costa
 */
public class PwaLuceneSimilarity implements PwaIRankingFunction {

	private float boosts[]={1.0f, 4.0f, 2.0f, 2.0f, 1.5f};   // the same order as PwaIndexStats.FIELDS[]={"content","url","host","anchor","title"} 
	private double score;
	
	
	/**
	 * Constructor
	 * @param tfPerField number of matches of terms from query in document per field
	 * @param idfPerField number of documents where the terms in query appear per field
	 * @param nTermsPerField number of all terms per field 
	 * @param nDocs number of all documents in collection	 
	 */
	public PwaLuceneSimilarity(Vector<Vector<Integer>> tfPerField, Vector<Vector<Integer>> idfPerField, Vector<Integer> nTermsPerField, int nDocs) {
		double tfNorm;
		double idfNorm;			
		Vector<Integer> tf;
		Vector<Integer> idf;
		int nTerms;
			
		for (int j=0;j<tfPerField.size();j++) { // for all fields
			tf=tfPerField.get(j);
			idf=idfPerField.get(j);
			nTerms=nTermsPerField.get(j);
		
			for (int i=0;i<tf.size();i++) { // for all query terms
				if (tf.get(i)!=0) {
					tfNorm=Math.sqrt((double)tf.get(i));
					idfNorm=1+Math.log((double)nDocs/idf.get(i)+1); 
					score+= tfNorm*idfNorm* norm(PwaIndexStats.FIELDS[j],nTerms,boosts[j]);
				}
			}	
		}
	}
	
	/**
	 * Return score
	 */
	public double score() {
		return score;
	}
	
	
	/**
	 * 
	 * Nutch and Lucene code
	 * 
	 */
	
	private static final int MIN_CONTENT_LENGTH = 1000;

	private float lengthNorm(String fieldName, int numTokens) {           
		if ("url".equals(fieldName)) {                // URL: prefer short
			return 1.0f / numTokens;                    // use linear normalization
		} 
		else if ("anchor".equals(fieldName)) {      // Anchor: prefer more
			return (float)(1.0/Math.log(Math.E+numTokens)); // use log
		} 
		else if ("content".equals(fieldName)) {     // Content: penalize short
			return superLengthNorm(fieldName,          // treat short as longer
					Math.max(numTokens, MIN_CONTENT_LENGTH));
		} 
		else {                                      // use default
			return superLengthNorm(fieldName, numTokens);
		}
	}

	private float superLengthNorm(String fieldName, int numTerms) {
		return (float)(1.0 / Math.sqrt(numTerms));
	}
		
	private float norm(String field, int nTerms, float boost) { // see this computation in DocumentWriter
		return lengthNorm(field,nTerms) * boost;		
	}	
}
