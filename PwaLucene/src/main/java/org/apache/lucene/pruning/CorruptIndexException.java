package org.apache.lucene.pruning;

import java.io.IOException;


public class CorruptIndexException extends IOException {

  public CorruptIndexException(String message) {
    super(message);
  }
  
}