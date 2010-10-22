package org.apache.lucene.search;

import java.io.IOException;


/**
 * Common methods for ArquivoWebJoinTwo classes	 
 * @author Miguel Costa
 */
abstract class PwaAbstractJoinTwo extends PwaSearchable {
	
	protected PwaSearchable term1;
	protected PwaSearchable term2;
	protected boolean hasDoc;	
	
	
	/**
	 * Auxiliary to move to next document
	 * @return true if has more documents; false otherwise
	 */
	protected abstract boolean nextAux() throws IOException;	
	
	/**
	 * Move to next document
	 * @return true if has more documents; false otherwise
	 */
	public boolean next() throws IOException {
		hasDoc=nextAux();
		return hasDoc;			
	}
	
	/**
	 * Indicates if has more documents
	 * @return true if has more documents; false otherwise	
	 */
	public boolean hasDoc() {
		return hasDoc;
	}
	
	/**
	 * Get document id
	 * @return document id
	 */
	public int doc() {
		return term1.doc();			
	}
	
	/**
	 * Indicates if has more documents
	 * @return true if has more documents; false otherwise	
	 */
	public boolean skipTo(int doc) throws IOException {
		hasDoc=skipToAux(doc);
		return hasDoc;
	}
	
	/**
	 * Auxiliary to indicate if has more documents
	 * @return true if has more documents; false otherwise	
	 */
	private boolean skipToAux(int doc) throws IOException { 	
		if (term2==null) {				
			return term1.skipTo(doc); 
		}
			
		if (!term1.skipTo(doc)) {				
			return false;
		}
		if (!term2.skipTo(doc)) {				
			return false;
		}
		do {					
			if (term1.doc()<term2.doc()) {
				if (!term1.skipTo(term2.doc())) {						
					return false;				
				}
			}
			else if (term1.doc()>term2.doc()) {
				if (!term2.skipTo(term1.doc())) {				
					return false;				
				}
			}	
		}
		while (term1.doc()!=term2.doc());
		
		return true;
	}

	/**
	 * Skip to document @doc or superior from start
	 * @return true if skip to document @doc or superior from start; false otherwise
	 */
	public boolean skipToFromStart(int doc) throws IOException {
		hasDoc=skipToFromStartAux(doc);
		return hasDoc;
	}
	
	/**
	 * Auxiliary to skip to document @doc or superior from start
	 * @return true if skip to document @doc or superior from start; false otherwise
	 */
	private boolean skipToFromStartAux(int doc) throws IOException { 	
		if (term2==null) {				
			return term1.skipToFromStart(doc);				
		}
			
		if (!term1.skipToFromStart(doc)) {				
			return false;
		}
		if (!term2.skipToFromStart(doc)) {				
			return false;
		}
		do {					
			if (term1.doc()<term2.doc()) {
				if (!term1.skipTo(term2.doc())) {						
					return false;				
				}
			}
			else if (term1.doc()>term2.doc()) {
				if (!term2.skipTo(term1.doc())) {						
					return false;				
				}
			}	
		}
		while (term1.doc()!=term2.doc());
		
		return true;		
	}
	
	/**
	 * Collect ranking features
	 * @param doc document id
	 * @param collector features collector 
	 */
	public void collectFeatures(int doc, PwaRawFeatureCollector collector) throws IOException {						
		term1.collectFeatures(doc,collector);
		if (term2!=null) {
			term2.collectFeatures(doc,collector);
		}
	}
	
	/**
	 * Set ranking features with empty values
	 * @param doc document id
	 * @param collector features collector
	 */
	public void collectEmptyFeatures(int doc, PwaRawFeatureCollector collector) throws IOException {
		term1.collectEmptyFeatures(doc,collector);
		if (term2!=null) {
			term2.collectEmptyFeatures(doc,collector);
		}
	}			
}