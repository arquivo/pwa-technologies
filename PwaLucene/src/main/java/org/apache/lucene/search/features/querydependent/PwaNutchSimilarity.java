package org.apache.lucene.search.features.querydependent;

import java.util.Vector;

import org.apache.lucene.search.caches.PwaIndexStats;
import org.apache.lucene.search.features.PwaIRankingFunction;


/**
 * Nutch IR function (@see Similarity and NutchSimilarity class and API)
 * @author Miguel Costa
 */
public class PwaNutchSimilarity implements PwaIRankingFunction {

	private float boosts[]={1.0f, 4.0f, 2.0f, 2.0f, 1.5f};   // the same order as PwaIndexStats.FIELDS[]={"content","url","host","anchor","title"} 
	private float score;
	
	
	/**
	 * Constructor
	 * @param tfPerField number of matches of terms from query in document per field
	 * @param idfPerField number of documents where the terms in query appear per field
	 * @param nTermsPerField number of all terms per field 
	 * @param nDocs number of all documents in collection	 
	 */
	public PwaNutchSimilarity(Vector<Vector<Integer>> tfPerField, Vector<Vector<Integer>> idfPerField, Vector<Integer> nTermsPerField, int nDocs) {
		double tfNorm;
		double idfNorm;			
		Vector<Integer> tf;
		Vector<Integer> idf;
		int nTerms=0;
		double sumOfSquaredWeights=0;
			
		for (int j=0;j<tfPerField.size();j++) { // for all fields
			tf=tfPerField.get(j);
			idf=idfPerField.get(j);
			nTerms=nTermsPerField.get(j);
		
			for (int i=0;i<tf.size();i++) { // for all query terms
				if (tf.get(i)!=0) {
					tfNorm= Math.sqrt((double)tf.get(i));
					idfNorm= 1+Math.log((double)nDocs/(double)(idf.get(i)+1));
					sumOfSquaredWeights+= Math.pow(idfNorm, 2);
					idfNorm= Math.pow(idfNorm, 2); // idfNorm*idfNorm				
					score+= tfNorm * idfNorm * norm(PwaIndexStats.FIELDS[j],nTerms,boosts[j]);
					
				}
			}	
		}
		
		score*= queryNorm(sumOfSquaredWeights); // normalizing factor used to make scores between queries comparable
	}
	
	/**
	 * Return score
	 */
	public float score() {
		return score;
	}
	
	
	/**
	 * 
	 * Nutch code
	 * 
	 */
	
	private static final int MIN_CONTENT_LENGTH = 1000;

	private float nutchLengthNorm(String fieldName, int numTokens) {           
		if ("url".equals(fieldName)) {                // URL: prefer short
			return 1.0f / numTokens;                    // use linear normalization
		} 
		else if ("anchor".equals(fieldName)) {      // Anchor: prefer more
			return (float)(1.0/Math.log(Math.E+numTokens)); // use log
		} 
		else if ("content".equals(fieldName)) {     // Content: penalize short
			return lengthNorm(fieldName, Math.max(numTokens, MIN_CONTENT_LENGTH));
		} 
		else {                                      // use default
			return lengthNorm(fieldName, numTokens);
		}
	}

	private float lengthNorm(String fieldName, int numTerms) {
		return (float)(1.0 / Math.sqrt(numTerms));
	}
		
	private float norm(String field, int nTerms, float boost) { // see this computation in DocumentWriter
		return nutchLengthNorm(field,nTerms) * boost;		
	}	
		
	public float queryNorm(double sumOfSquaredWeights) {
		return (float)(1.0 / Math.sqrt(sumOfSquaredWeights));
	}	

	/* This is always 1, since all terms must match in the document
	public float coord(int overlap, int maxOverlap) {
		return overlap / (float)maxOverlap;
	}
	 */
	
}
