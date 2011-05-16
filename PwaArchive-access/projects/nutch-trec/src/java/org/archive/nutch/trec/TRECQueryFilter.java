package org.archive.nutch.trec;


import org.apache.nutch.searcher.basic.BasicQueryFilter;

import java.util.regex.Pattern;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.TermQuery;

import org.apache.nutch.analysis.NutchDocumentAnalyzer;
import org.apache.nutch.analysis.CommonGrams;

import org.apache.nutch.searcher.QueryFilter;
import org.apache.nutch.searcher.Query;
import org.apache.nutch.searcher.Query.*;
import org.apache.hadoop.conf.Configuration;


public class TRECQueryFilter extends BasicQueryFilter {

	public TRECQueryFilter() {
		super();
	}
	
//	private void addTerms(Query input, BooleanQuery output) {
//	    Clause[] clauses = input.getClauses();
//	    for (int i = 0; i < clauses.length; i++) {
//	    	Clause c = clauses[i];
//
//		    if (!c.getField().equals(Clause.DEFAULT_FIELD))
//		        continue;                                 // skip non-default fields
//
//		    BooleanQuery out = new BooleanQuery();
//		    for (int f = 0; f < FIELDS.length; f++) {
//
//		      Clause o = c;
//		      if (c.isPhrase()) {                         // optimize phrase clauses
//		        String[] opt = new CommonGrams(getConf()).optimizePhrase(c.getPhrase(), FIELDS[f]);
//		        if (opt.length==1) {
//		          o = new Clause(new Term(opt[0]), c.isRequired(), c.isProhibited(), getConf());
//		        } else {
//		          o = new Clause(new Phrase(opt), c.isRequired(), c.isProhibited(), getConf());
//		        }
//		      }
//
//		      out.add(o.isPhrase()
//		              ? exactPhrase(o.getPhrase(), FIELDS[f], FIELD_BOOSTS[f])
//		              : termQuery(FIELDS[f], o.getTerm(), FIELD_BOOSTS[f]),
//		              BooleanClause.Occur.SHOULD);
//		    }
//		    output.add(out, (c.isProhibited()
//		            ? BooleanClause.Occur.MUST_NOT
//		            /* : (c.isRequired()
//		                ? BooleanClause.Occur.MUST    ALWAYS SHOULD */ 
//		                : BooleanClause.Occur.SHOULD
//		              ))/*)*/;
//		}
//	}
//	
//	private void addSloppyPhrases(Query input, BooleanQuery output) {
//		// nothing to do
//	}
	
	private void addIncludeExtensions(Query input, BooleanQuery output) {
		// nothing to do
	}
}
