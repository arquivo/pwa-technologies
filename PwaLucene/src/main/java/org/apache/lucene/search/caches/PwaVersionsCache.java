package org.apache.lucene.search.caches;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.BitSet;
import java.util.Enumeration;


/**
 * Caches digest difference and url radical id, indicating if it is a new version
 * @author Miguel Costa
 */
public abstract class PwaVersionsCache implements PwaICache {

	public final static String CACHE_FILENAME="versions.cache";
	
	protected static BitSet newVersion=null; // indicates if it is a new version comparing with the last one
	protected static long urlRadicalId[]=null; // indicates the radical/version group 
	private static Object lockObj=new Object();	

		
	/**
	 * Constructor
	 * @param searchable
	 * @param reader
	 * @throws IOException
	 */
	public PwaVersionsCache(IndexReader reader) throws IOException {				
		if (newVersion!=null) {
			return;
		}
			
		// load cache once		
		synchronized(lockObj) {
			if (newVersion!=null) {
				return;
			}
			System.out.println("Loading versions to RAM at "+this.getClass().getSimpleName()+" class.");
			
			newVersion=new BitSet(reader.maxDoc());				
			BitSet docSet=new BitSet(reader.maxDoc());
			urlRadicalId=new long[reader.maxDoc()];
			long urlRadicalCount=0;
			
			String fileDir=reader.directory().toString().substring(reader.directory().toString().indexOf('@')+1);
			BufferedReader br = new BufferedReader(new FileReader(new File(fileDir,CACHE_FILENAME)));
			String line;
			int nfields=5;
			String oldUrlRadical=null;
			String oldDigest=null;

			while ( ( line = br.readLine() ) != null ) {				
				String parts[] = line.split( "\\s" );			
				
				if (parts.length!=nfields) { 
					throw new IOException("ERROR: wrong number of fields.");
				}
				
				String urlRadical=parts[0];
				//String url=parts[1];
				//String date=parts[2];
				String digest=parts[3];
				int doc=Integer.parseInt(parts[4]);
				
				if (oldUrlRadical==null || !oldUrlRadical.equals(urlRadical) || (oldUrlRadical.equals(urlRadical) && !oldDigest.equals(digest))) {					
					newVersion.set(doc,true);	
				}
				else {
					newVersion.set(doc,false);
				}
				docSet.set(doc,true);
				
				if (oldUrlRadical==null || !oldUrlRadical.equals(urlRadical)) {
					urlRadicalCount++;
				}
				urlRadicalId[doc]=urlRadicalCount;
				
				oldUrlRadical=urlRadical;
				oldDigest=digest;													
			}					
			br.close();
					
			// sanity check - validate if all documents have timestamps assigned	
			for (int i=0;i<reader.maxDoc();i++) {
				if (docSet.get(i)==false) {
					throw new IOException("Versions not assigned to document "+i+" for a collection with "+reader.maxDoc()+" documents.");
				}
			}

			// free mem
			docSet=null;
			
			System.out.println("Loading versions to RAM at "+this.getClass().getSimpleName()+" class ended.");
		}		
	}
	
	
	/**
	 * Write a file with the fields of all the documents to disk to be sorted after
	 * @param reader index reader
	 * @throws IOException
	 */
	public static void writeCache(IndexReader reader) throws IOException {
		String fileDir=reader.directory().toString().substring(reader.directory().toString().indexOf('@')+1);
		PrintWriter pw=new PrintWriter(new File(fileDir,CACHE_FILENAME));
		Document doc=null;
		
		for (int i=0;i<reader.maxDoc();i++) {																								
			// add new document with field values
			doc = reader.document(i, new MapFieldSelector(new String[]{"date","digest","url"}));																																																				
			long date=-1;
			String digest=null;
			String url=null;
							
			Enumeration e = doc.fields();
			while (e.hasMoreElements()) {
			   Field field = (Field)e.nextElement();
			   if (field.name().equals("date")) {
				   date=Long.parseLong(field.stringValue());		
			   }
			   else if (field.name().equals("digest")) {
				   digest=field.stringValue();
			   }								   
			   else if (field.name().equals("url")) {
				   url=field.stringValue();
			   }
			   else {
				   throw new IOException("Wrong field read.");
			   }
			}
			
			pw.println(url+" "+date+" "+digest+" "+i);
		} 		
		pw.close();		
	}
	
	/**
	 * Main
	 * @param args arguments
	 */
	public static void main(String[] args) throws Exception {	
						
		String usage="usage: create [index path] (to create cache)";
		
		if (args.length!=2) {
			System.out.println(usage);
			System.exit(0);
		}
				
		if (args[0].equals("create")) {
			Directory idx = FSDirectory.getDirectory(args[1], false);
			org.apache.lucene.index.IndexReader reader=IndexReader.open(idx);
			writeCache(reader);
			reader.close();			
		}
		else {
			System.out.println(usage);
		}		
	}

}
