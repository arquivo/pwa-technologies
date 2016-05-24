package org.apache.lucene.search;

import org.apache.lucene.util.PriorityQueue;


/**	 
 * PriorityQueue for phrase processing
 * @author Miguel Costa
 */
class PwaPhraseQueue extends PriorityQueue {

	/**
	 * Constructor
	 * @param size queue size
	 */
	public PwaPhraseQueue(int size) {
		initialize(size);
	}

	/**
	 * Comparator
	 */
	protected final boolean lessThan(Object o1, Object o2) {		
		return ((PwaPositions)o1).get()<((PwaPositions)o2).get();
	}
}