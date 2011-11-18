package org.apache.lucene.search.features;

import java.util.*;


/**
 * Encapsulates the scores returned by ranking functions 
 * @author Miguel Costa
 */
public class PwaScores {
	
	private Hashtable<Integer,Float> scores=null;
	
	
	/**
	 * Constructor
	 */
	public PwaScores() {
		scores=new Hashtable<Integer,Float>();
	}		

    /**
     * Add a score
     * @param index function index
     * @param score function score
     */
    public void addScore(int index, float score) {
    	scores.put(index,score);
    }
    
    /**
     * Indicates if has score for index @index
     * @param index function index
     * @return true if exist; false otherwise
     */
    public boolean hasScore(int index) {
    	return scores.get(index)!=null;
    }
    
    /**
     * Get function score
     * @param index score index
     * @return function score
     */
    public float getScore(int index) {
    	return scores.get(index);
    }
          
  }