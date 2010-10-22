package org.apache.lucene.search;

import java.util.Vector;


/**
 * Stores positions of a posting of a triple, query term, document, field
 * @author Miguel Costa
 *
 */
public class PwaPositions {

	private Vector<Integer> pos; // positions vector
	private int next;
	
	/**
	 * Constructor
	 */
	public PwaPositions() {
		next=-1;
		pos=new Vector<Integer>();
	}
	
	/**
	 * Add position
	 * @param i position
	 */
	public void add(int i) {
		pos.add(i);
	}
	
	/**
	 * Get position of index
	 * @param i index for vector of positions
	 * @return position
	 */
	public int get(int i) {
		return pos.get(i);
	}
	
	/**
	 * Move to next document
	 * @return true if has more documents; false otherwise
	 */
	public boolean next() {
		next++;
		return next<pos.size();
	}
	
	/**
	 * Get position
	 * @return position
	 */
	public int get() {
		return pos.get(next);
	}
}
