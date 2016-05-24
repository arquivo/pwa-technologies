package org.apache.lucene.search.queries;

import java.io.IOException;

import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;


/**
 * A Query that matches a document with the closest timestamp. 
 * @author Miguel Costa
 * 
 * @note handle BUG wayback 0000153 
 */
public class PwaClosestQuery extends Query
{
    private Term closestTimestamp;
    

    /**
     * Constructor
     */
    public PwaClosestQuery(Term closestTimestamp) {
        if (closestTimestamp==null) {
            throw new IllegalArgumentException("The term must be non-null");
        }    
        this.closestTimestamp=closestTimestamp;
    }

    /** 
     * Rewrite this query 
     */
    public Query rewrite(IndexReader reader) throws IOException {      
        return this;
    }

    /**
     *  Returns the field name for this query 
     */
    public String getField() {
    	return closestTimestamp.field();
    }
  
    /** 
     * Returns the field value for this query 
     */
    public String getText() {
    	return closestTimestamp.text();
    }

    /** 
     * Prints a user-readable version of this query. 
     */
    public String toString(String garbage) {
        return closestTimestamp.toString();
    }

    /** 
     * Returns true iff <code>o</code> is equal to this. 
     */
    public boolean equals(Object o) {
        if (this==o) {
        	return true;
        }
        if (!(o instanceof PwaClosestQuery)) { 
        	return false;
        }

        final PwaClosestQuery other = (PwaClosestQuery) o;
        if (this.getBoost()!=other.getBoost()) { 
        	return false;       
        }
        if (!this.closestTimestamp.field().equals(other.closestTimestamp.field()) || 
        	 !this.closestTimestamp.text().equals(other.closestTimestamp.text())) { 
        	return false;
        }
        
        return true;
    }

    /*
     * Returns a hash code value for this object.
     */    
    public int hashCode() {      
    	return closestTimestamp.hashCode();
    }    
}
