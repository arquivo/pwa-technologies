package org.apache.lucene.search;

import java.io.IOException;
import java.util.*;

import org.apache.lucene.util.PriorityQueue;

import org.apache.lucene.index.*;


/**
 * Computes the minimal distance between query terms
 * @author Miguel Costa
 */
public class PwaPositionsManager {

	private Vector<PwaTerm> terms;
	private PwaPositions positions[];
	private PwaPhraseQueue queue;
	private int minSpanCovUnordered; // minimum span including all query terms
	private int minSpanCovOrdered; // minimum span including all query terms ordered as submited
	private int minPairDist; // minimum distance between two query terms	
	private String field;	
	private Vector<Integer> offsetTerms; // offset of terms in query
	private int nTermsInQuery;
	

	/**
	 * Constructor
	 * @param terms query terms
	 * @throws IOException
	 */
	public PwaPositionsManager(Vector<PwaTerm> terms) throws IOException {
		this.terms=terms;
		if (terms!=null) {					
			this.queue=new PwaPhraseQueue(terms.size());
			this.positions=new PwaPositions[terms.size()];
			this.field=terms.get(0).term().field();
			this.offsetTerms=null;
			this.nTermsInQuery=terms.size();
		}
	}
	
	/**
	 * Constructor
	 * @param terms query terms
	 * @param offsetTerms offset of terms in query
	 * @throws IOException
	 */
	public PwaPositionsManager(Vector<PwaTerm> terms, Vector<Integer> offsetTerms) throws IOException {
		this(terms);
		this.offsetTerms=offsetTerms;
		this.nTermsInQuery=offsetTerms.lastElement()+1;		
	}

	/**
	 * Compute minimal distances: ordered and unordered
	 * @param doc doc joined
	 * @throws IOException
	 * @note query terms that are stopwords, are considered wildcards due to efficiency
	 */
	public void computeDistances(int doc) throws IOException {		
		minSpanCovUnordered=Integer.MAX_VALUE;
		minSpanCovOrdered=Integer.MAX_VALUE;
		minPairDist=Integer.MAX_VALUE;
		
		if (terms==null || terms.size()<2) { // give the maximum weight with 0 or 1 term
			minSpanCovUnordered=0;
			minSpanCovOrdered=0;
			minPairDist=0;
			return;
		}
		
		// get positions from pairs term/field		
		for (int i=0;i<terms.size();i++) {													
			positions[i]=terms.get(i).getPos(doc);
			if (positions[i]==null) { // if a term does not have positions for a field, the distance is maximum
				return;
			}			
		}		
			
		queue.clear();		
		int end = 0;
		boolean done = false;
		// sets priority queue with initial positions of each term
		for (int i=0;i<positions.length;i++) {
			if (!positions[i].next()) {  
				return;
			}
			if (positions[i].get()>end) {
				end=positions[i].get();
			}
			queue.put(positions[i]); // store in priority queue			
		}

		do {
			PwaPositions pos = (PwaPositions)queue.pop();
			int start = pos.get();
			int next = ((PwaPositions)queue.top()).get();
			for (int i=start; i<=next && !done; ) {
				start = i;                              // advance pos to min window
				if (!pos.next()) {
					done=true;					
				}
				else {
					i=pos.get();
				}
			}

			// compute distances			
			int matchLength = end-start - nTermsInQuery+1;
			if (minSpanCovUnordered>matchLength) {
				minSpanCovUnordered=matchLength;
			}
			if (minSpanCovOrdered>matchLength) {				
				boolean testOrder=true;
				for (int i=1;i<positions.length && testOrder;i++) {
					int a = (pos==positions[i-1]) ? start : positions[i-1].get();
					int b = (pos==positions[i]) ? start : positions[i].get();
					if (a>b || (offsetTerms!=null && offsetTerms.get(i)-offsetTerms.get(i-1)!=b-a)) { // the order and distance must be maintained
						testOrder=false;
					}					
				}
				if (testOrder) {
					minSpanCovOrdered=matchLength;
				}
			}
			if (minPairDist>next-start-1) {
				minPairDist=next-start-1;
			}
			
			if (minSpanCovOrdered==0) { // exit when the minimal distance is achieved for all measures
				done=true;
			}		
			if (!done) {
				if (pos.get()>end) {
					end=pos.get();
				}
				queue.put(pos); // restore pq
			}
		} 
		while (!done);
	}


	/**
	 * Get minimum distance between sequential terms in a document, covering all query terms at least once
	 * @return
	 */
	public int getMinSpanCovUnordered() {
		return minSpanCovUnordered;
	}

	/**
	 * Get minimum distance between sequential terms in a document, covering all query terms at least once in the same order as the query
	 * @return
	 */
	public int getMinSpanCovOrdered() {
		return minSpanCovOrdered;
	}
	
	/**
	 * Get the minimum distance between all pairs of unique query terms
	 * @return
	 */
	public int getMinPairDist() {
		return minPairDist;
	}
	
	/**
	 * Get field
	 * @return
	 */
	public String getField() {
		return field;
	}
}
