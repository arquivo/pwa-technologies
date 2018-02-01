package org.apache.nutch.searcher;

import java.io.DataInput;
import java.io.IOException;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;


/**
 * Class that encapsualtes parameters for RPC communication to request details for a set of hits
 * @author Miguel Costa
 */
public class PwaRequestDetailsWritable implements Writable {
	
	/** serial id */
	private static final long serialVersionUID = 1L;

	/** names of the fields to request */
	private String[] fields=null;
	
	/** hits array */
	private Hit[] hits=null;
	
	
	/**
	 * Constructor
	 */
	public PwaRequestDetailsWritable() {		
	}
	
	/**
	 * Set fields
	 * @param fields
	 */
	public void setFields(String[] fields) {
		this.fields=fields;
	}

	/**
	 * Get fields
	 * @return
	 */
	public String[] getFields() {
		return fields;
	}
	
	/**
	 * Set hits
	 * @param hits
	 */
	public void setHits(Hit[] hits) {
		this.hits=hits;;
	}

	/**
	 * Get hits
	 * @return hits
	 */
	public Hit[] getHits() {
		return hits;
	}
	
	/**
	 * Marshall data
	 * @param out
	 * @throws IOException
	 */	
    public void write(java.io.DataOutput out) throws IOException {
    	// write field names
    	if (fields==null) {
    		out.writeInt(0);
    	}
    	else {
    		out.writeInt(fields.length);
    		for (int i=0;i<fields.length;i++) {    		    	
    			Text.writeString(out,fields[i]);
    		}
    	}
    	
    	// write hits
    	out.writeInt(hits.length);
    	if (hits.length>0) {
    		Text.writeString(out, hits[0].getSortValue().getClass().getName());
    	}
    	
    	for (int i=0;i<hits.length;i++) {
    		out.writeInt(hits[i].getIndexDocNo());            // write indexDocNo
    		hits[i].getSortValue().write(out);                // write sortValue
    		Text.writeString(out, hits[i].getDedupValue());   // write dedupValue    		
    	}    	
    }    
    
    /**
     * Unmarshall data
     * @param in
     * @throws IOException
     */	
    public void readFields(DataInput in) throws IOException {
    	// read field names    	
    	int arrSize=in.readInt(); // fields size
    	if (arrSize==0) {
    		fields=null;
    	}
    	else {
    		fields=new String[arrSize];      	    	
    		for (int i=0;i<arrSize;i++) {    		
    			fields[i]=Text.readString(in);
    		}
    	}
    	
    	// read hits
    	arrSize=in.readInt(); // hits size
    	hits=new Hit[arrSize];
    	    	
    	Class sortClass = null;
    	if (hits.length > 0) {                         // read sort value class
    		try {
    			sortClass = Class.forName(Text.readString(in));
    	    } 
    		catch (ClassNotFoundException e) {
    			throw new IOException(e.toString());
    	    }
    	}
    	
    	for (int i=0;i<arrSize;i++) {   		
    		int indexDocNo = in.readInt();              // read indexDocNo
            WritableComparable sortValue = null;
    		try {
    			sortValue = (WritableComparable)sortClass.newInstance();
    		} 
    		catch (Exception e) {
    		    throw new IOException(e.toString());
    		}
    		sortValue.readFields(in);                   // read sortValue
    		String dedupValue = Text.readString(in);    // read dedupValue
    		
    		hits[i] = new Hit(indexDocNo, sortValue, dedupValue);
    	}
    }    
    
    /**
     * Get instance
     * @param in
     * @return
     * @throws IOException
     */	
    public static PwaRequestDetailsWritable read(DataInput in) throws IOException {
    	PwaRequestDetailsWritable w = new PwaRequestDetailsWritable();
    	w.readFields(in);
    	return w;
    }    

  }