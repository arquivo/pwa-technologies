package org.apache.lucene.search;

import java.io.IOException;
import java.util.*;


/**
 * Merges posting lists of several query terms
 * @author Miguel Costa
 */
public class PwaMerger extends PwaSearchable {
	
	private Vector<PwaSearchable> terms;		
	private int minIndex;
	private boolean exclude;

	
	/**
	 * Constructor
	 * @param terms terms
	 * @param exclude flag to exclude if true
	 */
	public PwaMerger(Vector<PwaSearchable> terms, boolean exclude) {
		this.terms=terms;					
		this.minIndex=-2;
		this.exclude=exclude;
	}
		
	/**
	 * Indicates if has more documents
	 * @return true if has more documents; false otherwise
	 */
	public boolean hasDoc() {
		return (minIndex>-1);
	}	
	
	/**
	 * Move to next document
	 * @return true if has more documents; false otherwise
	 */
	public boolean next() throws IOException {		
		if (minIndex==-1) {
			return false;
		}	
		else if (minIndex==-2) {
			for (int i=0;i<terms.size();i++) {
				terms.get(i).next();				
			}
		}		
		else { // next() for all terms with the same doc			
			for (int i=0;i<terms.size();i++) {
				if (i!=minIndex && terms.get(i).hasDoc() && terms.get(i).doc()==terms.get(minIndex).doc()) { // next for all with the same doc as minIndex 
					terms.get(i).next();
				}							
			}		
			terms.get(minIndex).next();
		}
		
		// find min doc					
		minIndex=-1;
		for (int i=0;i<terms.size();i++) {
			if (terms.get(i).hasDoc() && (minIndex==-1 || terms.get(i).doc()<terms.get(minIndex).doc())) {				
				minIndex=i;				
			}
		}
		return (minIndex!=-1);			
	}
	
	/**
	 * Skips to document @doc or superior
	 * @param doc document id
	 * @return true if skip to document @doc or superior; false otherwise
	 */
	public boolean skipTo(int doc) throws IOException {		
		if (hasDoc() && doc()>doc) { // to speedup
			return true;
		}
		
		minIndex=-1;
		for (int i=0;i<terms.size();i++) {
			if (terms.get(i).skipTo(doc)) {		
				if (minIndex==-1 || terms.get(i).doc()<terms.get(minIndex).doc()) {
					minIndex=i;
				}
			}
		}
		
		return (minIndex!=-1);	
	}
	
	/**
	 * Skips to document @doc or superior from start
	 * @param doc document id
	 * @return true if skip to document @doc or superior from start; false otherwise
	 */
	public boolean skipToFromStart(int doc) throws IOException {
		minIndex=-1;
		for (int i=0;i<terms.size();i++) {
			if (terms.get(i).skipToFromStart(doc)) {		
				if (minIndex==-1 || terms.get(i).doc()<terms.get(minIndex).doc()) {
					minIndex=i;
				}
			}
		}
		
		return (minIndex!=-1);	
	}
	
	/**
	 * Get document id
	 * @return document id
	 */
	public int doc() {
		return terms.get(minIndex).doc();
	}
	
	/**
	 * Collect ranking features from this document
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
	 * Set ranking features with empty values for this document
	 * @param doc document id
	 * @param collector features collector
	 */
	public void collectEmptyFeatures(int doc, PwaRawFeatureCollector collector) throws IOException {
		for (int i=0;i<terms.size();i++) {	
			terms.get(i).collectEmptyFeatures(doc,collector);
		}
	}
	
	/**
	 * Indicates if this document should be excluded
	 * @return true if this document should be excluded; false otherwise
	 */
	public boolean isExclude() {
		return exclude;
	}	
}
