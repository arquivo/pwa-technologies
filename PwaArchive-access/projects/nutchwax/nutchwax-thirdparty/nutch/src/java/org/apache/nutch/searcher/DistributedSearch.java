/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.nutch.searcher;

import java.net.InetSocketAddress;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.lang.reflect.Method;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.nutch.parse.ParseData;
import org.apache.nutch.parse.ParseText;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.util.NutchConfiguration;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.ipc.RPC;
import org.apache.hadoop.ipc.VersionedProtocol;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;

import org.apache.lucene.search.*;
import org.apache.nutch.global.Global;



/** Implements the search API over IPC connnections. */
public class DistributedSearch {
  public static final Log LOG = LogFactory.getLog(DistributedSearch.class);

  private DistributedSearch() {}                  // no public ctor

  /** The distributed search protocol. */
  public static interface Protocol
    extends Searcher, HitDetailer, HitSummarizer, HitContent, HitInlinks, VersionedProtocol {

    /** The name of the segments searched by this node. */
    String[] getSegmentNames();
  }

  /** The search server. */
  public static class Server  {

    private Server() {}

    /** Runs a search server. */
    public static void main(String[] args) throws Exception {
      String usage = "DistributedSearch$Server <port> <index dir> <blacklist dir>";

      if (args.length == 0 || args.length > 3) {
        System.err.println(usage);
        System.exit(-1);
      }

      int port = Integer.parseInt(args[0]);
      Path directory = new Path(args[1]);
      File blacklistFile = null;
      if (args.length==3 && args[2]!=null) {
    	  blacklistFile = new File(args[2]);
	  }

      Configuration conf = NutchConfiguration.create();
      org.apache.hadoop.ipc.Server server = getServer(conf, directory, port, blacklistFile);           
      
      server.start();
      server.join();
    }
    
    static org.apache.hadoop.ipc.Server getServer(Configuration conf, Path directory, int port, File blacklistFile) throws IOException{      
      int numHandlers=conf.getInt(Global.NUMBER_HANDLERS, -1);
      boolean ipcVerbose=conf.getBoolean(Global.IPC_VERBOSE, false);
      NutchBean bean = new NutchBean(conf, directory, blacklistFile);
      return RPC.getServer(bean, "0.0.0.0", port, numHandlers, ipcVerbose, conf);
    }

  }

  /** The search client. */
  public static class Client extends Thread
    implements Searcher, HitDetailer, HitSummarizer, HitContent, HitInlinks,
               Runnable {

    private InetSocketAddress[] defaultAddresses;
    private boolean[] liveServer;
    private HashMap segmentToAddress = new HashMap();
    
    private boolean running = true;
    private Configuration conf;

    /** Construct a client talking to servers listed in the named file.
     * Each line in the file lists a server hostname and port, separated by
     * whitespace. 
     */
    public Client(Path file, Configuration conf) throws IOException {
      this(readConfig(file, conf), conf);
    }

    private static InetSocketAddress[] readConfig(Path path, Configuration conf)
      throws IOException {
      FileSystem fs = FileSystem.get(conf);
      BufferedReader reader =
        new BufferedReader(new InputStreamReader(fs.open(path)));
      try {
        ArrayList addrs = new ArrayList();
        String line;
        while ((line = reader.readLine()) != null) {
          StringTokenizer tokens = new StringTokenizer(line);
          if (tokens.hasMoreTokens()) {
            String host = tokens.nextToken();
            if (tokens.hasMoreTokens()) {
              String port = tokens.nextToken();
              addrs.add(new InetSocketAddress(host, Integer.parseInt(port)));
              if (LOG.isInfoEnabled()) {
                LOG.info("Client adding server "  + host + ":" + port);
              }
            }
          }
        }
        return (InetSocketAddress[])
          addrs.toArray(new InetSocketAddress[addrs.size()]);
      } finally {
        reader.close();
      }
    }

    /** Construct a client talking to the named servers. */
    public Client(InetSocketAddress[] addresses, Configuration conf) throws IOException {
      this.conf = conf;
      this.defaultAddresses = addresses;
      this.liveServer = new boolean[addresses.length];
      updateSegments();
      setDaemon(true);
      start();
    }
    
    private static final Method GET_SEGMENTS;
    private static final Method SEARCH;
    private static final Method SEARCH_MAX_MATCHES; // TODO TREC and ranking tests
    private static final Method DETAILS;
    private static final Method DETAILS_FIELDS; // returns only the fields required. Returns multiple hits in only one Hadoop request, contrary to DETAILS 
    private static final Method SUMMARY;
    private static final Method SUMMARY_MULTIPLE; // returns multiple hits in only one Hadoop request, contrary to SUMMARY
    static {
      try {
        GET_SEGMENTS = Protocol.class.getMethod
          ("getSegmentNames", new Class[] {});
        SEARCH = Protocol.class.getMethod
          ("search", new Class[] { Query.class, Integer.TYPE, String.class,
                                   String.class, Boolean.TYPE});
        SEARCH_MAX_MATCHES = Protocol.class.getMethod // TODO TREC
          ("search", new Class[] { Query.class, Integer.TYPE, Integer.TYPE, Integer.TYPE,
        		  				   String.class, String.class, Boolean.TYPE, PwaFunctionsWritable.class, Integer.TYPE});                              
        DETAILS = Protocol.class.getMethod
          ("getDetails", new Class[] { Hit.class});
        DETAILS_FIELDS = Protocol.class.getMethod // BUG wayback 0000155
          ("getDetails", new Class[] { PwaRequestDetailsWritable.class});
        SUMMARY = Protocol.class.getMethod
          ("getSummary", new Class[] { HitDetails.class, Query.class});
        SUMMARY_MULTIPLE = Protocol.class.getMethod
          ("getSummary", new Class[] { PwaRequestSummaryWritable.class});
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }
    

    /** Updates segment names.
     * 
     * @throws IOException
     */
    public void updateSegments() throws IOException {
      
      int liveServers=0;
      int liveSegments=0;
      
      // Create new array of flags so they can all be updated at once.
      boolean[] updatedLiveServer = new boolean[defaultAddresses.length];
      
      // build segmentToAddress map
      Object[][] params = new Object[defaultAddresses.length][0];
      String[][] results =
        (String[][])RPC.call(GET_SEGMENTS, params, defaultAddresses, this.conf);

      for (int i = 0; i < results.length; i++) {  // process results of call
        InetSocketAddress addr = defaultAddresses[i];
        String[] segments = results[i];
        if (segments == null) {
          updatedLiveServer[i] = false;
          if (LOG.isWarnEnabled()) {
            LOG.warn("Client: no segments from: " + addr);
          }
          continue;
        }
        for (int j = 0; j < segments.length; j++) {
          if (LOG.isTraceEnabled()) {
            LOG.trace("Client: segment "+segments[j]+" at "+addr);
          }
          segmentToAddress.put(segments[j], addr);
        }
        updatedLiveServer[i] = true;
        liveServers++;
        liveSegments+=segments.length;
      }

      // Now update live server flags.
      this.liveServer = updatedLiveServer;

      if (LOG.isInfoEnabled()) {
        LOG.info("STATS: "+liveServers+" servers, "+liveSegments+" segments.");
      }
    }

    /** Return the names of segments searched. */
    public String[] getSegmentNames() {
      return (String[])
        segmentToAddress.keySet().toArray(new String[segmentToAddress.size()]);
    }
    

    /**
     * Send query to lucene
     * @param query
     * @param numHits
     * @param dedupField
     * @param sortField
     * @param reverse
     * @return
     * @throws IOException
     */
    public Hits search(final Query query, final int numHits, 
            final String dedupField, final String sortField,
            final boolean reverse) throws IOException {
    	return search(query, numHits, NutchBean.MATCHED_DOCS_CONST_IGNORE, 0, dedupField, sortField, reverse, null, Integer.MAX_VALUE);
    }

 
    /**
     * Send query to lucene
     * @param searcherMaxHits maximum number of matched documents
     * @param maxHitsPerDup maximum hits returned per version or zero to ignore
     */       
    public Hits search(final Query query, final int numHits, final int searcherMaxHits,
    				   final int maxHitsPerDup, final String dedupField, final String sortField,
                       final boolean reverse, final PwaFunctionsWritable functions, int maxHitsPerVersion) throws IOException {
      // Get the list of live servers.  It would be nice to build this
      // list in updateSegments(), but that would create concurrency issues.
      // We grab a local reference to the live server flags in case it
      // is updated while we are building our list of liveAddresses.
      boolean[] savedLiveServer = this.liveServer;
      int numLive = 0;
      for (int i = 0; i < savedLiveServer.length; i++) {
        if (savedLiveServer[i])
          numLive++;
      }
      InetSocketAddress[] liveAddresses = new InetSocketAddress[numLive];
      int[] liveIndexNos = new int[numLive];
      int k = 0;
      for (int i = 0; i < savedLiveServer.length; i++) {
        if (savedLiveServer[i]) {
          liveAddresses[k] = defaultAddresses[i];
          liveIndexNos[k] = i;
          k++;
        }
      }
      int N = liveAddresses.length;
      
      Hits[] results = null;
      if (searcherMaxHits==NutchBean.MATCHED_DOCS_CONST_IGNORE && functions==null) { // TODO MC 
    	  Object[][] params = new Object[liveAddresses.length][5];
    	  for (int i = 0; i < params.length; i++) {
    		  params[i][0] = query;
    		  params[i][1] = new Integer(numHits);
    		  params[i][2] = dedupField;
    		  params[i][3] = sortField;
    		  params[i][4] = Boolean.valueOf(reverse);
    	  }
    	  //long startTime = System.currentTimeMillis( );
    	  //String timeoutPerQueryServer = this.conf.get( "timeout.index.servers.response" );
    	  //LOG.info( "[DistributedSearch][Search] 1  Method = " + SEARCH_MAX_MATCHES + " Query = " + params[ 0 ][ 0 ] + " timeout = " + timeoutPerQueryServer + " calling query server...");
    	  
    	  results = (Hits[])RPC.call(SEARCH, params, liveAddresses, this.conf);
    	  
    	  //long elapsed = System.currentTimeMillis( ) - startTime;
    	  //LOG.info( "[DistributedSearch][Search] 1 Method = " + SEARCH_MAX_MATCHES + " Query = " + params[ 0 ][ 0 ] + " timeout = " + timeoutPerQueryServer + " response time = " + elapsed );

      }
      else { // TODO MC 
    	  Object[][] params = new Object[liveAddresses.length][9];
    	  for (int i = 0; i < params.length; i++) {
    		  params[i][0] = query;
    		  params[i][1] = new Integer(numHits);    		  
    		  params[i][2] = new Integer(searcherMaxHits);
    		  params[i][3] = new Integer(maxHitsPerDup);    		  
    		  params[i][4] = dedupField;
    		  params[i][5] = sortField;
    		  params[i][6] = Boolean.valueOf(reverse);    
    		  params[i][7] = functions;
    		  params[i][8] = new Integer(maxHitsPerVersion);
    	  }
    	  
    	  //long startTime = System.currentTimeMillis( );
    	  //String timeoutPerQueryServer = this.conf.get( "timeout.index.servers.response" );
    	  //LOG.info( "[DistributedSearch][Search] 2  Method = " + SEARCH_MAX_MATCHES + " Query = " + params[ 0 ][ 0 ] + " timeout = " + timeoutPerQueryServer + " calling query server...");
    	  
    	  results = (Hits[])RPC.call(SEARCH_MAX_MATCHES, params, liveAddresses, this.conf);
    	  
    	  //long elapsed = System.currentTimeMillis( ) - startTime;
    	  //LOG.info( "[DistributedSearch][Search] 2 Method = " + SEARCH_MAX_MATCHES + " Query = " + params[ 0 ][ 0 ] + " timeout = " + timeoutPerQueryServer + " response time = " + elapsed );
    	  
      }
      
      TreeSet queue;                              // cull top hits from results

      if (sortField == null || reverse) { // reverse=true
        queue = new TreeSet(new Comparator() {
            public int compare(Object o1, Object o2) {
              return ((Comparable)o2).compareTo(o1); // reverse natural order
            }
          });
      } else {
        queue = new TreeSet();
      }
      
      long totalHits = 0;
      Comparable maxValue = null;
      for (int i = 0; i < results.length; i++) {
        Hits hits = results[i];
        if (hits == null) continue;
        totalHits += hits.getTotal();
        for (int j = 0; j < hits.getLength(); j++) {
          Hit h = hits.getHit(j);
          if (maxValue == null ||
              ((reverse || sortField == null)
               ? h.getSortValue().compareTo(maxValue) >= 0
               : h.getSortValue().compareTo(maxValue) <= 0)) {        	         	 
            queue.add(new Hit(liveIndexNos[i], h.getIndexDocNo(), h.getSortValue(), h.getDedupValue()));
            if (queue.size() > numHits) {         // if hit queue overfull
              queue.remove(queue.last());         // remove lowest in hit queue
              maxValue = ((Hit)queue.last()).getSortValue(); // reset maxValue
            }
          }
        }
      }
      return new Hits(totalHits, (Hit[])queue.toArray(new Hit[queue.size()]));
    }
    
    // version for hadoop-0.5.0.jar
    public static final long versionID = 1L;
    
    private Protocol getRemote(Hit hit) throws IOException {
      return (Protocol)
        RPC.getProxy(Protocol.class, versionID, defaultAddresses[hit.getIndexNo()], conf);
    }

    private Protocol getRemote(HitDetails hit) throws IOException {
      InetSocketAddress address =
        (InetSocketAddress)segmentToAddress.get(hit.getValue("segment"));
      return (Protocol)RPC.getProxy(Protocol.class, versionID, address, conf);
    }

    public String getExplanation(Query query, Hit hit) throws IOException {
      return getRemote(hit).getExplanation(query, hit);
    }
        
    public String getExplanation(Query query, Hit hit, PwaFunctionsWritable functions) throws IOException {
  	  return getRemote(hit).getExplanation(query, hit, functions);
    }
        
    public HitDetails getDetails(Hit hit) throws IOException {
      return getRemote(hit).getDetails(hit);
    }
        
    public HitDetails[] getDetails(Hit[] hits) throws IOException {
      InetSocketAddress[] addrs = new InetSocketAddress[hits.length];
      Object[][] params = new Object[hits.length][1];
      for (int i = 0; i < hits.length; i++) {
        addrs[i] = defaultAddresses[hits[i].getIndexNo()];
        params[i][0] = hits[i];
      }
      return (HitDetails[])RPC.call(DETAILS, params, addrs, conf);
    }
    
    /* BUG wayback 0000155*/ /* BUG nutchwax 0000616 */
    public HitDetails[] getDetails(PwaRequestDetailsWritable details) throws IOException {    
      Hit[] hits=details.getHits();      
    
      // see the number of servers containing these hits
      Hashtable<Integer,Vector<Hit>> hitsClusters=new Hashtable<Integer,Vector<Hit>>();
      Vector<Hit> vecHit=null;
      for (int i=0; i<hits.length; i++) { 
    	  vecHit=hitsClusters.get(hits[i].getIndexNo());
    	  if (vecHit==null) {
    		  vecHit=new Vector<Hit>();
    	  }
    	  vecHit.add(hits[i]);
    	  hitsClusters.put(hits[i].getIndexNo(),vecHit);
      }            
      
      InetSocketAddress[] addrs = new InetSocketAddress[hitsClusters.size()];
      Object[][] params = new Object[hitsClusters.size()][1];      
      for (int i=0;i<hitsClusters.size();i++) {
    	  vecHit=hitsClusters.get(i);		
	  			
		  PwaRequestDetailsWritable detailsWritable = new PwaRequestDetailsWritable();
		  detailsWritable.setFields(details.getFields());
		  detailsWritable.setHits(vecHit.toArray(new Hit[vecHit.size()]));
		
		  addrs[i] = defaultAddresses[vecHit.get(0).getIndexNo()]; // all hits of the vector (included the 0) have the same IndexNo, so they go to the same machine
		  params[i][0] = detailsWritable;					  
      }      	       
      
      HitDetails[][] allDetails=(HitDetails[][])RPC.call(DETAILS_FIELDS, params, addrs, conf);      
                  
      // copy HitDetails arrays from all server into only one array
      int totalSize=0;
      for (int i=0;i<allDetails.length;i++) {
    	  totalSize+=allDetails[i].length;    	      	 
      }     
      HitDetails[] response=new HitDetails[totalSize];
      int offset=0;
      for (int i=0;i<allDetails.length;i++) {
    	  System.arraycopy(allDetails[i], 0, response, offset, allDetails[i].length);
    	  offset+=allDetails[i].length;
      }      
      return response;             
    }    

    public Summary getSummary(HitDetails hit, Query query) throws IOException {
      return getRemote(hit).getSummary(hit, query);
    }

    public Summary[] getSummary(HitDetails[] hits, Query query) throws IOException {
      InetSocketAddress[] addrs = new InetSocketAddress[hits.length];
      Object[][] params = new Object[hits.length][2];
      for (int i = 0; i < hits.length; i++) {
        HitDetails hit = hits[i];
        addrs[i] =
          (InetSocketAddress)segmentToAddress.get(hit.getValue("segment"));
        params[i][0] = hit;
        params[i][1] = query;
      }
      return (Summary[])RPC.call(SUMMARY, params, addrs, conf);
    }
    
    /* BUG nutchwax 0000616 */
    public Summary[] getSummary(PwaRequestSummaryWritable summaries) throws IOException {
    	HitDetails[] hitDetails=summaries.getHitDetails();      
    	Query query=summaries.getQuery();
    	
    	// see the number of servers containing these hits
        Hashtable<InetSocketAddress,Vector<HitDetails>> hitsClusters=new Hashtable<InetSocketAddress,Vector<HitDetails>>();
        Vector<HitDetails> vecHit=null;
        InetSocketAddress addrsAux=null;
        for (int i=0; i<hitDetails.length; i++) { 
        	addrsAux=(InetSocketAddress)segmentToAddress.get(hitDetails[i].getValue("segment"));
      	  	vecHit=hitsClusters.get(addrsAux);
      	  	if (vecHit==null) {
      	  		vecHit=new Vector<HitDetails>();
      	  	}
      	  	vecHit.add(hitDetails[i]);
      	  	hitsClusters.put(addrsAux,vecHit);
        }            
        
        InetSocketAddress[] addrs = new InetSocketAddress[hitsClusters.size()];
        Object[][] params = new Object[hitsClusters.size()][1];  
        int i=0;
        Set<Entry<InetSocketAddress,Vector<HitDetails>>> entrySet=hitsClusters.entrySet();
        for (Iterator<Entry<InetSocketAddress,Vector<HitDetails>>> it=entrySet.iterator();it.hasNext();i++) {        
        	Entry<InetSocketAddress,Vector<HitDetails>> entry = it.next();
        	addrsAux=entry.getKey();
        	vecHit=entry.getValue();		
  	  			
        	PwaRequestSummaryWritable summariesWritable = new PwaRequestSummaryWritable();
  		  	summariesWritable.setQuery(query);
  		  	summariesWritable.setHitDetails(vecHit.toArray(new HitDetails[vecHit.size()]));
  		
  		  	addrs[i] = addrsAux; // all hits of the vector (included the 0) have the same IndexNo, so they go to the same machine
  		  	params[i][0] = summariesWritable;					  
        }      	       
        
        Summary[][] allSummaries=(Summary[][])RPC.call(SUMMARY_MULTIPLE, params, addrs, conf);       
                    
        // copy HitDetails arrays from all server into only one array
        int totalSize=0;
        for (i=0;i<allSummaries.length;i++) {
        	totalSize+=allSummaries[i].length;    	      	 
        }     
        Summary[] response=new Summary[totalSize];
        int offset=0;
        for (i=0;i<allSummaries.length;i++) {
        	System.arraycopy(allSummaries[i], 0, response, offset, allSummaries[i].length);
        	offset+=allSummaries[i].length;
        }      
        return response;
    }
    
    public byte[] getContent(HitDetails hit) throws IOException {
      return getRemote(hit).getContent(hit);
    }
    
    public ParseData getParseData(HitDetails hit) throws IOException {
      return getRemote(hit).getParseData(hit);
    }
      
    public ParseText getParseText(HitDetails hit) throws IOException {
      return getRemote(hit).getParseText(hit);
    }
      
    public String[] getAnchors(HitDetails hit) throws IOException {
      return getRemote(hit).getAnchors(hit);
    }

    public Inlinks getInlinks(HitDetails hit) throws IOException {
      return getRemote(hit).getInlinks(hit);
    }

    public long getFetchDate(HitDetails hit) throws IOException {
      return getRemote(hit).getFetchDate(hit);
    }
      
    public static void main(String[] args) throws Exception {
      String usage = "DistributedSearch$Client query <host> <port> ...";

      if (args.length == 0) {
        System.err.println(usage);
        System.exit(-1);
      }

      Query query = Query.parse(args[0], NutchConfiguration.create());
      
      InetSocketAddress[] addresses = new InetSocketAddress[(args.length-1)/2];
      for (int i = 0; i < (args.length-1)/2; i++) {
        addresses[i] =
          new InetSocketAddress(args[i*2+1], Integer.parseInt(args[i*2+2]));
      }

      Client client = new Client(addresses, NutchConfiguration.create());
      //client.setTimeout(Integer.MAX_VALUE);

      Hits hits = client.search(query, 10, null, null, false);
      System.out.println("Total hits: " + hits.getTotal());
      for (int i = 0; i < hits.getLength(); i++) {
        System.out.println(" "+i+" "+ client.getDetails(hits.getHit(i)));
      }

    }

    public void run() {           
      int timeoutAlive=conf.getInt(Global.TIMEOUT_INDEX_SERVERS_ALIVE, -1);         
    	
      // first time
      try {
    	  updateSegments();
      } 
      catch (IOException ioe) {
          if (LOG.isWarnEnabled()) { 
        	  LOG.warn("No search servers available!"); 
          }          
      }
    	
      while (running){
        try{
          Thread.sleep(timeoutAlive);
        } catch (InterruptedException ie) {
          if (LOG.isInfoEnabled()) {
            LOG.info("Thread sleep interrupted.");
          }
        }
        try{
          if (LOG.isInfoEnabled()) {
            LOG.info("Querying segments from search servers...");
          }
          updateSegments();
        } catch (IOException ioe) {
          if (LOG.isWarnEnabled()) { LOG.warn("No search servers available!"); }
          liveServer = new boolean[defaultAddresses.length];
        }
      }
    }
    
    /**
     * Stops the watchdog thread.
     */
    public void close() {
      running = false;
      interrupt();
    }
  }
}
