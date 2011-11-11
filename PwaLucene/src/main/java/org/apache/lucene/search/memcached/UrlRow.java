package org.apache.lucene.search.memcached;

import java.io.Serializable;


/**
 * Encapsulates a database row
 * @author Miguel Costa
 */
public class UrlRow implements Serializable {

	private int nVersions;
	private int min;
	private int max;
	
	
	/**
	 * Constructor
	 */
	public UrlRow(int nVersions, int min, int max) {		
		this.nVersions=nVersions;
		this.min=min;
		this.max=max;
	}
	
	/**
	 * Get number of versions
	 */
	public int getNVersions() {
		return nVersions;
	}
	
	/**
	 * Get minimum timestamp
	 * @return
	 */
	public int getMin() {
		return min;
	}

	/**
	 * Get maximum timestamp
	 * @return
	 */
	public int getMax() {
		return max;
	}

	public void setNVersions(int versions) {
		nVersions = versions;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public void setMax(int max) {
		this.max = max;
	}	
		
}
