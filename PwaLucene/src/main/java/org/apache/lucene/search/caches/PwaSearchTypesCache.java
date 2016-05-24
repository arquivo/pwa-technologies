package org.apache.lucene.search.caches;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PwaExtraTerm;
import org.apache.lucene.search.PwaMerger;
import org.apache.lucene.search.PwaSearchable;

import java.io.IOException;
import java.util.BitSet;
import java.util.Vector;


/**
 * Cache searchable types of documents 
 * @author Miguel Costa
 * @deprecated  as of release 1.14; It is not used anymore because not searchable types are pruned from posting lists
 */
public class PwaSearchTypesCache implements PwaICache {

	protected static BitSet validDocs; // bitset indicating the documents with one of the searched types
	private static Object lockObj=new Object();
	private static String fieldName="type";

	
	/**
	 * Constructor
	 * @param searchable
	 * @param reader
	 * @param types searched types
	 * @throws IOException
	 */
	public PwaSearchTypesCache(IndexReader reader, String[] types) throws IOException {	
		if (validDocs!=null) {
			return;
		}

		// load cache once		
		synchronized(lockObj) {					
			if (validDocs!=null) {
				return;
			}
			System.out.println("Loading searched types to RAM at "+this.getClass().getSimpleName()+" class.");
			
			validDocs=new BitSet(reader.maxDoc());	
		
			// set merger for type terms
			Vector<PwaSearchable> vecTermsAux=new Vector<PwaSearchable>();
			for (int i=0;i<types.length;i++) {
				vecTermsAux.add(new PwaExtraTerm(new Term(fieldName,types[i]),reader));  
			}			
			PwaMerger merger=new PwaMerger(vecTermsAux, false);
			
			while (merger.next()) {
				validDocs.set(merger.doc(), true);
			}			
		}
		System.out.println("Loading searched types to RAM at "+this.getClass().getSimpleName()+" class ended.");
	}
		
	
	/**
	 * Get field name cached
	 * @return
	 */
	public String getFieldName() {
		return fieldName;
	}	
	
	/**
	 * Indicates if document has a searchable type
	 * @param doc document id
	 * @return
	 */
	public Object getValue(int doc) { 	
		return Boolean.valueOf(validDocs.get(doc));
	}
}
