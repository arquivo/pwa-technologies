package org.apache.lucene.search;

import java.io.IOException;


/**
 * Join two terms intersecting posting lists to exclude the postings of the second term
 * @author Miguel Costa
 */	
class PwaJoinTwoNOT extends PwaAbstractJoinTwo {							
	
	/**
	 * Constructor
	 * @param term1 term
	 * @param term2 term
	 * @throws IOException
	 */
	public PwaJoinTwoNOT(PwaSearchable term1, PwaSearchable term2) throws IOException {
		if (!term1.isExclude()) { // term2 is always to exclude
			this.term1=term1;
			this.term2=term2;
		}
		else {
			this.term1=term2;
			this.term2=term1;
		}
		if (this.term1.isExclude() || !this.term2.isExclude()) {
			throw new IOException("sanity check failed");
		}
		this.term2.next(); // initialize term to exclude			
		this.hasDoc=false;	
	}		
	
	/**
	 * Auxiliary to move to next document
	 * @return true if has more documents; false otherwise
	 */
	protected boolean nextAux() throws IOException {					
		if (!term1.next()) {						
			return false;
		}					
		if (!term2.hasDoc()) { 							
			return true;
		}
		
		do { // exits when term1 < term2				
			if (term1.doc()==term2.doc()) {
				if (!term1.next()) {						
					return false;
				}					
			}				 
			if (term1.doc()>term2.doc()) {					
				if (!term2.skipTo(term1.doc())) {						
					return true;
				}
			}		
		}
		while (term1.doc()==term2.doc());			
		
		return true;
	}			
		
	/**
	 * Indicates if this document should be excluded
	 * @return true if this document should be excluded; false otherwise
	 */
	public boolean isExclude() { // has always one !exclude 
		return false;
	}		
}