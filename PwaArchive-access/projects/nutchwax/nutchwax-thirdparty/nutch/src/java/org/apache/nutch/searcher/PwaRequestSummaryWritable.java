package org.apache.nutch.searcher;

import java.io.DataInput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.util.ToolBase;

import org.apache.nutch.util.NutchConfiguration;


/**
 * Class that encapsualtes parameters for RPC communication to request summary for a set of hits
 * @author Miguel Costa
 */
public class PwaRequestSummaryWritable implements Writable {
	
	/** serial id */
	private static final long serialVersionUID = 1L;

	/** query */
	private Query query=null;
	
	/** hit details array */
	private HitDetails[] hitDetails=null;
	
	
	/**
	 * Constructor
	 */
	public PwaRequestSummaryWritable() {		
	}
	
	/**
	 * Set query
	 * @param query
	 */
	public void setQuery(Query query) {
		this.query=query;
	}

	/**
	 * Get query
	 * @return query
	 */
	public Query getQuery() {
		return query;
	}
	
	/**
	 * Set hit details
	 * @param hit details
	 */
	public void setHitDetails(HitDetails[] hitDetails) {
		this.hitDetails=hitDetails;;
	}

	/**
	 * Get hit details
	 * @return hit details
	 */
	public HitDetails[] getHitDetails() {
		return hitDetails;
	}
	
	/**
	 * Marshall data
	 * @param out
	 * @throws IOException
	 */	
    public void write(java.io.DataOutput out) throws IOException {
    	// write query    	  
    	query.write(out);
    	
    	// write hit details
    	out.writeInt(hitDetails.length);
    	for (int i=0;i<hitDetails.length;i++) {
    		hitDetails[i].write(out);
    	}    	    
    }    
    
    /**
     * Unmarshall data
     * @param in
     * @throws IOException
     */	
    public void readFields(DataInput in) throws IOException {
    	// read query
    	query=new Query(NutchConfiguration.create());
    	query.readFields(in); 
    	
    	// read hit details
    	int arrSize=in.readInt(); // hit details size
    	hitDetails=new HitDetails[arrSize];	
    	for (int i=0;i<hitDetails.length;i++) {
    		hitDetails[i]=HitDetails.read(in);
    	}       	 
    }    
    
    /**
     * Get instance
     * @param in
     * @return
     * @throws IOException
     */	
    public static PwaRequestSummaryWritable read(DataInput in) throws IOException {
    	PwaRequestSummaryWritable w = new PwaRequestSummaryWritable();
    	w.readFields(in);
    	return w;
    }    

  }