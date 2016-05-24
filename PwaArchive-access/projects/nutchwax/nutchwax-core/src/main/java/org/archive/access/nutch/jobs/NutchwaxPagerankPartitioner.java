package org.archive.access.nutch.jobs;

import org.apache.hadoop.io.*;
import org.apache.hadoop.mapred.*;


/**
 * Partition tasks by mappers
 * @author Miguel Costa
 */
public class NutchwaxPagerankPartitioner implements Partitioner {

  public void configure(JobConf job) {
  }
  
  public void close() {}

  /**
   * Send all work for the same reducer
   */
  public int getPartition(WritableComparable key, Writable value, int numReduceTasks) {
	  return 0;
  }
  
}
