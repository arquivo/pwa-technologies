package org.apache.lucene.search;

import java.io.IOException;
import java.util.Vector;


/**
 * Join posting lists of query terms for one field
 * @author Miguel Costa
 * 
 * @see based on http://old.di.fc.ul.pt/sobre/documentos/tech-reports/04-17.pdf, section 3.2, page 46, Table 3.1
 */
public class PwaJoiner extends PwaSearchable {
	
	protected PwaSearchable join;	
	protected boolean hasDoc;	
		
	/**
	 * Constructor
	 */
	protected PwaJoiner() {		
	}
	
	/**
	 * Constructor
	 * @param terms terms/mergers/joiners 
	 * @note the terms to exclude (not) must follow all the others in the vector
	 */
	public PwaJoiner(Vector<PwaSearchable> terms) throws IOException {						
		// join
		join=new PwaJoinTwoAND(terms.get(0));
		for (int i=1;i<terms.size();i++) {
			if (join.isExclude() && terms.get(i).isExclude()) { // if all to exclude then UNION
				Vector<PwaSearchable> termsUNION=new Vector<PwaSearchable>();
				termsUNION.add(join);
				termsUNION.add(terms.get(i));				
				join=new PwaMerger(termsUNION,true);			
			}
			else if (!join.isExclude() && !terms.get(i).isExclude()) { // if all !exclude then INTERSECT				
				join=new PwaJoinTwoAND(join,terms.get(i));					
			}
			else { // if one to exclude and the other not then EXCLUDE
				join=new PwaJoinTwoNOT(join,terms.get(i));			
			}		
		}		
				
		this.hasDoc=false;
	}
		
	/**
	 * Indicates if has more documents
	 * @return true if has more documents; false otherwise	
	 */
	public boolean hasDoc() {
		return hasDoc;
	}
	
	/**
	 * Move to next document
	 * @return true if has more documents; false otherwise
	 */
	public boolean next() throws IOException {	
		hasDoc=join.next();
		return hasDoc;
	}
	
	/**
	 * Skip to document @doc or superior
	 * @return true if skip to document @doc or superior; false otherwise
	 */
	public boolean skipTo(int doc) throws IOException {		
		hasDoc=join.skipTo(doc);
		return hasDoc;
	}
	
	/**
	 * Skip to document @doc or superior from start
	 * @return true if skip to document @doc or superior from start; false otherwise
	 */
	public boolean skipToFromStart(int doc) throws IOException {
		hasDoc=join.skipToFromStart(doc);
		return hasDoc;				
	}
	
	/**
	 * Get document id
	 * @return document id
	 */
	public int doc() {
		return join.doc();
	}
	
	/**
	 * Collect ranking features
	 * @param doc document id
	 * @param collector features collector 
	 */
	public void collectFeatures(int doc, PwaRawFeatureCollector collector) throws IOException {
		join.collectFeatures(doc, collector);			
	}
	
	/**
	 * Set ranking features with empty values
	 * @param doc document id
	 * @param collector features collector
	 */
	public void collectEmptyFeatures(int doc, PwaRawFeatureCollector collector) throws IOException {
		join.collectEmptyFeatures(doc, collector);
	}
		
	/**
	 * Indicates if this document should be excluded
	 * @return true if this document should be excluded; false otherwise
	 */
	public boolean isExclude() {	
		throw new RuntimeException("this methods should not be called!");
	}				
}
