package org.apache.lucene.search;

import java.util.Vector;
import java.io.IOException;

import org.apache.lucene.index.*;


/**
 * Equal to PwaTerm but with a minimum term frequency's threshold
 * @author Miguel Costa
 */
public class PwaTermLimited extends PwaTerm {
		
	private int minTf; // minimum term frequency's threshold
	
	
	/**
	 * Constructor
	 * @param term term
	 * @param reader index reader
	 * @param minTf minimum tf accepted
	 * @throws IOException
	 */
	public PwaTermLimited(Term term, IndexReader reader, int minTf) throws IOException {								
		super(term,reader);		
		this.minTf=minTf;
	}
	
	/**
	 * Move to next document
	 * @return true if has more documents; false otherwise
	 */
	public boolean next() throws IOException {
		while (super.next()) {
			if (tf()>=minTf) {
				return true;
			}			
		}
	    pointer=-1; // for PwaTermCommon to return false at hasDoc()
		return false;
	}
	
	/**
	 * Skips to document @doc or superior
	 * @param doc document id
	 * @return true if skip to document @doc or superior; false otherwise
	 */
	public boolean skipTo(int doc) throws IOException {
		if (super.skipTo(doc) && tf()>=minTf) {
			return true;					
		}
		while (super.next()) { // if the skipTo does not return a valid doc, then return the next one valid
			if (tf()>=minTf) {
				return true;
			}			
		}
		pointer=-1; // for PwaTermCommon to return false at hasDoc()
		return false;
	}
	
	/**
	 * Skips to document @doc or superior from start
	 * @param doc document id
	 * @return true if skip to document @doc or superior from start; false otherwise
	 */
	public boolean skipToFromStart(int doc) throws IOException {
		return super.skipToFromStart(doc); // it calls the skipTo of this class
	}
}
