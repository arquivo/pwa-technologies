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


import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiReader;
//import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;


/**
 * A command-line tool to configure and run a {@link PruningReader} on an input
 * index and produce a pruned output index using
 * {@link IndexWriter#addIndexes(IndexReader...)}.
 */
public class PruningTool {

  public static void main(String[] args) throws Exception {
    int res = run(args);
    System.exit(res);
  }
  
  public static int run(String[] args) throws Exception {
    if (args.length < 5) {
      System.err.println("Usage: PruningTool -impl (tf | carmel | arquivo) (-in <path1> [-in <path2> ...]) -out <outPath> -t <NN> [-del f1,f2,..] [-conf <file>]");
      System.err.println("\t-impl (tf | carmel | arquivo)\timplementation name: TFPruningReader or CarmelPruningReader or PwaPruningReader");
      System.err.println("\t-in path\tpath to the input index. Can specify multiple input indexes.");
      System.err.println("\t-out path\toutput path where the output index will be stored.");
      System.err.println("\t-t NN\tdefault threshold value (minimum in-document frequency) for all terms");
      System.err.println("\t-del f1,f2,..\tcomma-separated list of field specs to delete (postings, vectors & stored):");
      System.err.println("\t\tfield spec : fieldName ( ':' [pPsv] )");
      System.err.println("\t\twhere: p - postings, P - payloads, s - stored value, v - vectors");
      System.err.println("\t-conf file\tpath to config file with per-term thresholds");
      return -1;
    }
    ArrayList<IndexReader> inputs = new ArrayList<IndexReader>();
    Searcher searcher = null; 
    Directory out = null;
    float thr = -1;
    Map<String, Integer> delFields = new HashMap<String, Integer>();
    String impl = null;
    for (int i = 0; i < args.length; i++) {
      if (args[i].equals("-in")) {
    	File fdir=null; 
        Directory d = FSDirectory.getDirectory(fdir=new File(args[++i]));
        if (!IndexReader.indexExists(d)) {
          System.err.println("WARN: no index in " + args[i] + ", skipping ...");
        }               
        //inputs.add(IndexReader.open(d, true));        
        inputs.add(IndexReader.open(d));
        
        searcher = new IndexSearcher(fdir.getAbsolutePath()); 
      } else if (args[i].equals("-out")) {
        File outFile = new File(args[++i]);
        if (outFile.exists()) {
          throw new Exception("Output " + outFile + " already exists.");
        }
        outFile.mkdirs();
        out = FSDirectory.getDirectory(outFile);
      } else if (args[i].equals("-impl")) {
        impl = args[++i];
      } else if (args[i].equals("-t")) {
        thr = Float.parseFloat(args[++i]);
      } else if (args[i].equals("-del")) {
        String[] fields = args[++i].split(",");
        for (String f : fields) {
          // parse field spec
          String[] spec = f.split(":");
          int opts = PruningPolicy.DEL_ALL;
          if (spec.length > 0) {
            opts = 0;
            if (spec[1].indexOf('p') != -1) {
              opts |= PruningPolicy.DEL_POSTINGS;
            }
            if (spec[1].indexOf('P') != -1) {
              opts |= PruningPolicy.DEL_PAYLOADS;
            }
            if (spec[1].indexOf('s') != -1) {
              opts |= PruningPolicy.DEL_STORED;
            }
            if (spec[1].indexOf('v') != -1) {
              opts |= PruningPolicy.DEL_VECTOR;
            }
          }
          delFields.put(spec[0], opts);
        }
      } else if (args[i].equals("-conf")) {
        ++i;
        System.err.println("WARN: -conf option not implemented yet.");
      } else {
        throw new Exception("Invalid argument: '" + args[i] + "'");
      }
    }
    if (impl == null) {
      throw new Exception("Must select algorithm implementation");
    }
    if (inputs.size() == 0) {
      throw new Exception("At least one input index is required.");
    }
    if (out == null) {
      throw new Exception("Output path is not set.");
    }
    if (thr == -1) {
      throw new Exception("Threshold value is not set.");
    }
    IndexReader in;
    if (inputs.size() == 1) {
      in = inputs.get(0);
    } else {
      //in = new MultiReader((IndexReader[])inputs.toArray(new IndexReader[inputs.size()]), true); 
      in = new MultiReader((IndexReader[])inputs.toArray(new IndexReader[inputs.size()]));
    }
    if (in.hasDeletions()) {
      System.err.println("WARN: input index(es) with deletions - document ID-s will NOT be preserved!");
    }
    IndexReader pruning = null;
    StorePruningPolicy stp = null;
    if (delFields.size() > 0) {
      stp = new StorePruningPolicy(in, delFields);
    }
    TermPruningPolicy tpp = null;
    if (impl.equals("tf")) {
      tpp = new TFTermPruningPolicy(in, delFields, null, (int)thr);
    } else if (impl.equals("carmel")) {
      tpp = new CarmelTermPruningPolicy(in, delFields, null, thr, null);      
    } 
    else if (impl.equals("arquivo")) { // BUG added this new pruning policy                  
      tpp = new PwaTermPruningPolicy(in, searcher, delFields);
    }
    else {
      throw new Exception("Unknown algorithm: '" + impl + "'");
    }
    pruning = new PruningReader(in, stp, tpp);
    //IndexWriter iw = new IndexWriter(out, new WhitespaceAnalyzer(), MaxFieldLength.UNLIMITED); 
    IndexWriter iw = new IndexWriter(out, new WhitespaceAnalyzer(), true);       
    iw.setUseCompoundFile(false);
    iw.addIndexes(new IndexReader[]{pruning});
    iw.close();
    System.err.println("DONE.");
    return 0;
  }
}
