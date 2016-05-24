package org.apache.lucene.search.caches;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

import org.apache.lucene.index.IndexReader;


/**
* Caches stopwords to be ignored
* @author Miguel Costa
*/
public class PwaStopwords {

	public final static String CACHE_FILENAME="stopwords.cache";
	
	private static Object lockObj=new Object();		
	private static PwaStopwords instance=null; // singleton class
	private static HashSet<String> stopwords=null; 
	
	
	/**
	 * Constructor
	 * @param reader index reader
	 * @throws IOException
	 */
	private PwaStopwords(IndexReader reader) throws IOException {
				
		System.out.println("Loading stopwords to RAM at "+this.getClass().getSimpleName()+" class.");
		
		stopwords=new HashSet<String>();				
		String fileDir=reader.directory().toString().substring(reader.directory().toString().indexOf('@')+1);
		BufferedReader br = new BufferedReader(new FileReader(new File(fileDir,CACHE_FILENAME)));
		String line;
		int nfields=1;		
		
		while ( ( line = br.readLine() ) != null ) {				
			String parts[] = line.split( "\\s" );			
			
			if (parts.length!=nfields) { 
				throw new IOException("ERROR: wrong number of fields from stopwords.");
			}
						
			for (int i=0;i<PwaIndexStats.FIELDS.length;i++) {
				stopwords.add(PwaIndexStats.FIELDS[i]+" "+parts[0]);
			}			
		}			
		br.close();
		
		System.out.println("Loading stopwords to RAM at "+this.getClass().getSimpleName()+" class ended.");
	}
	
	/**
	 * Get instance
	 * @param reader index reader
	 * @return
	 * @throws IOException
	 */
	public static PwaStopwords getInstance(IndexReader reader) throws IOException {
		if (instance!=null) {
			return instance;
		}
		
		synchronized(lockObj) {
			if (instance!=null) {
				return instance;
			}
			instance=new PwaStopwords(reader);
		}
		return instance;
	}
	
	/**
	 * Indicates if the term is a stopword
	 * @param field term field
	 * @param text term text
	 * @return
	 */
	public boolean contains(String field, String text) {
		if (text.length()==1 && !Character.isDigit(text.charAt(0))) { // all characters that are not digits are stopwords
			return true;
		}
		return stopwords.contains(field+" "+text);
	}	
}
