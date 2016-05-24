package org.apache.lucene.search;

import java.io.IOException;
import java.util.*;

import org.apache.lucene.index.*;
import org.apache.lucene.search.caches.PwaIndexStats;


/**
 * Stores all raw features for ranking
 * @author Miguel Costa
 */
public class PwaRawFeatureCollector {
	
	private final static String TF_STRING="/tf";
	private final static String IDF_STRING="/idf";
	private final static String LENGTH_STRING="/length";
	private final static String TERM_STRING="/term";
	
	
	private HashMap map;
	private int nDocs;
	private PwaIndexStats indexstats;
	
	
	/**
	 * Constructor
	 * @param reader index reader
	 * @throws IOException
	 */
	public PwaRawFeatureCollector(IndexReader reader) throws IOException {
		map=new HashMap();
		nDocs=reader.numDocs(); 
		indexstats=PwaIndexStats.getInstance(reader);
	}
	
	/**
	 * Add term aggregated per field
	 * @param term query term 
	 * @param tf term frequency
	 * @param idf inverse document frequency
	 * @param length field length measured in terms
	 */
	public void addTerm(Term term, int tf, int idf, int length) throws IOException {
		// set tf
		Vector<Integer> vec=(Vector<Integer>)map.get(term.field()+TF_STRING);
		if (vec==null) {
			vec=new Vector<Integer>();
		}
		vec.add(tf);
		map.put(term.field()+TF_STRING, vec);

		// set idf
		vec=(Vector<Integer>)map.get(term.field()+IDF_STRING);
		if (vec==null) {
			vec=new Vector<Integer>();
		}
		vec.add(idf);
		map.put(term.field()+IDF_STRING, vec);
		
		// set length	
		Integer i=(Integer)map.get(term.field()+LENGTH_STRING);
		if (i!=null && i!=0) {
			if (i!=length && length!=0) { // sanity check
				throw new IOException("Different lengths for field. This can not occur.");
			}			
		}
		else {	
			map.put(term.field()+LENGTH_STRING, length);				
		}				
		
		// set term text for debug
		Vector<String> vecs=(Vector<String>)map.get(term.field()+TERM_STRING);
		if (vecs==null) {
			vecs=new Vector<String>();
		}
		vecs.add(term.text());
		map.put(term.field()+TERM_STRING, vecs);
	}	
	
	/**
	 * Get term frequency
	 * @param field field
	 * @return
	 */
	public Vector<Integer> getFieldTfs(String field) {
		return (Vector<Integer>)map.get(field+TF_STRING);
	}
	
	/**
	 * Get inverse document frequency
	 * @param field field
	 * @return
	 */
	public Vector<Integer> getFieldIdfs(String field) {
		return (Vector<Integer>)map.get(field+IDF_STRING);
	}
	
	/**
	 * Get query terms text
	 * @param field field
	 * @return
	 */	
	public Vector<String> getFieldTermsText(String field) {
		return (Vector<String>)map.get(field+TERM_STRING);
	}
	
	/**
	 * Get field length measured in terms
	 * @param field field
	 * @return
	 */
	public int getFieldLength(String field) {	
		return (Integer)map.get(field+LENGTH_STRING);
	}
	
	/**
	 * Get number of documents
	 * @return
	 */
	public int getNumDocs() {
		return nDocs;
	}
	
	/**
	 * Get number of query terms
	 * @return
	 */
	public int getNumQueryTerms() {
		return getFieldTermsText(PwaIndexStats.FIELDS[0]).size();
	}
	
	/**
	 * Get average length of terms in field
	 * @param field field
	 * @return
	 */
	public double getFieldAvgLength(String field) {
		return indexstats.getFieldAvgLength(field);
	}
	
	/**
	 * Indicates if this collector is empty
	 * @return
	 */
	public boolean isEmpty() {
		return (map.size()==0);
	}
}
