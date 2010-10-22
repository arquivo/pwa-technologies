package org.apache.lucene.pruning;
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


import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.TermQuery;
//import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.index.*;
import org.apache.lucene.search.Filter;


/**
 * This class produces a subset of the input index, by removing postings data
 * for those terms where their in-document frequency is below a specified
 * threshold. The net effect of this processing is a much smaller index that for
 * many types of queries returns nearly identical top-N results as compared with
 * the original index, but with increased performance.
 * <p>
 * See the following paper for more details about this method: <a
 * href="http://portal.acm.org/citation.cfm?id=383958">Static index pruning for
 * information retrieval systems, D. Carmel at al, ACM SIGIR 2001 </a>.
 * Conclusions of this paper indicate that it's best to use per-term thresholds,
 * but in practice this is tedious for large number of terms - instead, this
 * implementation allows to specify three levels of thresholds: one default
 * threshold, then a threshold per field, and then a threshold per term. These
 * thresholds are applied so that always the most specific one takes precedence.
 * <p>
 * Please note that while this method produces good results for term queries it
 * often leads to poor results for phrase queries (because we remove postings
 * without considering whether they belong to an important phrase).
 * 
 * <p>Thresholds in this method of pruning are expressed as the percentage of
 * the top-N scoring documents per term that are retained. The list of top-N
 * documents is established by using a regular {@link IndexSearcher}
 * and {@link Similarity} to run a simple {@link TermQuery}.
 * <p>
 * <small>See the following papers for a discussion of this problem and the
 * proposed solutions to improve the quality of a pruned index (not implemented
 * here):
 * <ul>
 * <li><a href="http://portal.acm.org/citation.cfm?id=1148235">Pruned query
 * evaluation using pre-computed impacts, V. Anh et al, ACM SIGIR 2006</a></li>
 * <li><a href="http://portal.acm.org/citation.cfm?id=1183614.1183644"> A
 * document-centric approach to static index pruning in text retrieval systems,
 * S. Buettcher et al, ACM SIGIR 2006</a></li>
 * <li><a href=" http://oak.cs.ucla.edu/~cho/papers/ntoulas-sigir07.pdf">
 * Pruning Policies for Two-Tiered Inverted Index with Correctness Guarantee, A.
 * Ntoulas et al, ACM SIGIR 2007.</a></li>
 * </ul>
 * </small>
 * 
 * <p>
 * As the threshold values increase, the total size of the index decreases,
 * search performance increases, and recall decreases (i.e. search quality
 * deteriorates). NOTE: especially phrase recall deteriorates significantly at
 * higher threshold values.
 * <p>
 * Primary purpose of this class is to produce small first-tier indexes that fit
 * completely in RAM, and store these indexes using
 * {@link IndexWriter#addIndexes(IndexReader[])}. <b>NOTE: If the input index is
 * optimized (i.e. doesn't contain deletions) then the index produced via
 * {@link IndexWriter#addIndexes(IndexReader[])} will preserve internal document
 * id-s so that they are in sync with the original index.</b> This means that
 * all other auxiliary information not necessary for first-tier processing, such
 * as some stored fields, can also be removed, to be quickly retrieved on-demand
 * from the original index using the same internal document id. See
 * {@link StorePruningPolicy} for information about removing stored fields.
 * <p>
 * Threshold values can be specified globally (for terms in all fields) using
 * <code>defaultThreshold</code> parameter, and can be overriden using per-field
 * or per-term values supplied in a <code>thresholds</code> map. Keys in this
 * map are either field names, or terms in <code>field:text</code> format. The
 * precedence of these values is the following: first a per-term threshold is
 * used if present, then per-field threshold if present, and finally the default
 * threshold.
 */

public class CarmelTermPruningPolicy extends TermPruningPolicy {
  int docsPos = 0;
  float curThr;
  float defThreshold;
  Map<String, Float> thresholds;
  ScoreDoc[] docs = null;
  IndexSearcher is;
  Similarity sim;

  @SuppressWarnings("unchecked")
  protected CarmelTermPruningPolicy(IndexReader in,
          Map<String, Integer> fieldFlags, Map<String, Float> thresholds,
          float defThreshold, Similarity sim) {
    super(in, fieldFlags);
    this.defThreshold = defThreshold;
    if (thresholds != null) {
      this.thresholds = thresholds;
    } else {
      this.thresholds = Collections.EMPTY_MAP;
    }
    if (sim != null) {
      this.sim = sim;
    } else {
      sim = new DefaultSimilarity();
    }
    is = new IndexSearcher(in);
    is.setSimilarity(sim);
  }

  // too costly - pass everything at this stage
  @Override
  public boolean pruneTermEnum(TermEnum te) throws IOException {
    return false;
  }

  @Override
  public void initTermPositions(TermPositions tp, Term t) throws IOException {
    curThr = defThreshold;
    String termKey = t.field() + ":" + t.text();
    if (thresholds.containsKey(termKey)) {
      curThr = thresholds.get(termKey);
    } else if (thresholds.containsKey(t.field())) {
      curThr = thresholds.get(t.field());
    }
    // calculate count
    int df = in.docFreq(t);
    int count = Math.round((float)df * curThr);
    if (count < 100) count = 100;
    TopScoreDocCollector collector = TopScoreDocCollector.create(count, true);
    TermQuery tq = new TermQuery(t);
    //is.search(tq, collector); TODO MC
    if (1==1) {
    	throw new IOException("Do no use this class"); // TODO MC
    }
    docs = collector.topDocs().scoreDocs;
    Arrays.sort(docs, ByDocComparator.INSTANCE);
    if (docs.length > count) {
      ScoreDoc[] subset = new ScoreDoc[count];
      System.arraycopy(docs, 0, subset, 0, count);
      docs = subset;
    }
    docsPos = 0;
  }

  @Override
  public boolean pruneTermPositions(TermPositions termPositions, Term t)
          throws IOException {
    if (docsPos >= docs.length) { // used up all doc id-s
      return true; // skip any remaining docs
    }
    while ((docsPos < docs.length - 1) && termPositions.doc() > docs[docsPos].doc) {
      docsPos++;
    }
    if (termPositions.doc() == docs[docsPos].doc) {
      // pass
      docsPos++; // move to next doc id
      return false;
    } else if (termPositions.doc() < docs[docsPos].doc) {
      return true; // skip this one - it's less important
    }
    // should not happen!
    throw new IOException("termPositions.doc > docs[docsPos].doc");
  }

  // it probably doesn't make sense to prune term vectors using this method,
  // due to its overhead
  @Override
  public int pruneTermVectorTerms(int docNumber, String field, String[] terms,
          int[] freqs, TermFreqVector tfv) throws IOException {
    return 0;
  }

  public static class ByDocComparator implements Comparator<ScoreDoc> {
    public static final ByDocComparator INSTANCE = new ByDocComparator();

    @Override
    public int compare(ScoreDoc o1, ScoreDoc o2) {
      return o1.doc - o2.doc;
    }    
  }
  
}
