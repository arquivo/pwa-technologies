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

import java.io.*;
import java.util.*;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.Closeable;
import org.apache.hadoop.conf.*;
import org.apache.nutch.parse.*;
import org.apache.nutch.indexer.*;
import org.apache.nutch.crawl.Inlinks;
import org.apache.nutch.crawl.Inlink;
import org.apache.nutch.util.NutchConfiguration;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.Query.*;

import org.apache.lucene.search.PwaFunctionsWritable;

import org.apache.nutch.global.Global;


/** 
 * One stop shopping for search-related functionality.
 * @version $Id: NutchBean.java,v 1.19 2005/02/07 19:10:08 cutting Exp $
 */   
public class NutchBean
  implements Searcher, HitDetailer, HitSummarizer, HitContent, HitInlinks,
             DistributedSearch.Protocol, Closeable {

  public static final Log LOG = LogFactory.getLog(NutchBean.class);
  
  public static final int MATCHED_DOCS_CONST_IGNORE = -2;

//  static {
//    LogFormatter.setShowThreadIDs(true);
//  }

  private String[] segmentNames;

  private Searcher searcher;
  private HitDetailer detailer;
  private HitSummarizer summarizer;
  private HitContent content;
  private HitInlinks linkDb;


  /** BooleanQuery won't permit more than 32 required/prohibited clauses.  We
   * don't want to use too many of those. */ 
  private static final int MAX_PROHIBITED_TERMS = 20;
  
  private Configuration conf;
  private FileSystem fs;
  
  private int maxFulltextMatchesReturned;
  private int maxFulltextMatchesRanked;	
  private int maxQueryTerms;
  private int maxQueryExtraTerms;
  

  /** Cache in servlet context. */
  public static NutchBean get(ServletContext app, Configuration conf) throws IOException {
    NutchBean bean = (NutchBean)app.getAttribute("nutchBean");
    if (bean == null) {
      //if (LOG.isInfoEnabled()) { 
    	  LOG.info("creating new bean"); 
      //}
      bean = new NutchBean(conf);
      app.setAttribute("nutchBean", bean);
    }
    return bean;
  }


  /**
   * 
   * @param conf
   * @throws IOException
   */
  public NutchBean(Configuration conf) throws IOException {
    this(conf, null, null);
  }
  
  /**
   *  Construct in a named directory. 
   * @param conf
   * @param dir
   * @throws IOException
   */
  public NutchBean(Configuration conf, Path dir, Path blacklistDir) throws IOException {
	    this.conf = conf;
        this.fs = FileSystem.get(this.conf);
        if (dir == null) {
            dir = new Path(this.conf.get("searcher.dir", "crawl"));
        }
        Path servers = new Path(dir, "search-servers.txt");
        if (fs.exists(servers)) {
            LOG.info("searching servers in " + servers);            
            init(new DistributedSearch.Client(servers, conf));
        } 
        else {
            init(new Path(dir, "index"), new Path(dir, "indexes"), new Path(
                    dir, "segments"), new Path(dir, "linkdb"), blacklistDir);
        }
                       
    	this.maxFulltextMatchesReturned = conf.getInt(Global.MAX_FULLTEXT_MATCHES_RETURNED, -1);
    	this.maxFulltextMatchesRanked = conf.getInt(Global.MAX_FULLTEXT_MATCHES_RANKED, -1);    	
    	this.maxQueryTerms = conf.getInt(Global.MAX_QUERY_TERMS, -1);
    	this.maxQueryExtraTerms = conf.getInt(Global.MAX_QUERY_EXTRA_TERMS, -1);
    }

  private void init(Path indexDir, Path indexesDir, Path segmentsDir, Path linkDb, Path blacklistDir)
    throws IOException {
	  
    IndexSearcher indexSearcher;
    if (this.fs.exists(indexDir)) {
        LOG.info("opening merged index in " + indexDir);
        indexSearcher = new IndexSearcher(indexDir, this.conf, blacklistDir);
    } 
    else {
        LOG.info("opening indexes in " + indexesDir);
      
        Vector vDirs=new Vector();
        Path [] directories = fs.listPaths(indexesDir);
        for(int i = 0; i < fs.listPaths(indexesDir).length; i++) {
        	Path indexdone = new Path(directories[i], Indexer.DONE_NAME);
        	if(fs.isFile(indexdone)) {
        		vDirs.add(directories[i]);
        	}
        }
            
        directories = new Path[ vDirs.size() ];
        for(int i = 0; vDirs.size()>0; i++) {
        	directories[i]=(Path)vDirs.remove(0);
        }
      
        indexSearcher = new IndexSearcher(directories, this.conf, blacklistDir);
    }

    LOG.info("opening segments in " + segmentsDir);    
    FetchedSegments segments = new FetchedSegments(this.fs, segmentsDir.toString(),this.conf);
    
    this.segmentNames = segments.getSegmentNames();

    this.searcher = indexSearcher;
    this.detailer = indexSearcher;
    this.summarizer = segments;
    this.content = segments;

    LOG.info("opening linkdb in " + linkDb);     
    this.linkDb = new LinkDbInlinks(fs, linkDb, this.conf);
  }

  private void init(DistributedSearch.Client client) {
    this.segmentNames = client.getSegmentNames();
    this.searcher = client;
    this.detailer = client;
    this.summarizer = client;
    this.content = client;
    this.linkDb = client;
  }


  public String[] getSegmentNames() {
    return segmentNames;
  }

  public Hits search(Query query, int numHits) throws IOException {
    return search(query, numHits, null, null, false);
  }
  
  public Hits search(Query query, int numHits,
                     String dedupField, String sortField, boolean reverse)
    throws IOException {

    return searcher.search(query, numHits, dedupField, sortField, reverse);
  }
  
  private class DupHits extends ArrayList {
    private boolean maxSizeExceeded;
  }

  /** Search for pages matching a query, eliminating excessive hits from the
   * same site.  Hits after the first <code>maxHitsPerDup</code> from the same
   * site are removed from results.  The remaining hits have {@link
   * Hit#moreFromDupExcluded()} set.  <p> If maxHitsPerDup is zero then all
   * hits are returned.
   * 
   * @param query query
   * @param numHits number of requested hits
   * @param maxHitsPerDup the maximum hits returned with matching values, or zero
   * @return Hits the matching hits
   * @throws IOException
   */
  public Hits search(Query query, int numHits, int maxHitsPerDup) throws IOException {
	  return search(query, numHits, maxHitsPerDup, "site", null, false, false);
  }

  /** Search for pages matching a query, eliminating excessive hits with
   * matching values for a named field.  Hits after the first
   * <code>maxHitsPerDup</code> are removed from results.  The remaining hits
   * have {@link Hit#moreFromDupExcluded()} set.  <p> If maxHitsPerDup is zero
   * then all hits are returned.
   * 
   * @param query query
   * @param numHits number of requested hits
   * @param maxHitsPerDup the maximum hits returned with matching values, or zero
   * @param dedupField field name to check for duplicates
   * @return Hits the matching hits
   * @throws IOException
   */
  public Hits search(Query query, int numHits, int maxHitsPerDup, String dedupField) throws IOException {
	  return search(query, numHits, maxHitsPerDup, dedupField, null, false, false);
  }
  
  /** Search for pages matching a query, eliminating excessive hits with
   * matching values for a named field.  Hits after the first
   * <code>maxHitsPerDup</code> are removed from results.  The remaining hits
   * have {@link Hit#moreFromDupExcluded()} set.  <p> If maxHitsPerDup is zero
   * then all hits are returned.
   * 
   * @param query query
   * @param numHits number of requested hits
   * @param searcherMaxHits number of matched documents for ranking, or MATCHED_DOCS_CONST_IGNORE to ignore   
   * @param maxHitsPerDup the maximum hits returned with matching values, or zero
   * @param dedupField field name to check for duplicates
   * @param sortField Field to sort on (or null if no sorting).
   * @param reverse True if we are to reverse sort by <code>sortField</code>.
   * @param functions Extra parameters   
   * @param maxHitsPerVersion maximum hits returned with the same url and different version
   * @return Hits the matching hits
   * @throws IOException
   */
  public Hits search(Query query, int numHits, int searcherMaxHits, int maxHitsPerDup, String dedupField,
                     String sortField, boolean reverse, PwaFunctionsWritable functions, int maxHitsPerVersion) throws IOException {	  
	  return search(query, numHits, searcherMaxHits, maxHitsPerDup, dedupField, sortField, reverse, functions, maxHitsPerVersion, false);
  }
  
  
  /** Search for pages matching a query, eliminating excessive hits with
   * matching values for a named field.  Hits after the first
   * <code>maxHitsPerDup</code> are removed from results.  The remaining hits
   * have {@link Hit#moreFromDupExcluded()} set.  <p> If maxHitsPerDup is zero
   * then all hits are returned.
   * 
   * @param query query
   * @param numHits number of requested hits
   * @param searcherMaxHits number of matched documents for ranking, or MATCHED_DOCS_CONST_IGNORE to ignore   
   * @param maxHitsPerDup the maximum hits returned with matching values, or zero
   * @param dedupField field name to check for duplicates
   * @param sortField Field to sort on (or null if no sorting).
   * @param reverse True if we are to reverse sort by <code>sortField</code>.
   * @param functions Extra parameters    
   * @param maxHitsPerVersion maximum hits returned with the same url and different version
   * @param waybackQuery if true it is a query from wayback; otherwise it is from nutchwax
   * @return Hits the matching hits
   * @throws IOException
   */
  public Hits search(Query query, int numHits, int searcherMaxHits, int maxHitsPerDup, String dedupField,
                     String sortField, boolean reverse, PwaFunctionsWritable functions, int maxHitsPerVersion, boolean waybackQuery) throws IOException {	  
       	  		  
	Hits hits = null; 
	if (waybackQuery) {
		hits=searcher.search(query, numHits, searcherMaxHits, maxHitsPerDup, dedupField, sortField, reverse, functions, maxHitsPerVersion);		
		hits.setTotalIsExact(true);
		return hits;
	}
	
	// check maximum value of variables
	if (numHits>maxFulltextMatchesReturned) {
		numHits=maxFulltextMatchesReturned;
	}
	if (searcherMaxHits>maxFulltextMatchesRanked) {
		searcherMaxHits=maxFulltextMatchesRanked;
	}
	
	// limit query terms for full-text queries
	query=limitTerms(query);	  
    
    int numHitsRaw;
    float rawHitsFactor;
    if (maxHitsPerDup<=0) {
    	if (searcherMaxHits==MATCHED_DOCS_CONST_IGNORE && functions==null) { 
    		return searcher.search(query, numHits, dedupField, sortField, reverse);
    	}
    	else {    		
    		return searcher.search(query, numHits, searcherMaxHits, maxHitsPerDup, dedupField, sortField, reverse, functions, maxHitsPerVersion);    	
    	}
    }
    else {
    	rawHitsFactor = this.conf.getFloat("searcher.hostgrouping.rawhits.factor", 2.0f);
        numHitsRaw = (int)(numHits * rawHitsFactor);
        
        LOG.debug("searching for "+numHitsRaw+" raw hits");                
        hits=searcher.search(query, numHitsRaw, searcherMaxHits, maxHitsPerDup, dedupField, sortField, reverse, functions, maxHitsPerVersion);  // the same method for all values of searcherMaxHits
    }           
    
    boolean lastRequest=false; 
    if (numHitsRaw>hits.getTotal()) { // BUG 200608 - do no request continuously until it have numHits if the match has a smaller number of hits
    	lastRequest=true;
    }
    
    // remove duplicates block
    long total = hits.getTotal();
    Map dupToHits = new HashMap(); 
    List resultList = new ArrayList();
    Set seen = new HashSet();
    List excludedValues = new ArrayList();
    boolean totalIsExact = true;
    for (int rawHitNum = 0; rawHitNum < hits.getTotal(); rawHitNum++) {
      // get the next raw hit
      if (rawHitNum >= hits.getLength()) {
    	  
    	if (lastRequest) { // BUG 200608
    		break;
    	}
    	  
        // optimize query by prohibiting more matches on some excluded values
        Query optQuery = (Query)query.clone();
        for (int i = 0; i < excludedValues.size(); i++) {
          if (i == MAX_PROHIBITED_TERMS)
            break;
          optQuery.addProhibitedTerm(((String)excludedValues.get(i)),dedupField);
        }
        numHitsRaw = (int)(numHitsRaw * rawHitsFactor);
        //if (LOG.isInfoEnabled()) {
          LOG.debug("re-searching for "+numHitsRaw+" raw hits, query: "+optQuery);
        //}
        // hits = searchAux(optQuery, numHitsRaw, searcherMaxHits, maxHitsPerDup, dedupField, sortField, reverse);  // for TREC 
        hits = searcher.search(optQuery, numHitsRaw, searcherMaxHits, maxHitsPerDup, dedupField, sortField, reverse, functions, maxHitsPerVersion);        
        if (numHitsRaw>hits.getTotal()) { // BUG 200608
        	lastRequest=true;
        }
        
        //if (LOG.isInfoEnabled()) {
          LOG.debug("found "+hits.getTotal()+" raw hits");
        //}
        rawHitNum = -1;
        continue;
      }

      Hit hit = hits.getHit(rawHitNum);
      if (seen.contains(hit)) // processed in the previous query
        continue;
      seen.add(hit);
      
      // get dup hits for its value
      String value = hit.getDedupValue();      
      DupHits dupHits = (DupHits)dupToHits.get(value);       
      if (dupHits == null) {   	  
        dupToHits.put(value, dupHits = new DupHits());
      }
                 
      // does this hit exceed maxHitsPerDup?
      if (dupHits.size()==maxHitsPerDup ) {      // yes -- then ignore the hit 
        if (!dupHits.maxSizeExceeded) {

          // mark prior hits with moreFromDupExcluded
          for (int i = 0; i < dupHits.size(); i++) {
            ((Hit)dupHits.get(i)).setMoreFromDupExcluded(true);
          }
          dupHits.maxSizeExceeded = true;

          excludedValues.add(value);              // exclude dup
        }
        totalIsExact = false;
      }    
      else {                                    // no -- then collect the hit
        resultList.add(hit);
        dupHits.add(hit);        

        // are we done?
        // we need to find one more than asked for, so that we can tell if
        // there are more hits to be shown
        if (resultList.size() > numHits)
          break;
      }
    }

    Hits results = new Hits(total, (Hit[])resultList.toArray(new Hit[resultList.size()]));
    results.setTotalIsExact(totalIsExact);
    return results;
  }
  
  
  /**
   * Limit number of query terms and extra query terms
   * @param input
   * @param output
   */
  public Query limitTerms(Query input) {
	  Query output=new Query(input.getConf());
	  Clause[] clauses = input.getClauses();
	  int termsCounter=0;
	  int termsExtraCounter=0;
	  	  
	  for (int i=0; i<clauses.length; i++) {
		  Clause c = clauses[i];

	      if (c.getField().equals(Clause.DEFAULT_FIELD) && !c.isProhibited() && termsCounter>=maxQueryTerms) { // is it is a term and reached the limiti
	    	  continue;
	      }		  
	      if ((!c.getField().equals(Clause.DEFAULT_FIELD) || c.isProhibited()) && termsExtraCounter>=maxQueryExtraTerms) // it is an exstra term or a not
	        continue;                                 

	      if (c.isPhrase()) {                         
	    	  Term[] terms = c.getPhrase().getTerms();
	    	  
	    	  int newLength=terms.length;
	    	  if (c.getField().equals(Clause.DEFAULT_FIELD) && !c.isProhibited()) {
	    		  if (terms.length+termsCounter>maxQueryTerms) {
		        	  newLength=maxQueryTerms-termsCounter;
		        	  termsCounter+=newLength;
	    		  }
	    		  else {
	    			  termsCounter+=terms.length;
	    		  }
	    	  }
	    	  else {
	    		  if (terms.length+termsExtraCounter>maxQueryExtraTerms) {
		        	  newLength=maxQueryExtraTerms-termsExtraCounter;
		        	  termsExtraCounter+=newLength;
	    		  }
	    		  else {
	    			  termsExtraCounter+=terms.length;
	    		  }
	    	  }
	    	  	    	  
	          if (newLength!=terms.length) {	        	  
	        	  if (newLength==1) {
	        		  output.addClause(new Clause(terms[0], c.isRequired(), c.isProhibited(), c.getConf()));
	              } 
	        	  else {
	        		  Term[] newTerms=new Term[newLength];
	        		  System.arraycopy(terms, 0, newTerms, 0, newLength);
	                  output.addClause(new Clause(new Phrase(newTerms), c.isRequired(), c.isProhibited(), c.getConf()));
	              }
	          }
	          else {
	        	  output.addClause(c);		        	  
	          }	           
	      }
	      else {
	    	  output.addClause(c);	      
	    	  if (c.getField().equals(Clause.DEFAULT_FIELD) && !c.isProhibited()) {
	    		  termsCounter++;  
	    	  }
	    	  else {
	    		  termsExtraCounter++;
	    	  }	      	    	  	      	    	 
	      }
	  }
	  
	  return output;
  }
      
  /**
   * @param searcherMaxHits  
   */
  public Hits search(Query query, int numHits, int maxHitsPerDup, String dedupField, String sortField, boolean reverse, boolean waybackQuery) throws IOException {
	return search(query, numHits, MATCHED_DOCS_CONST_IGNORE, maxHitsPerDup, dedupField, sortField, reverse, null, Integer.MAX_VALUE, waybackQuery);
  }
  
  public String getExplanation(Query query, Hit hit, PwaFunctionsWritable functions) throws IOException {
	return searcher.getExplanation(query, hit, functions); 
  }
  
  public String getExplanation(Query query, Hit hit) throws IOException {
    return searcher.getExplanation(query, hit, null);
  }  

  public HitDetails getDetails(Hit hit) throws IOException {
    return detailer.getDetails(hit);
  }
  
  public HitDetails[] getDetails(Hit[] hits) throws IOException {
	 return detailer.getDetails(hits);
  }
  
  /* BUG wayback 0000155 */
  public HitDetails[] getDetails(PwaRequestDetailsWritable details) throws IOException {
    return detailer.getDetails(details);
  }
  
  public Summary getSummary(HitDetails hit, Query query) throws IOException {
    return summarizer.getSummary(hit, query);
  }

  public Summary[] getSummary(HitDetails[] hits, Query query) throws IOException {
    return summarizer.getSummary(hits, query);
  }
  
  /* BUG nutchwax 0000616 */
  public Summary[] getSummary(PwaRequestSummaryWritable summaries) throws IOException {
	return summarizer.getSummary(summaries);
  }  

  public byte[] getContent(HitDetails hit) throws IOException {
    return content.getContent(hit);
  }

  public ParseData getParseData(HitDetails hit) throws IOException {
    return content.getParseData(hit);
  }

  public ParseText getParseText(HitDetails hit) throws IOException {
    return content.getParseText(hit);
  }

  public String[] getAnchors(HitDetails hit) throws IOException {
    return linkDb.getAnchors(hit);
  }

  public Inlinks getInlinks(HitDetails hit) throws IOException {
    return linkDb.getInlinks(hit);
  }  

  public long getFetchDate(HitDetails hit) throws IOException {
    return content.getFetchDate(hit);
  }

  public void close() throws IOException {
    if (content != null) { content.close(); }
    if (searcher != null) { searcher.close(); }
    if (linkDb != null) { linkDb.close(); }
    if (fs != null) { fs.close(); }
  }
  
  /** For debugging. */
  public static void main(String[] args) throws Exception {
    String usage = "NutchBean query";

    if (args.length == 0) {
      System.err.println(usage);
      System.exit(-1);
    }

    Configuration conf = NutchConfiguration.create();
    NutchBean bean = new NutchBean(conf);
    Query query = Query.parse(args[0], conf);
    Hits hits = bean.search(query, 10);
    System.out.println("Total hits: " + hits.getTotal());
    int length = (int)Math.min(hits.getTotal(), 10);
    Hit[] show = hits.getHits(0, length);
    HitDetails[] details = bean.getDetails(show);
    Summary[] summaries = bean.getSummary(details, query);

    for (int i = 0; i < hits.getLength(); i++) {
      System.out.println(" "+i+" "+ details[i] + "\n" + summaries[i]);
    }
  }

  public long getProtocolVersion(String className, long arg1) throws IOException {
    if(DistributedSearch.Protocol.class.getName().equals(className)){
      return 1;
    } else {
      throw new IOException("Unknown Protocol classname:" + className);
    }
  }



}
