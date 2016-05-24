package org.apache.lucene.search.caches;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.util.Base32;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;
import java.util.ArrayList;


/**
 * Identify and cache pages that should be discarded
 * @author Miguel Costa
 * 
 * @note this class can receive a list of errors and redirects from crawler or test all documents
 */
public class PwaBlacklistCache implements PwaICache {

	private final static String CACHE_FILENAME="blacklist.cache";
	//private final static String ERRORS_FILENAME="errors.urls";
	private final static int PAGE_CODE_NOT_FOUND=404;
	private final static int INDEX_ID=0;
	private final static int INC_LOG=100000;
	//private final static long MAX_TIMEOUT_THREAD=300000; // millisec until timeout of thread
	private static Object lockObj=new Object();	
	private static BitSet docBlackList=null; // blacklist of documents	
	private IndexReader reader;
	private Searcher searcher;	
	private ArrayList<Integer>[] idsThread; // array of vectors, with each vector containing the ids for each thread
		
	
	
	/**
	 * Constructor	 
	 * @param reader index reader	 
	 * @param searcher index searcher
	 * @param blacklistDir blacklist directory 
	 * @throws IOException
	 */
	public PwaBlacklistCache(IndexReader reader, Searcher searcher, File blacklistFile) throws IOException {
		this(reader, blacklistFile);
		this.searcher=searcher;
	}
	
	/**
	 * Constructor
	 * @param reader index reader
	 * @param blacklistDir blacklist directory 
	 * @throws IOException
	 */
	public PwaBlacklistCache(IndexReader reader, File blacklistFile) throws IOException {		
		if (docBlackList!=null) {
			return;
		}

		// load cache once		
		synchronized(lockObj) {
			if (docBlackList!=null) {
				return;
			}
			this.reader=reader;			
			
			System.out.println("Loading blacklist to RAM at "+this.getClass().getSimpleName()+" class. The file is at "+blacklistFile.getAbsolutePath());			
			docBlackList=new BitSet(reader.maxDoc());	
												
			if (blacklistFile==null) {
				String fileDir=reader.directory().toString().substring(reader.directory().toString().indexOf('@')+1);
				blacklistFile=new File(fileDir,CACHE_FILENAME);
			}	
			BufferedReader br = new BufferedReader(new FileReader(blacklistFile));					
			String line;
			int nfields=1;
			
			while ( ( line = br.readLine() ) != null ) {				
				String parts[] = line.split( "\\s" );			
				
				if (parts.length!=nfields) { 
					throw new IOException("ERROR: wrong number of fields.");
				}
							
				int doc=Integer.parseInt(parts[0]);
				docBlackList.set(doc,true);															
			}			
			br.close();
			
			System.out.println("Loading blacklist to RAM at "+this.getClass().getSimpleName()+" class ended.");
		}			
	}
			
	/**
	 * Get field name
	 * @return field name
	 */
	public String getFieldName() {
		return "blacklist";
	}
	
	/**
	 * Get value from cache
	 * @param ocument identifier
	 * @return value from cache
	 */
	public Object getValue(int doc) {
		return docBlackList.get(doc);
	}		
	
	/**
	 * Indicates if the document is valid
	 * @param doc document identifier
	 * @return true if the document is valid, false otherwise
	 */
	public boolean isValid(int doc) {
		return !docBlackList.get(doc);
	}
	
	
		
	/**	 
	 * Write a file with the ids of documents not archived
	 * @param reader index reader
	 * @param urlBase url to concat
	 * @param nThreads number of threads
	 * @param firstDoc first document
	 * @param lastDoc last document
	 * @param errorsFile errors filename with HTTP error and URL, or null
	 * @throws IOException
	 */
	public void writeCache(String urlBase, int nThreads, int firstDoc, int lastDoc, String errorsFile) throws IOException {					

		// identify the urls with problems
		identifyIdsFromUrls(nThreads,errorsFile);
		
		if (lastDoc==-1) {
			lastDoc=reader.maxDoc();
		}
		else {						
			if (lastDoc>reader.maxDoc()) {
				lastDoc=reader.maxDoc();
			}
		}
		docBlackList=new BitSet(reader.maxDoc()); // documents to eliminate		
		
		ProcessThread thr[]=new ProcessThread[nThreads];
		for (int i=0;i<nThreads;i++) { // for all threads process data					
			thr[i] = null;
			thr[i] = new ProcessThread(i,firstDoc,lastDoc,urlBase);
			thr[i].setPriority(Thread.MAX_PRIORITY);
			thr[i].start();
		}
		
		for (int i=0;i<nThreads;i++) { // for all threads process data					
			if (thr[i]!=null) {
				try {
					//thr[i].join(MAX_TIMEOUT_THREAD);
					thr[i].join();
					//thr[i].stop();					
				}
				catch (InterruptedException e) { 
					throw new IOException("ERROR: interrupt for thread "+(i+1)+".");
				}
				thr[i]=null;
			}
		}
		
			
		// save list to file
		writeList(firstDoc, lastDoc);
	}

	/**
	 * Save blacklist to file
	 * @param firstDoc first document to save
	 * @param lasttDoc last document to save
	 */
	private void writeList(int firstDoc, int lastDoc) throws IOException {
		String fileDir=reader.directory().toString().substring(reader.directory().toString().indexOf('@')+1);		
		PrintWriter pw=new PrintWriter(new File(fileDir,CACHE_FILENAME));
		for (int i=firstDoc;i<lastDoc;i++) {
			if (docBlackList.get(i)) {
				pw.println(""+i);
			}
		}
		pw.flush();
		pw.close();	
	}

	/**
	 * Identify ids of documents with errors given a list of urls	 
	 * @param nThreads number of threads
	 * @param errorsFile errors filename with HTTP error and URL, or null
	 */
	private void identifyIdsFromUrls(int nThreads, String errorsFile) throws IOException {			
		idsThread=new ArrayList[nThreads];		
		for (int i=0;i<nThreads;i++) {
			idsThread[i]=new ArrayList<Integer>();
		}
								
		if (errorsFile!=null) { // set id for each url in error file
			BufferedReader br = new BufferedReader(new FileReader(new File(errorsFile)));
			String line;
			int nfields=2;		
			MessageDigest md = null;	
			try {
				md = MessageDigest.getInstance("MD5");
			}
			catch (NoSuchAlgorithmException e) {
				throw new IOException("Failed to get md5 digester: " + e.getMessage());
			}	    	   
		
			while ( ( line = br.readLine() ) != null ) {				
				String parts[] = line.split( "\\s" );			
			
				if (parts.length!=nfields) { 
					throw new IOException("ERROR: wrong number of fields.");
				}
						
				int errorCode=Integer.parseInt(parts[0]);
				String url=parts[1];											
				String encoded = Base32.encode(md.digest(url.getBytes()));
				Hits hits=searcher.search(new TermQuery(new Term("exacturl", encoded))); // index query					
				if (hits.length()>0) {
					idsThread[hits.id(0)%nThreads].add(hits.id(0));
				}
			
				System.out.println(errorCode+" "+hits.length()+" "+url+" "+(hits.length()>0 ? hits.id(0) : "")); // TODO remove
			}			
			br.close();
		}
		else { // set all ids
			int maxDoc=reader.maxDoc();
			for (int i=0;i<maxDoc;i++) {
				idsThread[i%nThreads].add(i);
			}		
		}
		
		for (int i=0;i<nThreads;i++) {
			System.out.println("thread "+i+"'s list size:"+idsThread[i].size());
		}
	}
	
	/**
	 * Open url and get response code
	 * @param url
	 * @return response code
	 * @throws IOException
	 */
	public static int openUrlInputStream(URL url) throws IOException {
		int httpResponseCode = -1;
        URLConnection urlConnection = url.openConnection();        
        if(urlConnection instanceof HttpURLConnection) {
            HttpURLConnection httpUrlConnection = (HttpURLConnection)urlConnection;
            httpUrlConnection.setInstanceFollowRedirects(true);
            httpResponseCode = httpUrlConnection.getResponseCode();             
        }
        urlConnection.getInputStream().close();     
        return httpResponseCode;
    }

	
 
	
	/**
	 * Main
	 * @param args arguments
	 */
	public static void main(String[] args) throws Exception {	
						
		String usage="usage: [index path] [url base] [number threads] [startDoc or 0(first)] [lastDoc exclusively or -1(all)] [errorsFile or nothing] \n e.g.: /data/arcs/outputsIAall/index http://t2.tomba.fccn.pt/wayback/wayback 20 0 -1 errors.urls";
				
		if (args.length!=5 && args.length!=6) {
			System.out.println(usage);
			System.exit(0);
		}
		
		Directory idx = FSDirectory.getDirectory(args[0], false);
		org.apache.lucene.index.IndexReader reader=IndexReader.open(idx);		
		org.apache.lucene.search.Searcher searcher = new IndexSearcher(idx);
		PwaBlacklistCache cache=new PwaBlacklistCache(reader,searcher,null);				
		cache.writeCache(args[1],Integer.parseInt(args[2]),Integer.parseInt(args[3]),Integer.parseInt(args[4]), args.length==6 ? args[5] : null);
		searcher.close();
		reader.close();				
	}
	
	
	
	
	/**
	 * Process thread
	 */
	class ProcessThread extends Thread {		
		private int id;
		private int count;
		private int thread;
		private int lastDoc;
		private String urlBase;
		private URL netUrl;		

		public ProcessThread(int thread, int firstDoc, int lastDoc, String urlBase) {
			this.count=0;
			this.thread=thread;
			this.lastDoc=lastDoc;
			this.urlBase=urlBase;
		}
		
		public void run() {			
			System.out.println("Started thread "+thread);
			
			int size=idsThread[thread].size();
			while (count<size) {							
				if (thread==0 && count%INC_LOG==0) {
					System.out.println("count: "+count);
				}
				
				try {
					id=idsThread[thread].get(count);
					netUrl = new URL(urlBase+"/"+"id"+id+"index"+INDEX_ID);									
					int responseCode=openUrlInputStream(netUrl);				
					if (responseCode==PAGE_CODE_NOT_FOUND) {				
						docBlackList.set(id,true);
						System.err.println(""+id+" error 404");
					}
					else if (responseCode==-1) {
						System.err.println("-1: "+id);
					}								
				}
				catch (IOException ex) {
					docBlackList.set(id,true);
					System.err.println(""+id+" error:"+ex.getMessage());
				}		
				catch (RuntimeException ex) { // problem in response from the server
					docBlackList.set(id,true);
					System.err.println(""+id+" error:"+ex.getMessage());
				}
								
				count++;
			}
		}
	}
}
