package org.apache.lucene.search.queries;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;


/**
 * A Query to sorts results 
 * @author Miguel Costa 
 */
public class PwaSortQuery extends Query {
	
    private String field; // field to sort results
    private boolean reverse; // ascending or descending sorting
    

    /**
     * Constructs a query to sort the results by a field.
     * @param field index field
     * @param reverse true for descending sorting, false for ascending
     */
    public PwaSortQuery(String field, boolean reverse) {
        if (field==null) {
            throw new IllegalArgumentException("The field must be non-null");
        }    
        this.field=field;
        this.reverse=reverse;
    }

    /** 
     * Rewrite this query 
     */
    public Query rewrite(IndexReader reader) throws IOException {      
        return this;
    }

    /** 
     * Returns the field name for this query 
     */
    public String getField() {
      return field;
    }
    
    /** 
     * Returns the reverse for this query 
     */
    public boolean getReverse() {
      return reverse;
    }
    
    /** 
     * Prints a user-readable version of this query. 
     */
    public String toString(String garbage) {
        return "sort:"+field+" reverse:"+reverse;
    }

    /** 
     * Returns true iff <code>o</code> is equal to this. 
     */
    public boolean equals(Object o) {
        if (this==o) {
        	return true;
        }
        if (!(o instanceof PwaSortQuery)) { 
        	return false;
        }

        final PwaSortQuery other = (PwaSortQuery) o;
        if (this.getBoost()!=other.getBoost()) { 
        	return false;       
        }
        if (!this.field.equals(other.field) || 
        	 !this.reverse==other.reverse) { 
        	return false;
        }
        
        return true;
    }

    /** 
     * Returns a hash code value for this object.
     */    
    public int hashCode() {      
    	return field.hashCode()+(reverse ? 1 : 0);
    }    
}
