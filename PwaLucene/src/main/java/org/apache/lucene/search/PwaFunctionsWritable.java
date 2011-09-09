package org.apache.lucene.search;

import java.io.DataInput;
import java.io.IOException;
import java.util.*;

import org.apache.hadoop.io.Writable;


/**
 * Encapsulates the parameters of ranking functions for RPC communication 
 * @author Miguel Costa
 */
public class PwaFunctionsWritable implements Writable {
	
	private Hashtable<Integer,Float> functions=null;
	
	
	/**
	 * Constructor
	 */
	public PwaFunctionsWritable() {
		functions=new Hashtable<Integer,Float>();
	}
	
	/**
	 * Marshall data
	 * @param out output straeam
	 * @throws IOException
	 */
    public void write(java.io.DataOutput out) throws IOException {
    	Integer key=null;
    	
    	out.writeInt(functions.size());    	
    	for (Enumeration<Integer> e = functions.keys(); e.hasMoreElements();) {
    		key=e.nextElement();
            out.writeInt(key);
            out.writeFloat(functions.get(key));
        }
    }
    
    /**
     * Unmarshall data
     * @param in input stream
     * @throws IOException
     */
    public void readFields(DataInput in) throws IOException {    	
    	int size=in.readInt();
    	for (int i=0;i<size;i++) {
    		functions.put(in.readInt(), in.readFloat());
    	}    	   
    }
    
    /**
     * Get instance
     * @param in input stream
     * @return
     * @throws IOException
     */
    public static PwaFunctionsWritable read(DataInput in) throws IOException {
    	PwaFunctionsWritable w = new PwaFunctionsWritable();
    	w.readFields(in);
    	return w;
    }


    /**
     * Add a function
     * @param index function index
     * @param boost function boost
     */
    public void addFunction(int index, float boost) {
    	functions.put(index,boost);
    }
    
    /**
     * Indicates if has function with index @index
     * @param index function index
     * @return true if exist; false otherwise
     */
    public boolean hasFunction(int index) {
    	return functions.get(index)!=null;
    }
    
    /**
     * Get function boost
     * @param index function index
     * @return functicon boost
     */
    public float getBoost(int index) {
    	return functions.get(index);
    }
    
    
    /**
     * Parse string list of ranking functions  
     * @param s list of ranking functions in format "functionId weight functionId weight ..."
     * @return new PwaFunctionsWritable object with the ranking functions defined in the list
     */
    public static PwaFunctionsWritable parse(String s) {
    	PwaFunctionsWritable newFunctions=new PwaFunctionsWritable();
        String parts[]=s.split(" ");
        for (int i=0;i<parts.length;i+=2) {
        	newFunctions.addFunction(Integer.parseInt(parts[i]), Float.parseFloat(parts[i+1]));    	  
        }
        return newFunctions;
    }
  }