package org.apache.lucene.search;

import java.io.IOException;


/**
 * Join two terms intersecting posting lists
 * @author Miguel Costa
 */	
class PwaJoinTwoAND extends PwaAbstractJoinTwo {
			
	/**
	 * Constructor
	 * @param term1 term
	 */
	public PwaJoinTwoAND(PwaSearchable term1) {
		this(term1,null);	
	}
	
	/**
	 * Constructor
	 * @param term1 term
	 * @param term2 term
	 */
	public PwaJoinTwoAND(PwaSearchable term1, PwaSearchable term2) {
		this.term1=term1;
		this.term2=term2;		
		this.hasDoc=false;
	}		
	
	/**
	 * Auxiliary to move to next document
	 * @return true if has more documents; false otherwise
	 */
	protected boolean nextAux() throws IOException {							
		if (term2==null) {
			return term1.next();
		}			
								
		if (!term1.next()) {
			return false;
		}
		if (!term2.next()) {
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
	 * Indicates if this document should be excluded
	 * @return true if this document should be excluded; false otherwise
	 */
	public boolean isExclude() { // is always !exclude except if has one term exclude
		return (term1.isExclude() && (term2==null || term2.isExclude()));
	}		
}