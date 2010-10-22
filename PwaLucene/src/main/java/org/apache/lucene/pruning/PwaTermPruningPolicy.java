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
import java.util.*;
import java.util.regex.Pattern;

import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.document.*;



/**
 * This class reduces the index by removing postings that are not shown in results, such as CSS or images. 
 * The net effect of this processing is a much smaller index that returns identical results.
 * 
 * @author Miguel Costa
 */
public class PwaTermPruningPolicy extends TermPruningPolicy {
  protected final static String EXTENSIONS="html|plain|pdf|postscript|xml|x-shockwave-flash|xhtml+xml|sgml|msword|mspowerpoint|vnd|vnd.ms-powerpoint|rtf|richtext";    
  protected Searcher searcher;
  protected Set<String> fieldsToPrune;
  protected BitSet validDocs;
  
 
  @SuppressWarnings("unchecked")
  protected PwaTermPruningPolicy(IndexReader in, Searcher searcher, Map<String, Integer> fieldFlags) throws IOException {	  		  
    super(in, fieldFlags);
    this.searcher=searcher;
        
    System.out.println("Valid extensions:");
    Pattern includes = Pattern.compile("\\|");
	//String value = NutchConfiguration.create().get(INCLUDE_EXTENSIONS_KEY, "");
    String value=EXTENSIONS; // TODO get from stdin or xml
	String includeExtensions[] = includes.split(value);
	Set<String> validExtensions=new HashSet<String>();	
	for (int i = 0; i < includeExtensions.length; i++) {
		validExtensions.add(includeExtensions[i]);	
		System.out.println("extension boosted "+includeExtensions[i]);
	}
    	               
    System.out.println("START: Analyzing valid documents.");
    Document docMeta;
    int maxDoc=in.maxDoc();
    validDocs=new BitSet(maxDoc);       
    for (int i=0;i<maxDoc;i++) {
    	docMeta=searcher.doc(i);
		if (validExtensions.contains(docMeta.get("subType"))) {   
			validDocs.set(i,true);	
		}
		else {
			validDocs.set(i,false);
		}
    }
    System.out.println("END: Analyzing valid documents.");
    
    // fields to prune
    fieldsToPrune = new HashSet<String>();
    fieldsToPrune.add("content");
    fieldsToPrune.add("title");
    fieldsToPrune.add("anchor");
    fieldsToPrune.add("url");
    fieldsToPrune.add("host");   
  }
  
  /**
   * Check if the term has only documents that are not shown in results  
   * @param term
   * @return
   * @throws IOException
   */
  private boolean pruneTerm(Term term) throws IOException {
	  	  	 
	if (!fieldsToPrune.contains(term.field())) {
		return false;
	}
	  	
	boolean prune=true;
	TermDocs td=in.termDocs(term);	
	while (td.next() && prune) {		
		if (validDocs.get(td.doc())) {
			prune=false;
		}
	 }
	 td.close();	 
  	 return prune;
  }
  

  @Override
  public boolean pruneTermEnum(TermEnum te) throws IOException {	  		  	  	 	 	
	  return pruneTerm(te.term());
  }

  @Override
  public void initTermPositions(TermPositions in, Term t) throws IOException {   
  }

  @Override
  public boolean pruneTermPositions(TermPositions termPositions, Term t)
          throws IOException {	  
		
	if (!fieldsToPrune.contains(t.field())) {
		return false;
	}
	  	
    if (validDocs.get(termPositions.doc())) {    	
    	return false;
    }           
    return true;
  }

  @Override
  public int pruneTermVectorTerms(int docNumber, String field, String[] terms,
          int[] freqs, TermFreqVector tfv)
          throws IOException {
	  		 	 	
	int removed=0;
    for (int i=0; i<terms.length; i++) {     
    	Term term=new Term(field,terms[i]);
    	if (pruneTerm(term)) {
    		terms[i]=null;
    		removed++;             
    	}      
    }
    return removed;       
  }

}
