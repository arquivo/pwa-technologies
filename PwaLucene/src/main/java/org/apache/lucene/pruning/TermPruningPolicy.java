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
import java.util.Collections;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.index.*;


/**
 * Superclass of term pruning policies.
 */
public abstract class TermPruningPolicy extends PruningPolicy {
  protected Map<String, Integer> fieldFlags;
  protected IndexReader in;
  
  /**
   * Construct a policy.
   * @param in input reader
   * @param fieldFlags a map, where keys are field names and values
   * are bitwise-OR flags of operations to be performed (see
   * {@link PruningPolicy} for more details).
   */
  @SuppressWarnings("unchecked")
  protected TermPruningPolicy(IndexReader in, Map<String, Integer> fieldFlags) {
    this.in = in;
    if (fieldFlags != null) {
      this.fieldFlags = fieldFlags;
    } else {
      this.fieldFlags = Collections.EMPTY_MAP;
    }
  }
  
  /**
   * Term vector pruning.
   * @param docNumber document number
   * @param field field name
   * @return true if the complete term vector for this field should be
   * removed (as specified by {@link PruningPolicy#DEL_VECTOR} flag).
   * @throws IOException
   */
  public boolean pruneWholeTermVector(int docNumber, String field)
      throws IOException {
    if (fieldFlags.containsKey(field) && 
            (fieldFlags.get(field) & DEL_VECTOR) != 0) {
      return true;
    } else {
      return false;
    }
  }
  
  /**
   * Pruning of all postings for a field
   * @param field field name
   * @return true if all postings for all terms in this field should be
   * removed (as specified by {@link PruningPolicy#DEL_POSTINGS}).
   * @throws IOException
   */
  public boolean pruneAllPostings(String field) throws IOException {
    if (fieldFlags.containsKey(field) && 
            (fieldFlags.get(field) & DEL_POSTINGS) != 0) {
      return true;
    } else {
      return false;
    }
  }
  
  /**
   * Called when moving {@link TermPositions} to a new {@link Term}.
   * @param in input term positions
   * @param t current term
   * @throws IOException
   */
  public abstract void initTermPositions(TermPositions in, Term t)
    throws IOException;

  /**
   * Called when checking for the presence of payload for the current
   * term at a current position
   * @param in positioned term positions
   * @param curTerm current term associated with these positions
   * @return true if the payload should be removed, false otherwise.
   */
  public boolean prunePayload(TermPositions in, Term curTerm) {
    if (fieldFlags.containsKey(curTerm.field()) &&
            (fieldFlags.get(curTerm.field()) & DEL_PAYLOADS) != 0) {
      return true;
    }
    return false;
  }

  /**
   * Pruning of individual terms in term vectors.
   * @param docNumber document number
   * @param field field name
   * @param terms array of terms
   * @param freqs array of term frequencies
   * @param v the original term frequency vector
   * @return 0 if no terms are to be removed, positive number to indicate
   * how many terms need to be removed. The same number of entries in the terms
   * array must be set to null to indicate which terms to remove.
   * @throws IOException
   */
  public abstract int pruneTermVectorTerms(int docNumber, String field,
          String[] terms, int[] freqs, TermFreqVector v) throws IOException;

  /**
   * Pruning of all postings for a term.
   * @param te positioned term enum.
   * @return true if all postings for this term should be removed, false
   * otherwise.
   * @throws IOException
   */
  public abstract boolean pruneTermEnum(TermEnum te) throws IOException;

  /**
   * Prune individual postings per term.
   * @param termPositions positioned term positions. Implementations MUST NOT
   * advance this by calling {@link TermPositions} methods that advance either
   * the position pointer (next, skipTo) or term pointer (seek).
   * @param t current term
   * @return true if the current posting should be removed, false otherwise.
   * @throws IOException
   */
  public abstract boolean pruneTermPositions(TermPositions termPositions, Term t)
      throws IOException;
}
