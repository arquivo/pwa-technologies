package org.apache.lucene.search.caches;

import java.io.IOException;
import java.util.HashMap;

import org.apache.lucene.index.*;


/**
 * Caches some statistics from index, such as average field lengths
 * @author Miguel Costa
 */
public class PwaIndexStats {
	
	public final static String FIELDS[]={"content","url","host","anchor","title"};
	public final static String ANCHOR_DEF="anchor";
	private static HashMap<String,Double> avgFieldLenMap=null;
	private static HashMap<String,int[]> fieldLenMap=null;
	private static Object lockObj=new Object();
	private static HashMap<String,Integer> fieldsMap=null;	
	private static PwaIndexStats instance=null; // singleton class
	
	
	/**
	 * Constructor
	 * @param reader index reader
	 */
	private PwaIndexStats(IndexReader reader) throws IOException {
				
		avgFieldLenMap=new HashMap<String,Double>();
		fieldLenMap=new HashMap<String,int[]>();		
		fieldsMap=new HashMap<String,Integer>();
		long avgFieldLen;
		long countFieldLenHigherThan0; // this is mostly necessary for anchors, because most documents do not have links with anchors
		int lengths[];
		byte lengthsRaw[]=null;
		byte baux[]=new byte[4];
		
		System.out.println("Loading field lengths to RAM at "+this.getClass().getSimpleName()+" class.");
		for (int i=0;i<FIELDS.length;i++) {	// lengths only necessary for ranking			
			if (reader instanceof SegmentReader) { 
				lengthsRaw=((SegmentReader)reader).lengths(FIELDS[i]);
			}
			else {
				lengthsRaw=((MultiReader)reader).lengths(FIELDS[i]);
			}								
			if (lengthsRaw==null) {
				throw new IOException("Lengths array is null for field "+FIELDS[i]);
			}	
			
			avgFieldLen=0;
			countFieldLenHigherThan0=0;
			lengths=new int[lengthsRaw.length/4];
			// lengths must be converted from byte array to int				
			for (int j=0,k=0;j<lengthsRaw.length;j+=4,k++) {					
				System.arraycopy(lengthsRaw, j, baux, 0, 4);									
				lengths[k]=DocumentWriter.bytesToInt(baux);
				avgFieldLen+=lengths[k];
				if (lengths[k]>0) {
					countFieldLenHigherThan0++;
				}
			}
			//avgFieldLenMap.put(FIELDS[i], ((double)avgFieldLen/lengths.length));
			avgFieldLenMap.put(FIELDS[i], ((double)avgFieldLen/countFieldLenHigherThan0));
			fieldLenMap.put(FIELDS[i], lengths);				
			fieldsMap.put(FIELDS[i], i);
		}	
		System.out.println("Loading field lengths to RAM at "+this.getClass().getSimpleName()+" class ended.");
	}
	
	/**
	 * Get instance
	 * @param reader index reader
	 * @return instance
	 * @throws IOException
	 */
	public static PwaIndexStats getInstance(IndexReader reader) throws IOException {
		if (instance!=null) {
			return instance;
		}
		
		synchronized(lockObj) {
			if (instance!=null) {
				return instance;
			}
			instance=new PwaIndexStats(reader);
		}
		return instance;
	}
	
	/**
	 * Returns average length for @field
	 * @param field field
	 * @return average length for @field 
	 */
	public double getFieldAvgLength(String field) {
		return avgFieldLenMap.get(field);		
	}
	
	/**
	 * Returns lengths for @field
	 * @param field field
	 * @return lengths
	 */
	public int[] getFieldLengths(String field) {
		return fieldLenMap.get(field);		
	}
	
	/**
	 * Verifies if it is a term field
	 * @param field field
	 * @return true if it is a term field; false otherwise
	 */
	public boolean isField(String field) {		
		return (fieldsMap.get(field)!=null);
	}
	
	/**
	 * Returns the term field index
	 * @param field field
	 * @return field index if exist or null otherwise
	 */
	public Integer getFieldIndex(String field) {		
		return fieldsMap.get(field);
	}
}
