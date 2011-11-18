package org.apache.lucene.search.memcached;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.MapFieldSelector;
import org.apache.lucene.search.features.PwaIRankingFunction;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
//import org.apache.lucene.util.Base32;
import org.apache.commons.codec.binary.Base64;

import java.io.*;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * Load index into database
 * @author Miguel Costa
 */
public class MemcachedTransactions {

	// mime sub-stypes
	private final static String SUBTYPES[]={"html","plain","pdf","postscript","xml","x-shockwave-flash","xhtml+xml","sgml","msword","mspowerpoint","vnd","vnd.ms-powerpoint","rtf","richtext"};		
	private final static long DUMP_FACTOR=10000000;
	private final static String COLLECTION_STORED="STORED";
	public final static String MAX_VERSIONS="MAX_VERSIONS";
	public final static String MAX_SPAN="MAX_SPAN";
	
	
    private static SimpleDateFormat dformat=new SimpleDateFormat("yyyyMMdd");	
    
	private static MessageDigest md = null;
    
	/**
	 * Constructor
	 */
	public MemcachedTransactions() {				
	}
	
	
	/**
	 * Load database with urls' timestamps
	 * @param reader index reader
     * @param addresses memcached servers addresses
	 * @param collectionKey collection identifier
	 * @throws IOException
	 */	
	public static void load(IndexReader reader, String addresses, String collectionId) throws IOException {
		Memcached cache=new Memcached(addresses);
		// check if it this collection is already stored in memcached		
		
		String value=(String)cache.get(collectionId);	
		if (value!=null) {
			System.out.println("Collection is already stored in memcached.");
			cache.close();
			return;
		}
		cache.set(collectionId,COLLECTION_STORED);	
		Integer maxVersions=(Integer)cache.get(MAX_VERSIONS);
		if (maxVersions==null) {
			maxVersions=1;
		}
		Integer maxSpan=(Integer)cache.get(MAX_SPAN);
		if (maxSpan==null) {
			maxSpan=0;
		}
		
		Document doc=null;	      
		long count=0;
		long countWrongType=0;
		long countDynamic=0;		
		
		// initialize valid extensions
		Hashtable<String,Boolean> validExtensions=new Hashtable<String,Boolean>();			
		for (int i=0; i<SUBTYPES.length; i++) {
			validExtensions.put(SUBTYPES[i], true);				
		}				

		// read index
		System.out.println("Reading index with "+reader.maxDoc()+" documents ...");		
		for (int i=0;i<reader.maxDoc();i++) {																																																				      int idate=-1;
			String url=null;
			String subtype=null;

			doc = reader.document(i, new MapFieldSelector(new String[]{"date","url","subType"}));			
			Enumeration e = doc.fields();
			while (e.hasMoreElements()) {
			   Field field = (Field)e.nextElement();			   			    
			   if (field.name().equals("date")) {
				   idate=stringdateToInt(field.stringValue());				   
			   }
			   else if (field.name().equals("url")) {
				   url=field.stringValue();
			   }
			   else if (field.name().equals("subType")) {
				   subtype=field.stringValue();
			   }
			   else {
				   throw new IOException("Wrong field read.");
			   }
			}
							
			//System.out.println("url: "+ url+" date:"+ldate+" subtype:"+subtype);			
			if (validExtensions.get(subtype)==null) {
				countWrongType++;				
			}
			else if (url.indexOf('?')!=-1) {
				countDynamic++;
			}
			else { // store in cache								
			    url=getUrlKey(url);
			    			    
				try {
				    UrlRow row=cache.getRow(url);				    
					if (row==null) {						
						row=new UrlRow(1,idate,idate);
						cache.addRow(url,row);								
						count++;
						if (count%DUMP_FACTOR==0) {
							System.out.println("Stored "+count+" urls.");							
						}
					}
					else {					       
						int minDate=(row.getMin()<idate) ? row.getMin() : idate;
						int maxDate=(row.getMax()>idate) ? row.getMax() : idate;	
						row=new UrlRow(row.getNVersions()+1,minDate,maxDate);
						cache.replaceRow(url,row);		
								
						// set maxVersions and maxSpan
						if (row.getNVersions()>maxVersions) {
							maxVersions=row.getNVersions();
						}			
						long lMaxDate=intToLongdate(maxDate);
						long lMinDate=intToLongdate(minDate);
						float span=(lMaxDate-lMinDate)/PwaIRankingFunction.DAY_MILLISEC;
						if (span>maxSpan) {
							maxSpan=(int)span;
						}
					}
				} 
				catch (IllegalArgumentException ex) { // "Key is too long (maxlen = 250)" 
					System.err.println(ex.getMessage());
				}					
			}
		} 				
		
		// store data			
		cache.set(MAX_VERSIONS,maxVersions);
		cache.set(MAX_SPAN,maxSpan);
					
		// store in database				    			
		System.out.println("Stored "+count+" urls in memcached.");
		System.out.println(countWrongType+" urls with wrong mime type filtered.");
		System.out.println(countDynamic+" dynamic urls filtered.");
		System.out.println(maxVersions+" is the maximum number of versions."); 
		System.out.println(maxSpan+" is the maximum span between versions.");
		cache.close();
	}	
	
	
	/**
	 * Get url key
	 * @param url URL string
	 * @return
	 */
	public static String getUrlKey(String url) throws IOException {	
		if (md==null) {
			try {
				md = MessageDigest.getInstance("MD5");
			}
			catch (NoSuchAlgorithmException e) {
				throw new IOException("Failed to get md5 digester: " + e.getMessage());
			}
		}
		
		return new String(Base64.encodeBase64(md.digest(url.getBytes()))); // base64 of a md5 digest
	}
	
	/**
	 * Convert date in string format from index to a 4 bytes integer
	 * @param date
	 * @return
	 */
	public static int stringdateToInt(String date) {			
		long ldate=Long.parseLong(date)*1000;	
		String daux=dformat.format(new Date(ldate));
		return Integer.parseInt(daux); // only 4 bytes to cache
	}
	
	/**
	 * Convert date from a 4 bytes integer to a 8 bytes long
	 * @param date
	 * @return
	 */
	public static long intToLongdate(int date) throws IOException {	
		try {
			return dformat.parse(""+date).getTime();
		}
		catch (ParseException e) {
			throw new IOException(e.getMessage());
		}		
	}
	
	/**
	 * Main
	 * @param args arguments
	 */
	public static void main(String[] args) throws Exception {							
		String usage="usage: load [index path] [address1=127.0.0.1:8091] [address2] ... [addressn]";
		
		if (args.length<3) {
			System.out.println(usage);
			System.exit(0);
		}
		
		if (args[0].equals("load")) {
			Directory idx = FSDirectory.getDirectory(args[1], false);
			IndexReader reader = IndexReader.open(idx);

			String addresses=new String();
			for (int i=2;i<args.length;i++) {
				addresses+=" "+args[i];
			}
			
			MemcachedTransactions.load(reader,addresses,args[1]);
			reader.close();			
		}
		else {
			System.out.println(usage);
		}		
	}
}
