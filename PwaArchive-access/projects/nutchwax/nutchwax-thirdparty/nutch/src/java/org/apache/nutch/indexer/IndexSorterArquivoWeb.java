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

package org.apache.nutch.indexer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.lucene.index.*;
import org.apache.lucene.document.*;
import org.apache.lucene.store.*;
import org.apache.lucene.search.*;

import org.apache.nutch.util.NutchConfiguration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.StringUtils;
import org.apache.hadoop.util.ToolBase;

/** Sort a Nutch index by page score.  
 * Higher scoring documents are assigned smaller document numbers. */
public class IndexSorterArquivoWeb extends ToolBase {
  private static final Log LOG = LogFactory.getLog(IndexSorterArquivoWeb.class);
  private static final String INCLUDE_EXTENSIONS_KEY="arquivo.include.types";  
  
  private static class PostingMap implements Comparable {
    private int newDoc;
    private long offset;

    public int compareTo(Object o) {              // order by newDoc id
      return this.newDoc - ((PostingMap)o).newDoc;
    }
  }

  private static class SortedTermPositions implements TermPositions {
    private TermPositions original;
    private int[] oldToNew;

    private int docFreq;

    private PostingMap[] postingMaps = new PostingMap[0];
    private int pointer;

    private int freq;
    private int position;

    private static final String TEMP_FILE = "temp";
    private final RAMDirectory tempDir = new RAMDirectory();
    private final RAMOutputStream out =
      (RAMOutputStream)tempDir.createOutput(TEMP_FILE);
    private IndexInput in;

    public SortedTermPositions(TermPositions original, int[] oldToNew) {
      this.original = original;
      this.oldToNew = oldToNew;
    }

    public void seek(Term term) throws IOException {
      throw new UnsupportedOperationException();
    }

    public void seek(TermEnum terms) throws IOException {
      original.seek(terms);

      docFreq = terms.docFreq();
      pointer = -1;

      if (docFreq > postingMaps.length) {         // grow postingsMap
        PostingMap[] newMap = new PostingMap[docFreq];
        System.arraycopy(postingMaps, 0, newMap, 0, postingMaps.length);
        for (int i = postingMaps.length; i < docFreq; i++) {
          newMap[i] = new PostingMap();
        }
        postingMaps = newMap;
      }

      out.reset();

      int i = 0;
      while (original.next()) {
        PostingMap map = postingMaps[i++];
        map.newDoc = oldToNew[original.doc()];    // remap the newDoc id
        map.offset = out.getFilePointer();        // save pointer to buffer

        final int tf = original.freq();           // buffer tf & positions
        out.writeVInt(tf);
        int prevPosition = 0;
        for (int j = tf; j > 0; j--) {            // delta encode positions
          int p = original.nextPosition();
          out.writeVInt(p - prevPosition);
          prevPosition = p;
        }
      }
      out.flush();
      docFreq = i;                                // allow for deletions
      
      Arrays.sort(postingMaps, 0, docFreq);       // resort by mapped doc ids
      //HeapSorter.sort(postingMaps,docFreq); // TODO MC - due to the lack of space

      // NOTE: this might be substantially faster if RAMInputStream were public
      // and supported a reset() operation.
      in = tempDir.openInput(TEMP_FILE);
    }
        
    public boolean next() throws IOException {
      pointer++;
      if (pointer < docFreq) {
        in.seek(postingMaps[pointer].offset);
        freq = in.readVInt();
        position = 0;
        return true;
      }
      return false;
    }
      
    public int doc() { return postingMaps[pointer].newDoc; }
    public int freq() { return freq; }

    public int nextPosition() throws IOException {
      int positionIncrement = in.readVInt();
      position += positionIncrement;
      return position;
    }

    public int read(int[] docs, int[] freqs) {
      throw new UnsupportedOperationException();
    }
    public boolean skipTo(int target) {
      throw new UnsupportedOperationException();
    }

    public void close() throws IOException {
      original.close();
    }

  }

  private static class SortingReader extends FilterIndexReader {
    
    private int[] oldToNew;
    private DocScore[] newToOld;

    //public SortingReader(IndexReader oldReader, int[] oldToNew) { // TODO MC
    public SortingReader(IndexReader oldReader, DocScore[] newToOld) { // TODO MC
      super(oldReader);
      this.newToOld = newToOld;
      
      this.oldToNew = new int[oldReader.maxDoc()];
      int newDoc = 0;
      while (newDoc < newToOld.length) {
        int oldDoc = newToOld[newDoc].doc;
       	oldToNew[oldDoc] = newDoc;
        newDoc++;
      }      
    }

    public Document document(int n) throws IOException {
      return super.document(newToOld[n].doc);
    }

    public Document document(int n, FieldSelector fieldSelector) throws IOException {
      return super.document(newToOld[n].doc, fieldSelector);
    }    
    
    public boolean isDeleted(int n) {
      return false;       
      //return newToOld[n].score<0; // TODO MC - to erase not searchable types
    }

    public byte[] norms(String f) throws IOException {
      throw new UnsupportedOperationException();
    }

    public void norms(String f, byte[] norms, int offset) throws IOException {
      byte[] oldNorms = super.norms(f);
      int oldDoc = 0;
      //while (oldDoc < oldNorms.length) { TODO MC
      while (oldDoc < oldToNew.length) { 
        int newDoc = oldToNew[oldDoc];
        //if (newDoc != -1) { TODO MC
          //norms[newDoc] = oldNorms[oldDoc]; TODO MC 
        System.arraycopy(oldNorms, oldDoc*4, norms, newDoc*4, 4); // TODO MC copy lengths instead of norms
        //} TODO MC
        oldDoc++;
      }
    }

    protected void doSetNorm(int d, String f, byte b) throws IOException {
      throw new UnsupportedOperationException();
    }

    public TermDocs termDocs() throws IOException {
      throw new UnsupportedOperationException();
    }
    
    public TermPositions termPositions() throws IOException {
      return new SortedTermPositions(super.termPositions(), oldToNew);
    }

    protected void doDelete(int n) throws IOException { 
      throw new UnsupportedOperationException();
    }

  }

  private static class DocScore implements Comparable {
    private int doc;
    private float score;

    public int compareTo(Object o) {              // order by score, doc
      DocScore that = (DocScore)o;
      if (this.score == that.score) {
        return this.doc - that.doc;
      } else {
        return this.score < that.score ? 1 : -1 ;
      }
    }
    
    public String toString() {
      return "doc=" + doc + ",score=" + score;
    }
  }

  public IndexSorterArquivoWeb() {
    
  }
  
  public IndexSorterArquivoWeb(Configuration conf) {	 
    setConf(conf);          
  }
  
  public void sort(File directory) throws IOException {
    LOG.info("IndexSorter: starting.");
    Date start = new Date();
    int termIndexInterval = getConf().getInt("indexer.termIndexInterval", 128);
    IndexReader reader = IndexReader.open(new File(directory, "index"));
	Searcher searcher = new IndexSearcher(new File(directory, "index").getAbsolutePath()); // TODO MC

    SortingReader sorter = new SortingReader(reader, newToOld(reader,searcher)); // TODO MC
    IndexWriter writer = new IndexWriter(new File(directory, "index-sorted"),
                                         null, true);
    writer.setTermIndexInterval
      (termIndexInterval);
    writer.setUseCompoundFile(false);
    writer.addIndexes(new IndexReader[] { sorter });
    writer.close();
    Date end = new Date();
    LOG.info("IndexSorter: done, " + (end.getTime() - start.getTime())
        + " total milliseconds");
  }

  /**
   * Sort the documents by score
   * @param reader
   * @param searcher
   * @return
   * @throws IOException
   */
  //private static int[] oldToNew(IndexReader reader, Searcher searcher) throws IOException {
  private static DocScore[] newToOld(IndexReader reader, Searcher searcher) throws IOException {
    int readerMax = reader.maxDoc();           
    DocScore[] newToOld = new DocScore[readerMax];
    
    // use site, an indexed, un-tokenized field to get boost
    //byte[] boosts = reader.norms("site"); TODO MC
    /* TODO MC */
    Document docMeta;         
    Pattern includes = Pattern.compile("\\|");
	String value = NutchConfiguration.create().get(INCLUDE_EXTENSIONS_KEY, "");
	String includeExtensions[] = includes.split(value);
	Hashtable<String,Boolean> validExtensions=new Hashtable<String,Boolean>();
	for (int i = 0; i < includeExtensions.length; i++) {
		validExtensions.put(includeExtensions[i], true);	
		System.out.println("extension boosted "+includeExtensions[i]);
	}	  	 
	 /* TODO MC */
    
    for (int oldDoc = 0; oldDoc < readerMax; oldDoc++) {
      float score;
      if (reader.isDeleted(oldDoc)) {
        //score = 0.0f;    	
    	score = -1f; // TODO MC
      } 
      else {    	 
        //score = Similarity.decodeNorm(boosts[oldDoc]); TODO MC
    	/* TODO MC */
    	docMeta=searcher.doc(oldDoc);	
    	if (validExtensions.get(docMeta.get("subType"))==null) { // searched extensions will have higher scores 
    		score=-0.5f;
    	}
    	else {
    		score=Integer.parseInt(docMeta.get("inlinks")); 
    		/*
    		if (score==0) {
    			score=0.001f; // TODO MC - to not erase
    		}
    		*/
    	}
    	/* TODO MC */
    	//System.out.println("Score for old document "+oldDoc+" is "+score+" and type "+docMeta.get("subType")); // TODO MC debug remove
      }
      DocScore docScore = new DocScore();
      docScore.doc = oldDoc;
      docScore.score = score;
      newToOld[oldDoc] = docScore;
    }

    System.out.println("Sorting "+newToOld.length+" documents.");
    Arrays.sort(newToOld);    
    //HeapSorter.sort(newToOld); // TODO MC - due to the lack of space
    
    /* TODO MC
    int[] oldToNew = new int[readerMax];
    for (int newDoc = 0; newDoc < readerMax; newDoc++) {
      DocScore docScore = newToOld[newDoc];
      //oldToNew[docScore.oldDoc] = docScore.score > 0.0f ? newDoc : -1; // TODO MC
      oldToNew[docScore.oldDoc] = newDoc; // TODO MC
    } 
    */
        
    /* TODO MC *
    for (int newDoc = 0; newDoc < readerMax; newDoc++) {
    	DocScore docScore = newToOld[newDoc];
    	System.out.println("Score for new document "+newDoc+" is "+docScore.score); // TODO MC debug remove
    }
    * TODO MC */
    
    //return oldToNew; TODO MC
    return newToOld; // TODO MC
  }
    

  /** */
  public static void main(String[] args) throws Exception {
    int res = new IndexSorterArquivoWeb().doMain(NutchConfiguration.create(), args);
    System.exit(res);
  }
  
  public int run(String[] args) throws Exception {
    File directory;
      
    String usage = "IndexSorter directory";

    if (args.length < 1) {
      System.err.println("Usage: " + usage);
      return -1;
    }

    directory = new File(args[0]);

    try {
      sort(directory);
      return 0;
    } catch (Exception e) {
      LOG.fatal("IndexSorter: " + StringUtils.stringifyException(e));
      return -1;
    }
  }

}
