package org.apache.lucene.search;

import java.util.Vector;
import java.io.IOException;


/**
 * Join posting lists of query terms of a phrase for one field
 * @author Miguel Costa
 */
public class PwaPhrase extends PwaJoiner {
				
	/* terms vector */
	private Vector<PwaTerm> terms;
	
	
	/**
	 * Constructor
	 * @param terms sequential terms with a field
	 * @param offsetTerms offset of terms in query
	 */
	public PwaPhrase(Vector<PwaTerm> terms, Vector<Integer> offsetTerms) throws IOException {			
		this.terms=terms;
		
		// set join of AND terms
		join=new PwaJoinTwoAND(terms.get(0));
		for (int i=1;i<terms.size();i++) {
			join=new PwaJoinTwoAND(join,terms.get(i));
		}		
				
		// initially does not have documents
		hasDoc=false;
		
		// set position manager
		posmanagers= new Vector<PwaPositionsManager>();
		posmanagers.add(new PwaPositionsManager(terms,offsetTerms));
		setPositionsManager(posmanagers);		
	}
	
	/**
	 * Move to next document
	 * @return true if has more documents; false otherwise
	 */
	public boolean next() throws IOException {		
		do {
			hasDoc=join.next();			
			if (!hasDoc) {
				return false;
			}
				
			// filter by positions - must have 0 distance
			posmanagers.get(0).computeDistances(join.doc()); // get 0 because we have only one manager for one field
			if (posmanagers.get(0).getMinSpanCovOrdered()>0) {
				hasDoc=false;
			}
		}
		while (!hasDoc);
		
		return hasDoc; // is true
	}	
	
	/**
	 * Skip to document @doc or superior
	 * @return true if skip to document @doc or superior; false otherwise
	 */
	public boolean skipTo(int doc) throws IOException {	
		if (join.hasDoc() && join.doc()>doc) { // to speedup
			return true;
		}
		
		hasDoc=join.skipTo(doc);
		if (!hasDoc) {
			return false;
		}
				
		// filter by positions - must have 0 distance
		posmanagers.get(0).computeDistances(join.doc()); // get 0 because we have only one manager for one field
		if (posmanagers.get(0).getMinSpanCovOrdered()==0) {			
			hasDoc=true;
			return true;
		}
		
		// else return true if the next with distance 0 after the skip exist
		return next();			
	}
	
	/**
	 * Skip to document @doc or superior from start
	 * @return true if skip to document @doc or superior from start; false otherwise
	 */
	public boolean skipToFromStart(int doc) throws IOException {
		if (join.hasDoc() && join.doc()>doc) { // to speedup
			return true;
		}
		
		hasDoc=join.skipToFromStart(doc);
		if (!hasDoc) {
			return false;
		}
				
		// filter by positions - must have 0 distance
		posmanagers.get(0).computeDistances(join.doc()); // get 0 because we have only one manager for one field
		if (posmanagers.get(0).getMinSpanCovOrdered()==0) {			
			hasDoc=true;
			return true;
		}
		
		// else return true if the next with distance 0 after the skip exist
		return next();				
	}
		
	/**
	 * Get terms 
	 * @return terms
	 */
	public Vector<PwaTerm> getTerms() {
		return terms;
	}
		
	/**
	 * Collect ranking features
	 * @param doc document id
	 * @param collector features collector 
	 */
	public void collectFeatures(int doc, PwaRawFeatureCollector collector) throws IOException {
		for (int i=0;i<terms.size();i++) {			
			if (terms.get(i).hasDoc() && terms.get(i).doc()==doc) {
				terms.get(i).collectFeatures(doc,collector);
			}
			else {				
				terms.get(i).collectEmptyFeatures(doc,collector);
			}		
		}
	}
	
	/**
	 * Set ranking features with empty values
	 * @param doc document id
	 * @param collector features collector
	 */
	public void collectEmptyFeatures(int doc, PwaRawFeatureCollector collector) throws IOException {						
		for (int i=0;i<terms.size();i++) {			
			terms.get(i).collectEmptyFeatures(doc,collector);
		}
	}
}
