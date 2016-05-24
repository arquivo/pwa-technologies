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

import org.apache.lucene.search.*;
import org.apache.lucene.search.queries.PwaSortQuery;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.QueryFilter;
import org.apache.lucene.index.Term;
import org.apache.lucene.misc.ChainedFilter;
import org.apache.nutch.searcher.DistributedSearch;
import org.apache.nutch.global.Global;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;

import java.io.IOException;


/** Utility which converts certain query clauses into {@link QueryFilter}s and
 * caches these.  Only required clauses whose boost is zero are converted to
 * cached filters.  Range queries are converted to range filters.  This
 * accellerates query constraints like date, language, document format, etc.,
 * which do not affect ranking but might otherwise slow search considerably. */
class LuceneQueryOptimizer {

  public static final Log LOG = LogFactory.getLog(LuceneQueryOptimizer.class);
	
  // This thread provides a pseudo-clock service to all searching
  // threads, so that they can count elapsed time with less overhead than
  // repeatedly calling System.currentTimeMillis.
  private TimerThread timerThread = null;

  private static class TimerThread extends Thread {
    private int tick;
    // NOTE: we can avoid explicit synchronization here for several reasons:
    // * updates to 32-bit-sized variables are atomic
    // * only single thread modifies this value
    // * use of volatile keyword ensures that it does not reside in
    //   a register, but in main memory (so that changes are visible to
    //   other threads).
    // * visibility of changes does not need to be instantanous, we can
    //   afford losing a tick or two.
    //
    // See section 17 of the Java Language Specification for details.
    public volatile int timeCounter = 0;

    boolean running = true;

    public TimerThread(int tick) {
      super("LQO timer thread");
      this.tick = tick;
      this.setDaemon(true);
    }

    public void run() {
      while(running) {
        timeCounter++;
        try {
          Thread.sleep(tick);
        } 
        catch (InterruptedException ie) 
        {
        	// ignore
        };
      }
    }
  }

  private void initTimerThread(int p) {
    if (timerThread == null || !timerThread.isAlive()) {
      timerThread = new TimerThread(p);
      timerThread.start();
    }
  }
  
  private static class TimeExceeded extends RuntimeException {
    public long maxTime;
    private int maxDoc;
    
    public TimeExceeded(long maxTime, int maxDoc) {
      super("Exceeded search time: " + maxTime + " ms.");
      this.maxTime = maxTime;
      this.maxDoc = maxDoc;
    }
  }

  
  private static class LimitedCollector extends TopDocCollector {
    private int maxHits;
    private int maxTicks;
    private int startTicks;
    private TimerThread timer;
    private int curTicks;

    public LimitedCollector(int numHits, int maxHits, int maxTicks, TimerThread timer, boolean reverse) {
      super(numHits, reverse);
      this.maxHits = maxHits;
      this.maxTicks = maxTicks;
      if (timer != null) {
    	this.timer = timer;
        this.startTicks = timer.timeCounter;
      }
    }

    public void collect(int doc, float score) {
      if (maxHits > 0 && getTotalHits() >= maxHits) {
        throw new LimitExceeded(doc);
      }
      if (timer != null) {
        curTicks = timer.timeCounter;
        // overflow check
        if (curTicks < startTicks) curTicks += Integer.MAX_VALUE;
        if (curTicks - startTicks > maxTicks) {
          throw new TimeExceeded(timer.tick * (curTicks - startTicks), doc);
        }
      }
      super.collect(doc, score);
    }
  }  
  
  private static class LimitExceeded extends RuntimeException {
    private int maxDoc;
    public LimitExceeded(int maxDoc) { this.maxDoc = maxDoc; }    
  }
  
  
  private float threshold;
  private int maxFulltextMatchesRanked;
  private int tickLength;
  private int maxTickCount;  
  private int timeoutResponse;
  private String cacheType;
  
  
  /**
   * Construct an optimizer that caches and uses filters for required clauses
   * whose boost is zero.
   * 
   * @param cacheSize
   *          the number of QueryFilters to cache
   * @param threshold
   *          the fraction of documents which must contain a term
   */
  public LuceneQueryOptimizer(Configuration conf) {
    final int cacheSize = conf.getInt("searcher.filter.cache.size", 16);
    this.threshold = conf.getFloat("searcher.filter.cache.threshold", 0.05f);       
    this.tickLength = conf.getInt("searcher.max.time.tick_length", 200);
    this.maxTickCount = conf.getInt("searcher.max.time.tick_count", -1);
    this.maxFulltextMatchesRanked = conf.getInt(Global.MAX_FULLTEXT_MATCHES_RANKED, -1);
    this.timeoutResponse = conf.getInt(Global.TIMEOUT_INDEX_SERVERS_RESPONSE, -1);
    if (timeoutResponse>0) { 
    	this.maxTickCount=timeoutResponse;
    	this.tickLength=1000;
    }       
    if (this.maxTickCount > 0) {
    	initTimerThread(this.tickLength);
    }       
  }

  public TopDocs optimize(BooleanQuery original, Searcher searcher, int numHits, String sortField, boolean reverse) throws IOException {
    BooleanQuery query = new BooleanQuery(); 
    Filter filter = null;

    BooleanClause[] clauses = original.getClauses();
    for (int i = 0; i < clauses.length; i++) {
      BooleanClause c = clauses[i];
      if (c.isRequired() && c.getQuery().getBoost() == 0.0f) {   // boost is zero

    	  	if (c.getQuery() instanceof TermQuery     // TermQuery
    	  			&& (searcher.docFreq(((TermQuery)c.getQuery()).getTerm()) / (float)searcher.maxDoc()) < threshold) { // beneath threshold
    	  		query.add(c);                          
    	  	}          
    	  	else if (c.getQuery() instanceof RangeQuery) { // RangeQuery        
    	  		query.add(c);     	  		
    	  	}       
      }
      else {
    	  query.add(c);                               // query it
      }
    }
    
    query.setFunctions(original.getFunctions());  
    if (sortField!=null) { // to sort result by sortField
    	query.add(new PwaSortQuery(sortField,reverse), BooleanClause.Occur.MUST); 
    }
    
    // print query
    LOG.info("Query:"+query.toString());   
    
    // no hit limit
    if (this.maxFulltextMatchesRanked <= 0 && timerThread == null)  {
    	return searcher.search(query, filter, numHits);
    }

    // hits limited in time or in count -- use a LimitedCollector
    LimitedCollector collector = new LimitedCollector(numHits, maxFulltextMatchesRanked, maxTickCount, timerThread, (sortField!=null) ? !reverse : reverse);
    LimitExceeded exceeded = null;
    TimeExceeded timeExceeded = null;
    try {
    	searcher.search(query, filter, collector);
    } 
    catch (LimitExceeded le) {
    	exceeded = le;
    }
    catch (TimeExceeded te) {
    	timeExceeded = te;
    }
    TopDocs results = collector.topDocs();
    if (exceeded != null) {                     // limit was exceeded
    	results.totalHits = (int)(results.totalHits*(searcher.maxDoc()/(float)exceeded.maxDoc)); // estimate totalHits
    } 
    else if (timeExceeded != null) {
    	results.totalHits = (int)(results.totalHits * (searcher.maxDoc()/(float)timeExceeded.maxDoc));
    }
    return results;              
  }
  

  /** 
   * @param numHits number of top results
   * @param maxFulltextMatchesRanked number of matched documents for ranking
   */
  public TopDocs optimize(BooleanQuery original, Searcher searcher, int numHits, int maxFulltextMatchesRanked, String sortField, boolean reverse) throws IOException {
	  if (maxFulltextMatchesRanked!=NutchBean.MATCHED_DOCS_CONST_IGNORE) {
		  this.maxFulltextMatchesRanked=maxFulltextMatchesRanked;
	  }
	  return optimize(original, searcher, numHits, sortField, reverse);
  }

}
