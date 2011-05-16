package org.archive.access.nutch.jobs;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.*;

/**
 * Composed key by key,value
 * @author Miguel Costa
 */
public class ArquivoWebKeyValueWritable implements WritableComparable {
       
       private int key;
       private int value;
       
       public ArquivoWebKeyValueWritable() {}
       
       public ArquivoWebKeyValueWritable(int key, int value) {
    	   this.key=key;
    	   this.value=value; 
       }
       
       public void write(DataOutput out) throws IOException {
    	   out.writeInt(key);
    	   out.writeInt(value);
       }
       
       public void readFields(DataInput in) throws IOException {
    	   key = in.readInt();
    	   value = in.readInt();
       }
       
       public static ArquivoWebKeyValueWritable read(DataInput in) throws IOException {
    	   ArquivoWebKeyValueWritable w = new ArquivoWebKeyValueWritable();
    	   w.readFields(in);
    	   return w;
       }
       
       public int compareTo(Object o) {
    	   ArquivoWebKeyValueWritable w=(ArquivoWebKeyValueWritable)o;
    	   if (this.key<w.key) {
    		   return -1;
    	   }
    	   else if (this.key==w.key) { // if equal compare value    		   
    		   if (this.value<w.value) {
    			   return -1;
    		   }
    		   else if (this.value==w.value) {
    			   return 0;
    		   }
    		   else { // (this.value>w.value)
    			   return 1;
    		   }
       	   }
    	   else { // (this.key>w.key) 
    		   return 1;
    	   }           	          
       }

	public int getKey() {
		return key;
	}

	public void setKey(int key) {
		this.key = key;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}