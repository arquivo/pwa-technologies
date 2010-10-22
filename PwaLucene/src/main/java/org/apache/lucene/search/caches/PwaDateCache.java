package org.apache.lucene.search.caches;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;


/**
 * Cache document timestamps
 * @author Miguel Costa
 */
public class PwaDateCache implements PwaCache {

	protected static long timestamps[]; // timestamp per document cached
	private static Object lockObj=new Object();
	private static String fieldName="date";
	private static SimpleDateFormat dformat=null;
	
	
	/**
	 * Constructor
	 * @param searchable documents stream 
	 * @param reader index reader
	 * @throws IOException
	 */
	public PwaDateCache(IndexReader reader) throws IOException {	
		if (timestamps!=null) {
			return;
		}

		// load cache once		
		synchronized(lockObj) {			
			if (timestamps!=null) {
				return;
			}
			System.out.println("Loading date index to RAM at "+this.getClass().getSimpleName()+" class.");
			
			timestamps=new long[reader.maxDoc()];		
			TermEnum enumerator = reader.terms(new Term(fieldName, ""));	     

			try {            
				if (enumerator.term()==null) {
					throw new IOException("No term found.");
				}

				TermDocs termDocs = reader.termDocs();
				try {                	            
					do {                	                
						Term term = enumerator.term();
						if (term!=null && term.field().equals(fieldName)) {                            
							termDocs.seek(enumerator.term());
							while (termDocs.next()) {	
								// sanity check - validate if timestamp is already assigned to this document
								if (timestamps[termDocs.doc()]!=0) {
									throw new IOException("Timestamp already assigned.");
								}
								// sanity check - validate if docid is smaller than the max docid
								if (termDocs.doc()>=reader.maxDoc()) {
									throw new IOException("Timestamp with invalid docid "+termDocs.doc()+", since max docid is "+reader.maxDoc()+".");
								}
								
								timestamps[termDocs.doc()]=Long.parseLong(enumerator.term().text());
							}                        
						} 
						else {
							break;
						}
					}
					while (enumerator.next());
				} 
				finally {
					termDocs.close();
				}
			} 
			finally {
				enumerator.close();
			}		

			// sanity check - validate if all documents have timestamps assigned			
			for (int i=0;i<timestamps.length;i++) {
				if (timestamps[i]==0) {
					throw new IOException("Timestamp not assigned.");
				}
			}			
			
			// initialize date format
			dformat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			dformat.setTimeZone(TimeZone.getTimeZone("GMT"));
		}	
		
		System.out.println("Loading date index to RAM at "+this.getClass().getSimpleName()+" class ended.");
	}
	
	/**
	 * Get field name cached
	 * @return field name cached
	 */
	public String getFieldName() {
		return "tstamp";
	}	
	
	/**
	 * Get timestamp from document
	 * @param doc document id
	 * @return timestamp from document
	 */
	public Object getValue(int doc) {
		Date d=new Date(timestamps[doc]*1000);				
		return dformat.format(d);
	}

	/**
	 * Get timestamp from document
	 * @param doc document id
	 * @return timestamp from document
	 */
	public long getTimestamp(int doc) {
		return timestamps[doc];
	}
	
}
