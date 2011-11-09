package org.apache.lucene.search.caches;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Cache documents' timestamps
 * @author Miguel Costa
 */
public class PwaDateCache implements PwaICache {

	protected static long timestamps[]; // timestamp per document cached
	private static Object lockObj=new Object();
	private static String fieldName="date";
	private static SimpleDateFormat dformat=null;
	private static long minTimestamp;
	private static long maxTimestamp;
	
	
//private static IndexReader reader=null; // TODO remove
	
	
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
		
//this.reader=reader; // TODO remove		

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
								if (timestamps[termDocs.doc()]<minTimestamp) {
									minTimestamp=timestamps[termDocs.doc()];
								}
								if (timestamps[termDocs.doc()]>maxTimestamp) {
									maxTimestamp=timestamps[termDocs.doc()];
								}
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
			
			// initialize date format - millisec granularity
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
	
	/**
	 * Get minimum timestamp 
	 * @return minimum timestamp from collection
	 */
	public long getMinTimestamp() {
		return minTimestamp;
	}
	
	/**
	 * Get maximum timestamp 
	 * @return maximum timestamp from collection
	 */
	public long getMaxTimestamp() {
		return maxTimestamp;
	}
	
	
	
	/**
	 * Write a file with all documents' timestamps from index and the frequency they occur
	 * @param reader index reader
	 * @param output filename
	 * @throws IOException
	 */
	public static void writeCache(IndexReader reader, String outFilename) throws IOException {		
		Document doc=null;
		Date d=null;
		String day=null;
		Integer times=null;
		HashMap<String,Integer> daysMap=new HashMap<String,Integer>();
		
		// initialize date format - day granularity
		dformat = new SimpleDateFormat("yyyyMMdd");
		dformat.setTimeZone(TimeZone.getTimeZone("GMT"));
		
		for (int i=0;i<reader.maxDoc();i++) {																							
			// add new document with field values
			doc = reader.document(i, new MapFieldSelector(new String[]{"date"}));																																																				
			long date=-1;
							
			Enumeration e = doc.fields();
			while (e.hasMoreElements()) {
			   Field field = (Field)e.nextElement();
			   if (field.name().equals("date")) {
				   date=Long.parseLong(field.stringValue());		
			   }
			   else {
				   throw new IOException("Wrong field read.");
			   }
			}
			
			d=new Date(date*1000);	
			day=dformat.format(d);
			times=daysMap.get(day);
			if (times==null) {
				times=new Integer(1);
			}
			else {
				times++;
			}
			daysMap.put(day,times);
		} 		
		
		TreeMap<String,Integer> treeMap = new TreeMap<String,Integer>(daysMap); // sort entries by key
		PrintWriter pw=new PrintWriter(new File(outFilename));					
		for(Map.Entry<String,Integer> entry : treeMap.entrySet()) {
			pw.println(entry.getKey()+" "+entry.getValue());			
		}
		pw.close();		
	}

	
	
	/**
	 * Main
	 * @param args arguments
	 */
	public static void main(String[] args) throws Exception {	
						
		String usage="usage: create [index path] [output filename] (to show all documents' timestamps)";
		
		if (args.length!=3) {
			System.out.println(usage);
			System.exit(0);
		}
		
		if (args[0].equals("create")) {
			Directory idx = FSDirectory.getDirectory(args[1], false);
			org.apache.lucene.index.IndexReader reader=IndexReader.open(idx);
			writeCache(reader,args[2]);
			reader.close();			
		}
		else {
			System.out.println(usage);
		}		
	}
}
