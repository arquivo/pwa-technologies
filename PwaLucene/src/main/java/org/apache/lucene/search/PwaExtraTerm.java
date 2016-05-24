package org.apache.lucene.search;

import java.io.IOException;

import org.apache.lucene.index.*;


/**
 * Read data associated to special terms (e.g. DOCNUM, type)
 * @author Miguel Costa
 */
public class PwaExtraTerm extends PwaTermCommon {
	
	/**
	 * Constructor
	 * @param term term
	 * @param reader index reader
	 * @throws IOException
	 */
	public PwaExtraTerm(Term term, IndexReader reader) throws IOException {
		super(term,reader);
	}
}
