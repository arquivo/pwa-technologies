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

import org.apache.lucene.search.DefaultSimilarity;

/** Similarity implementatation used by Nutch indexing and search. */
public class NutchSimilarity extends DefaultSimilarity  {
  private static final int MIN_CONTENT_LENGTH = 1000;

  /** Normalize field by length.  Called at index time. */
  public float lengthNorm(String fieldName, int numTokens) { // TODO MC: the index have to be re-indexed each time I change this method
    if ("url".equals(fieldName)) {                // URL: prefer short
      return 1.0f / numTokens;                    // use linear normalization
      
    } else if ("anchor".equals(fieldName)) {      // Anchor: prefer more
      return (float)(1.0/Math.log(Math.E+numTokens)); // use log

    } else if ("content".equals(fieldName)) {     // Content: penalize short
      return super.lengthNorm(fieldName,          // treat short as longer
                              Math.max(numTokens, MIN_CONTENT_LENGTH));

    } else {                                      // use default
      return super.lengthNorm(fieldName, numTokens);  				// super return (float)(1.0 / Math.sqrt(numTerms)); - from DefaultSimilarity
    }
    
    // TODO MC
    // host and title should have 1.0f / numTokens;   
    // anchor should have 1.0/Math.log(Math.E+max(numTokens,10)) - a document with less than 10 anchors should have a larger weight
    // url and content seems ok
  }

  public float coord(int overlap, int maxOverlap) {
    return 1.0f;
  }
  
  // TODO MC - ignore normalization between queries
  public float queryNorm(float sumOfSquaredWeights) {
	  return 1.0f;
  }

}
