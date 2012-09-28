package org.archive.access.nutch.jobs;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.*;

/**
 * Composed key by key,value
 * @author Miguel Costa
 */
public class ArquivoWebComposedKeyWritable implements WritableComparable {
	
	private String key; // url radical
	private String value; // timestamp
	private long lvalue; // timestamp converted to long

	public ArquivoWebComposedKeyWritable() {}

	public ArquivoWebComposedKeyWritable(String key, String value) {
		this.key=key;
		this.value=value; 
		this.lvalue=Long.parseLong(value);
	}

	public void write(DataOutput out) throws IOException {
		out.writeUTF(key);
		out.writeUTF(value);
	}

	public void readFields(DataInput in) throws IOException {		
		key = in.readUTF();
		value = in.readUTF();
		lvalue=Long.parseLong(value);
	}

	public static ArquivoWebComposedKeyWritable read(DataInput in) throws IOException {
		ArquivoWebComposedKeyWritable w = new ArquivoWebComposedKeyWritable();
		w.readFields(in);
		return w;
	}

	public int compareTo(Object o) {
		ArquivoWebComposedKeyWritable w=(ArquivoWebComposedKeyWritable)o;
		// compare url radical
		int comp=this.key.compareTo(w.getKey());
		if (comp!=0) {
			return comp;
		}		
		// compare timestamp
		if (this.lvalue<w.getLongValue()) {
			return -1;
		}
		if (this.lvalue>w.getLongValue()) {
			return 1;
		}
		return 0;    	       	              	        
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getValue() {
		return value;
	}
	
	public long getLongValue() {
		return lvalue;
	}

	public void setValue(String value) {
		this.value = value;
		this.lvalue = Long.parseLong(value);
	}
	
	public String toString() {
		return key;
	}
}
