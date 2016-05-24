package org.archive.access.nutch.searcher;

import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.searcher.RawFieldQueryFilter;

import org.apache.lucene.search.*;
import org.apache.lucene.index.Term;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.Query.Clause;
import org.apache.nutch.searcher.QueryException;

/**
 * Look for explicit match of passed URL.
 * @author Miguel Costa
 */
public class WaxExacturlExpandQueryFilter extends RawFieldQueryFilter
{
  private final static String MATCH_KEY="exacturlexpand";
  private final static String FINAL_KEY="exacturl";
  private Configuration conf;

  public WaxExacturlExpandQueryFilter()
  {
    super(MATCH_KEY, false, 0.1f);
  } 

  public Configuration getConf()
  {
    return this.conf;
  }

  public void setConf(Configuration conf)
  {
    this.conf = conf;
  }
  
  public BooleanQuery filter(Query input, BooleanQuery output)
   throws QueryException {
   
   BooleanQuery outinner=new BooleanQuery();
   BooleanClause.Occur occurinner=null;
   int nClauses=0;
   // examine each clause in the Nutch query
   Clause[] clauses = input.getClauses();
   for (int i = 0; i < clauses.length; i++) {
     Clause c = clauses[i];

     // skip non-matching clauses
     if (!c.getField().equals(field))
       continue;

     // get the field value from the clause
     // raw fields are guaranteed to be Terms, not Phrases
     String value = c.getTerm().toString();
     if (lowerCase)
       value = value.toLowerCase();

     // add a Lucene TermQuery for this clause
     TermQuery clause = new TermQuery(new Term(FINAL_KEY, value));    
     // add it as specified in query        
     outinner.add(clause, BooleanClause.Occur.SHOULD);    
     if (nClauses==0) {
    	 occurinner=c.isProhibited()
	            ? BooleanClause.Occur.MUST_NOT
	    	            : (c.isRequired()
	    	                ? BooleanClause.Occur.MUST
	    	                : BooleanClause.Occur.SHOULD
	    	               );
     }
     nClauses++;
   }
   if (nClauses>0) {
	   // set boost
	   outinner.setBoost(boost);  
	   output.add(outinner, occurinner);	  
   }
  
   // return the modified Lucene query
   return output;
 }
  
}
