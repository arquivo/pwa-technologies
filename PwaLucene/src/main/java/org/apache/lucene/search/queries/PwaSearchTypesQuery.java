package org.apache.lucene.search.queries;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;


/**
 * A Query that matches documents with the searched types
 * @author Miguel Costa 
 */
public class PwaSearchTypesQuery extends Query {
	
    private String[] types; // file format types
    

    /**
     * Constructs a query to select the documents with the searched types.
     * @param types 
     */
    public PwaSearchTypesQuery(String[] types) {
        if (types==null) {
            throw new IllegalArgumentException("The types must be non-null");
        }    
        this.types=types;
    }

    /** 
     * Rewrite this query 
     */
    public Query rewrite(IndexReader reader) throws IOException {      
        return this;
    }

    /**
     * Returns the file format types of this query  
     */
    public String[] getTypes() {
    	return types;
    }
    

    /** 
     * Prints a user-readable version of this query. 
     */
    public String toString(String garbage) {
    	StringBuffer sbuf=new StringBuffer();
    	sbuf.append("type:");
    	for (int i=0;i<types.length;i++) {
    		if (i!=0) {
    			sbuf.append('|');
    		}    
    		sbuf.append(types[i]);    			
    	}
        return sbuf.toString();
    }

    /** 
     * Returns true iff <code>o</code> is equal to this. 
     */
    public boolean equals(Object o) {
        if (this==o) {
        	return true;
        }
        if (!(o instanceof PwaSearchTypesQuery)) { 
        	return false;
        }

        final PwaSearchTypesQuery other = (PwaSearchTypesQuery) o;
        if (this.getBoost()!=other.getBoost()) { 
        	return false;       
        }
        if (!this.toString("").equals(other.toString(""))) { 
        	return false;
        }
        
        return true;
    }

    /** 
     * Returns a hash code value for this object.
     */    
    public int hashCode() {      
    	return types.hashCode();
    }    
}
