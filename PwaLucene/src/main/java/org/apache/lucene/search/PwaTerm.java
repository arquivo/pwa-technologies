package org.apache.lucene.search;

import java.io.IOException;

import org.apache.lucene.index.*;
import org.apache.lucene.search.caches.PwaIndexStats;


/**
 * Reads and stores data associated to term/field
 * @author Miguel Costa
 */
public class PwaTerm extends PwaTermCommon {

	protected int idf;
	private int lengths[]; 
	private PhrasePositions ppos; // positions array
	
	
	/**
	 * Constructor
	 * @param term term
	 * @param reader index reader
	 * @throws IOException
	 */
	public PwaTerm(Term term, IndexReader reader) throws IOException {								
		super(term,reader);
						
		this.idf=reader.docFreq(term);				
		this.ppos=null;			
		this.lengths=PwaIndexStats.getInstance(reader).getFieldLengths(term.field());
	}
	
	/**
	 * Collect ranking features from this document
	 * @param doc document id
	 * @param collector features collector 
	 */
	public void collectFeatures(int doc, PwaRawFeatureCollector collector) throws IOException {		
		collector.addTerm(term(),tf(),idf(),length());		
	}	
	
	/**
	 * Set ranking features with empty values for this document
	 * @param doc document id
	 * @param collector features collector
	 */
	public void collectEmptyFeatures(int doc, PwaRawFeatureCollector collector) throws IOException {
		collector.addTerm(term(),0,idf(),length());
	}
		
	/**
	 * Get inverse document frequency
	 * @return inverse document frequency
	 */
	public int idf() {
		return idf;
	}			
	
	/**
	 * Get field length
	 * @note byte array to int conversion is only performed inside the method
	 */
	public int length() {		
		return lengths[doc()];		
	}
	
	
	/**
	 * Get positions of pair term/field of current document
	 * @param docJoined current document joined
	 * @return positions if exist, null otherwise
	 */
	public PwaPositions getPos(int docJoined) throws IOException {		
						
		if (!hasDoc() || docJoined!=doc()) {
			return null;
		}
		if (ppos==null) {
			TermPositions tpos = reader.termPositions(term);
			ppos=new PhrasePositions(tpos,-1);				
			
			while (ppos.next() && ppos.doc<doc()) {}; // jump to the right doc
		}
		else { // BUGFIX nutchwax 0000591
			if (ppos.doc<doc()) { // avoid to advance when the document is this
				while (ppos.next() && ppos.doc<doc()); // jump to the right doc
			}
		}		 				
		
		if (ppos.doc==doc()) {
			ppos.firstPosition(); // read frequency
			PwaPositions vpos=new PwaPositions();		
			vpos.add(ppos.position);
			while (ppos.nextPosition()) {
				vpos.add(ppos.position);
			}
								
			return vpos;
		}		
		
		return null; 
	}	
	
}
