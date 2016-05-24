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


import java.util.HashMap;
import java.util.Map;
import java.io.File;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.pruning.PruningPolicy;
import org.apache.lucene.pruning.PruningReader;
import org.apache.lucene.pruning.StorePruningPolicy;
import org.apache.lucene.pruning.TFTermPruningPolicy;
//import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.index.*;

import junit.framework.TestCase;

 

public class TestPruningReader extends TestCase {
  private RAMDirectory sourceDir = new RAMDirectory();	 	
  //private Directory sourceDir = FSDirectory.getDirectory(INDEX_DIR, false);
  
  private void assertTD(IndexReader ir, Term t, int[] ids) throws Exception {
    TermPositions td = ir.termPositions(t);
    assertNotNull(td);
    try {
      int i = 0;
      while(td.next()) {
        assertEquals(t + ", i=" + i, ids[i], td.doc());
        i++;
      }
      assertEquals(ids.length, i);
    } finally {
      td.close();
    }
  }
  
  private void assertTDCount(IndexReader ir, Term t, int count) throws Exception {
    TermPositions td = ir.termPositions(t);
    assertNotNull(td);
    try {
      int i = 0;
      while (td.next()) i++;
      assertEquals(t.toString(), count, i);
    } finally {
      td.close();
    }
  }
  
  public void setUp() throws Exception {	 
    IndexWriter iw = new IndexWriter(sourceDir, new WhitespaceAnalyzer(),/*MaxFieldLength.LIMITED*/ true);
    Document doc = new Document();
    //doc.add(new Field("body", "one two three four", Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("body", "one two three four", Field.Store.YES, Field.Index.TOKENIZED));
    doc.add(new Field("id", "0", Field.Store.YES, Field.Index.NO));
    iw.addDocument(doc);
    doc = new Document();
    //doc.add(new Field("body", "one two three one two three", Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("body", "one two three one two three", Field.Store.YES, Field.Index.TOKENIZED));
    doc.add(new Field("id", "1", Field.Store.YES, Field.Index.NO));
    iw.addDocument(doc);
    doc = new Document();
    //doc.add(new Field("body", "one two one two one two", Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("body", "one two one two one two", Field.Store.YES, Field.Index.TOKENIZED));
    doc.add(new Field("id", "2", Field.Store.YES, Field.Index.NO));
    iw.addDocument(doc);
    doc = new Document();
    //doc.add(new Field("body", "one three one three one three", Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("body", "one three one three one three", Field.Store.YES, Field.Index.TOKENIZED));
    doc.add(new Field("id", "3", Field.Store.YES, Field.Index.NO));
    iw.addDocument(doc);
    doc = new Document();
    //doc.add(new Field("body", "one one one one two", Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("body", "one one one one two", Field.Store.YES, Field.Index.TOKENIZED));
    //doc.add(new Field("test", "one two one two three three three four", Field.Store.YES, Field.Index.ANALYZED_NO_NORMS, Field.TermVector.WITH_POSITIONS_OFFSETS));
    doc.add(new Field("test", "one two one two three three three four", Field.Store.YES, Field.Index.TOKENIZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
    doc.add(new Field("id", "4", Field.Store.YES, Field.Index.NO));
    iw.addDocument(doc);
    // to be deleted
    doc = new Document();
    //doc.add(new Field("body", "one three one three one three five five five", Field.Store.YES, Field.Index.ANALYZED));
    doc.add(new Field("body", "one three one three one three five five five", Field.Store.YES, Field.Index.TOKENIZED));
    doc.add(new Field("id", "5", Field.Store.YES, Field.Index.NO));
    iw.addDocument(doc);
    iw.close();
    //IndexReader ir = IndexReader.open(sourceDir, false); 
    IndexReader ir = IndexReader.open(sourceDir);
    ir.deleteDocument(5);
    ir.close();
  }

  public void testTfPruning() throws Exception {
    RAMDirectory targetDir = new RAMDirectory();
    //IndexReader in = IndexReader.open(sourceDir, true); 
    IndexReader in = IndexReader.open(sourceDir);
    TFTermPruningPolicy tfp = new TFTermPruningPolicy(in, null, null, 2);
    PruningReader tfr = new PruningReader(in, null, tfp);
    // verify
    assertTD(tfr, new Term("body", "one"), new int[]{1, 2, 3, 4});
    assertTD(tfr, new Term("body", "two"), new int[]{1, 2});
    assertTD(tfr, new Term("body", "three"), new int[]{1, 3});
    assertTD(tfr, new Term("test", "one"), new int[]{4});
    assertTDCount(tfr, new Term("body", "four"), 0);
    assertTDCount(tfr, new Term("test", "four"), 0);
    // verify new reader
    IndexWriter iw = new IndexWriter(targetDir, new WhitespaceAnalyzer(), /*MaxFieldLength.LIMITED*/ true);
    iw.addIndexes(new IndexReader[]{tfr});
    iw.close();
    //IndexReader ir = IndexReader.open(targetDir, true); 
    IndexReader ir = IndexReader.open(targetDir);
    assertTD(ir, new Term("body", "one"), new int[]{1, 2, 3, 4});
    assertTD(ir, new Term("body", "two"), new int[]{1, 2});
    assertTD(ir, new Term("body", "three"), new int[]{1, 3});
    assertTD(ir, new Term("test", "one"), new int[]{4});
    tfr.close();
    ir.close();
  }
  
  public void testThresholds() throws Exception {
    Map<String, Integer> thresholds = new HashMap<String, Integer>();
    thresholds.put("test", 3);
    //IndexReader in = IndexReader.open(sourceDir, true); 
    IndexReader in = IndexReader.open(sourceDir);
    TFTermPruningPolicy tfp = new TFTermPruningPolicy(in, null, thresholds, 2);
    PruningReader tfr = new PruningReader(in, null, tfp);
    assertTDCount(tfr, new Term("test", "one"), 0);
    assertTDCount(tfr, new Term("test", "two"), 0);
    assertTD(tfr, new Term("test", "three"), new int[]{4});
    assertTDCount(tfr, new Term("test", "four"), 0);
  }
  
  public void testRemoveFields() throws Exception {
    RAMDirectory targetDir = new RAMDirectory();
    Map<String, Integer> removeFields = new HashMap<String, Integer>();
    removeFields.put("test", PruningPolicy.DEL_POSTINGS | PruningPolicy.DEL_STORED);
    //IndexReader in = IndexReader.open(sourceDir, true); 
    IndexReader in = IndexReader.open(sourceDir);
    TFTermPruningPolicy tfp = new TFTermPruningPolicy(in, removeFields, null, 2);
    StorePruningPolicy stp = new StorePruningPolicy(in, removeFields);
    PruningReader tfr = new PruningReader(in, stp, tfp);
    Document doc = tfr.document(4);
    // removed stored values?
    assertNull(doc.get("test"));
    // removed postings ?
    TermEnum te = tfr.terms();
    while (te.next()) {
      assertFalse("test".equals(te.term().field()));
    }
    // but vectors should be present !
    TermFreqVector tv = tfr.getTermFreqVector(4, "test");
    assertNotNull(tv);
    assertEquals(4, tv.getTerms().length); // term "four" not deleted yet from TermEnum
    // verify new reader
    IndexWriter iw = new IndexWriter(targetDir, new WhitespaceAnalyzer(), /*MaxFieldLength.LIMITED*/ true); 
    iw.addIndexes(new IndexReader[]{tfr});
    iw.close();
    //IndexReader ir = IndexReader.open(targetDir, true); 
    IndexReader ir = IndexReader.open(targetDir);
    tv = ir.getTermFreqVector(4, "test");
    assertNotNull(tv);
    assertEquals(3, tv.getTerms().length); // term "four" was deleted from TermEnum
  }
}
