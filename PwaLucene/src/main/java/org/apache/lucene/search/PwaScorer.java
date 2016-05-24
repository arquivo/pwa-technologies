package org.apache.lucene.search;

import java.io.IOException;
import java.util.*;

import org.apache.lucene.index.*;
import org.apache.lucene.search.caches.PwaDateCache;
import org.apache.lucene.search.caches.PwaIndexStats;
import org.apache.lucene.search.caches.PwaStopwords;
import org.apache.lucene.search.filters.PwaFilter;
import org.apache.lucene.search.filters.PwaDateClosestFilter;
import org.apache.lucene.search.filters.PwaDateRangeFilter;
import org.apache.lucene.search.filters.PwaFilterChain;
import org.apache.lucene.search.filters.PwaBlacklistFilter;
import org.apache.lucene.search.queries.PwaClosestQuery;
import org.apache.lucene.search.queries.PwaSortQuery;
import org.apache.lucene.search.features.PwaLinearRankingModel;
import org.apache.lucene.search.features.PwaScores;


/**
 * Matches and scores documents according to the query
 * @author Miguel Costa
 */
public class PwaScorer extends Scorer {
	
	private final static int MIN_TF_ANCHORS=4; // minimum number of terms that occur only in the anchor field when matching a document	
	private static enum ScoreType {NORMAL, FLAT, ONLY_ONE, DATE_SORTED, DATE_SORTED_REVERSE};
	private BooleanQuery query; 
	private Searcher searcher;   
	private IndexReader reader;
	private PwaFilterChain chainFilter;
	private Vector<PwaFilter> filters; // filters to apply
	private PwaJoiner joiner;
	private PwaFunctionsWritable functions;
	private PwaIndexStats indexstats;
	private boolean empty;
	private ScoreType scoreType;
	private long queryTimestamp;
	
	/**
	 * Constructor
	 * @param query query 
	 * @param searchersearcher
	 * @param reader reader
	 * @param functions ranking functions
	 * @throws IOException
	 */
	public PwaScorer(BooleanQuery query, Searcher searcher, IndexReader reader, PwaFunctionsWritable functions) throws IOException {
		super(null);
	    this.query=query; 
	    this.searcher=searcher;
	    this.reader=reader;
	    this.functions=functions;
	    this.indexstats=PwaIndexStats.getInstance(reader);
	    	   
	    this.chainFilter=null;
	    this.filters=new Vector<PwaFilter>(); 
	    this.joiner=null;
	    this.empty=false;
	    this.queryTimestamp=System.currentTimeMillis();
	    init();
	}	
	
	/**
	 * Initialize scorer
	 * @throws IOException
	 */
	private void init() throws IOException {
		
		LinkedHashMap htableMergers=new LinkedHashMap();  // to separate query terms by term text (e.g. lisbon:url,lisbon:content from portugal:url,portugal:content)
		LinkedHashMap htableMergersExclude=new LinkedHashMap();  // to separate query terms by term text to exclude (e.g. -lisbon:url,-lisbon:content from -portugal:url,-portugal:content)		
		LinkedHashMap htableMergersExtra=new LinkedHashMap();  // to separate query terms by extra term field (e.g. type:pdf,type:ps from site:www.fccn.pt)
		LinkedHashMap htableMergersExtraExclude=new LinkedHashMap();  // to separate query terms by extra term fields to exclude
		LinkedHashMap htablePositions=new LinkedHashMap();  // to separate query terms by term field (e.g. lisbon:url,portugal:url from lisbon:content,portugal:content)			
		PwaSearchable termAux=null;
		boolean exclude=false;
		String termText=null;
		String termField=null;		
		boolean isOnlyPhrasesForRank=true; // phrase search has always distance 0, so the rank value is the same for all
		
		// do not rank results for now
		scoreType=ScoreType.FLAT; 
		
		// extract terms and separate terms per text and field
		List lclauses=query.clauses();		
		for (int i=0; i<lclauses.size(); i++) {
						
			BooleanClause clause=(BooleanClause)lclauses.get(i);			
			List lclausesInside=processClause(clause);			
						
			// separate terms per text and field
	    	for (int j=0; j<lclausesInside.size(); j++) {	   		
	    		BooleanClause clauseInside = (BooleanClause)lclausesInside.get(j);
	    			    		    			    		
	    		Vector terms=new Vector();
	    		extractTerms(clauseInside.getQuery(),terms);
	    		Term termsArray[]=new Term[terms.size()];
	    		terms.toArray(termsArray);
				termField=termsArray[0].field();
	    		
	    		if (clause.getOccur()==BooleanClause.Occur.MUST_NOT) {		    	
	    			exclude=true;
	    		}
	    		else {
	    			exclude=false;
	    		}
	    		
	    		termAux=null;
    			// create PwaTerm or PwaPhrase
    			if (clauseInside.getQuery() instanceof TermQuery) {
    				termText=termsArray[0].text();				
	    			
    				if (!PwaStopwords.getInstance(reader).contains(termField,termText)) { // if term is not a stopword then process it, otherwise ignore it	    			
    					if (indexstats.isField(termField) && !exclude) { // it must be a term field and not a "NOT" term, which do not require positional data 	    						 	    							    	    						
    						if (termField.equals(PwaIndexStats.ANCHOR_DEF)) { // BUG nutchwax 0000202 
    							termAux=new PwaTermLimited(termsArray[0],reader,MIN_TF_ANCHORS);
    						}
    						else {
    							termAux=new PwaTerm(termsArray[0],reader);	
    						}	    							    							    						
    						isOnlyPhrasesForRank=false;	    						
    					}
    					else {
    						termAux=new PwaExtraTerm(termsArray[0],reader);  	    					
    					}	    						    			
    				}
    			}
    			else  if (clauseInside.getQuery() instanceof PhraseQuery && indexstats.isField(termField)) { // for phrase it must be a term		    		    			
    				Vector<PwaTerm> vecAux=new Vector<PwaTerm>();	// terms searched in phrase    						    					    			
    				Vector<Integer> vecOffsetTerms=new Vector<Integer>(); // offset of query terms (for phrase processing)
    				int stopwordsAtBegin=0;
    				
    				for (int k=0;k<termsArray.length; k++) {	  
    					// remove first and last terms if they are stopwords
    					if (!PwaStopwords.getInstance(reader).contains(termField,termsArray[k].text())) { // if term is not a stopword process it, else ignore it 	    					
    						if (termField.equals(PwaIndexStats.ANCHOR_DEF)) { // BUG nutchwax 0000202 
    							vecAux.add(new PwaTermLimited(termsArray[k],reader,MIN_TF_ANCHORS));
    						}
    						else {
    							vecAux.add(new PwaTerm(termsArray[k],reader));	
    						}	    						
    						vecOffsetTerms.add(k-stopwordsAtBegin);
    					}
    					else {
    						if (vecOffsetTerms.size()==0) { // to exclude stopwords at beginning
    							stopwordsAtBegin++;
    						}
    					}
    				}
    				if (!exclude) {
    					termAux=new PwaPhrase(vecAux,vecOffsetTerms); 
    				}
    				else {
    					termAux=new PwaExtraPhrase(vecAux,vecOffsetTerms); // does not need to collectFeatures
    				}	    					    					    					    			    				
    				StringBuffer sbuf=new StringBuffer();
    				for (int k=0; k<termsArray.length; k++) {
    					if (k>0) { 	
    						sbuf.append(" ");
    					}	    			
    					sbuf.append(termsArray[k].text());    					  							    		
    				}
    				termText=sbuf.toString();
    			}
    			else {    				
    				empty=true;
    				return;
    			}
		    	    			    			   
	    		if (termAux!=null) { // if it is not a stopword partition query per text and field
	    			if (indexstats.isField(termField)) {	
	    				if (!exclude) {		    					
	    					addTerms2Map(htableMergers, termAux, termText); // by term text	    						    						    			
	    					addTerms2Map(htablePositions, termAux, termField); // by term field
	    				}		    
	    				else {	    		
	    					addTerms2Map(htableMergersExclude, termAux, termText); // by term text	    						    			
	    				}
	    			}			    	
	    			else { // other fields like DOCNUM, type, site								    	
	    				if (!exclude) {
	    					addTerms2Map(htableMergersExtra, termAux, termField); // by term field	    					
	    				}
	    				else {
	    					addTerms2Map(htableMergersExtraExclude, termAux, termField); // by term field	    					
	    				}
	    			}
	    		}
	    	}
		}						 		

													
		// check empty query - at least a term must must be valid 
		if (htableMergers.size()==0 && htableMergersExtra.size()==0) {
			empty=true;
			return;
		}
		
		// preparing for matching documents
		prepareMatching(isOnlyPhrasesForRank, htableMergers, htableMergersExtra, htableMergersExclude, htableMergersExtraExclude, htablePositions);			
	}
	
	
	/**
	 * Processes query clause 
	 * @param clause query clause
	 * @return sub-clauses extracted
	 * @throws IOException
	 */
	private List processClause(BooleanClause clause) throws IOException {		
		List lclausesInside=new Vector(); 
		if (clause.getQuery() instanceof TermQuery) { // add term												
			lclausesInside.add(clause);				
		}
		else if (clause.getQuery() instanceof PhraseQuery) { // add phrase
			lclausesInside.add(clause);								
		}
		else if (clause.getQuery() instanceof PwaClosestQuery) { // add filter - date closest
			PwaClosestQuery query = (PwaClosestQuery)clause.getQuery();
			filters.add(new PwaDateClosestFilter(reader, query.getText()));
			scoreType=ScoreType.ONLY_ONE; // only one result, so it does not need to rank 				
		}
		else if (clause.getQuery() instanceof RangeQuery) { // add filter - date range
	        RangeQuery query = (RangeQuery)clause.getQuery();
	        filters.add(new PwaDateRangeFilter(reader, query.getLowerTerm().text(), query.getUpperTerm().text()));
		}			
		else if (clause.getQuery() instanceof PwaSortQuery) { // add filter - sort results by date 
			PwaSortQuery query = (PwaSortQuery)clause.getQuery();
			if (query.getField().equals("date")) { // it does not need to rank by score, because results are sorted by date
				if (query.getReverse()) {
					scoreType=ScoreType.DATE_SORTED_REVERSE;
				}
				else {
					scoreType=ScoreType.DATE_SORTED; 
				}
			}
		}						
		else { // BooleanQuery			
			lclausesInside=((BooleanQuery)clause.getQuery()).clauses();
		}		
		
		return lclausesInside;
	}
	
	
	/**
	 * Prepares joiner, mergers and filters for matching documents
	 * @param isOnlyPhrasesForRank indicates if query has only phrases or not 
	 * @param htableMergers
	 * @param htableMergersExtradouble score=0;
	 * @param htableMergersExclude
	 * @param htableMergersExtraExclude
	 * @param htablePositions
	 * @throws IOException 
	 */
	private void prepareMatching(boolean isOnlyPhrasesForRank, LinkedHashMap htableMergers, LinkedHashMap htableMergersExtra, LinkedHashMap htableMergersExclude, LinkedHashMap htableMergersExtraExclude, LinkedHashMap htablePositions) throws IOException {
	
		// set mergers for terms
		Vector<PwaMerger> mergers=new Vector<PwaMerger>();
		addTerms2Merger(mergers, htableMergers, false);	
		
		// set mergers for extra terms
		Vector<PwaMerger> mergersExtra=new Vector<PwaMerger>();
		addTerms2Merger(mergersExtra, htableMergersExtra, false);
	
		// set mergers for terms to exclude	
		Vector<PwaMerger> mergersExclude=new Vector<PwaMerger>();
		addTerms2Merger(mergersExclude, htableMergersExclude, true);		
	
		// set mergers for extra terms to exclude
		Vector<PwaMerger> mergersExtraExclude=new Vector<PwaMerger>();
		addTerms2Merger(mergersExtraExclude, htableMergersExtraExclude, true);				
	
		// join mergers and the exclude mergers after all others (this order must be followed)		
		Vector<PwaSearchable> joinAll=new Vector<PwaSearchable>();
		if (mergers.size()>0) {
			joinAll.addAll(mergers); // add mergers
			if (scoreType==ScoreType.FLAT) {
				scoreType=ScoreType.NORMAL;
			}
		}
		if (mergersExtra.size()>0) {
			joinAll.addAll(mergersExtra); // add mergers for extra terms
		}
		if (mergersExclude.size()>0) {
			joinAll.addAll(mergersExclude); // add mergers with terms to exclude
		}
		if (mergersExtraExclude.size()>0) {
			joinAll.addAll(mergersExtraExclude); // add mergers with extra terms for exclude
		}
		joiner=new PwaJoiner(joinAll);

		// set positions manager
		Vector<PwaPositionsManager> posmanagers=new Vector<PwaPositionsManager>();
		Vector<PwaSearchable> vecTermsAux=null;
		if (mergers.size()>0 && !isOnlyPhrasesForRank) {	    	
			for (int i=0;i<PwaIndexStats.FIELDS.length;i++) {  			
				vecTermsAux=(Vector)htablePositions.get(PwaIndexStats.FIELDS[i]);	    			    		
				Vector<PwaTerm> vecAllTerms = new Vector<PwaTerm>();
				for (int k=0;k<vecTermsAux.size();k++) { 
					if (vecTermsAux.get(k) instanceof PwaTerm) { 
						vecAllTerms.add((PwaTerm)vecTermsAux.get(k));
					}
					else { // PwaPhrase
						vecAllTerms.addAll(((PwaPhrase)vecTermsAux.get(k)).getTerms());
					}
				}
		
				posmanagers.add(new PwaPositionsManager(vecAllTerms));	    		
			}			 		    	
		}
		joiner.setPositionsManager(posmanagers);
    
		// set filter chain
		filters.add(new PwaBlacklistFilter(reader)); // add the blacklist filter 
		chainFilter=new PwaFilterChain(filters,joiner);   // set chain filter
	}
	
	/**
	 * Add terms to maps to separate terms
	 * @param htableMergers
	 * @param term term query term
	 * @param termText text or field of term
	 */
	private void addTerms2Map(LinkedHashMap htableMergers, PwaSearchable term, String termText) {
		Vector<PwaSearchable> vecTermsAux=(Vector)htableMergers.get(termText);
		if (vecTermsAux==null) {
			vecTermsAux=new Vector<PwaSearchable>();					
		}			
		vecTermsAux.add(term); 
		htableMergers.put(termText, vecTermsAux);
	}
	
	/**
	 * Add terms to mergers
	 * @param mergers
	 * @param htableMergers
	 * @param exclude
	 */
	private void addTerms2Merger(Vector<PwaMerger> mergers, LinkedHashMap htableMergers, boolean exclude) {
		Vector<PwaSearchable> vecTermsAux=null;
		Vector<PwaSearchable> vecTermsSearchableAux=null;		
		for (Iterator iter=htableMergers.values().iterator();iter.hasNext();) {
			vecTermsAux=(Vector)iter.next();			
			vecTermsSearchableAux=new Vector<PwaSearchable>(); 
			vecTermsSearchableAux.addAll(vecTermsAux);
			mergers.add(new PwaMerger(vecTermsSearchableAux,exclude));
		}
	}
	
	/**
	 * Extract terms
	 * @param terms query terms
	 * @note the extractTerms of Lucene uses a Set that eliminates duplicates. This is a problem for phrase queries. BUG nutchwax 0000583
	 */
	private void extractTerms(Query query, Vector terms) {
		if (query instanceof TermQuery) { 												
			terms.add(((TermQuery)query).getTerm());		
		}
		else if (query instanceof PhraseQuery) {
			terms.addAll(Arrays.asList(((PhraseQuery)query).getTerms())); 				
		}
		else if (query instanceof BooleanQuery) {
			List lclauses=((BooleanQuery)query).clauses();
			for (Iterator i = lclauses.iterator(); i.hasNext();) {
				BooleanClause clause = (BooleanClause) i.next();
				extractTerms(clause.getQuery(), terms);
			}	
		}		
	}

	
	/** 
	 * Scores and collects all matching documents
	 * @param hc the collector to which all matching documents are passed through
	 * {@link HitCollector#collect(int, float)}.
	 * <br>When this method is used the {@link #explain(int)} method should not be used.
	 */
	public void score(HitCollector hc) throws IOException {	
		while (next()) {
			hc.collect(doc(), score());  			  
		} 	 		  			
	}		
	  
	/**
	 * Move to next document
	 * @return true if has more documents; false otherwise
	 */
	public boolean next() throws IOException {
		return !empty && chainFilter.next();
	}	
	  
	/**
	 * Get document id
	 * @return document id
	 */
	public int doc() {
		return chainFilter.doc();
	}		  
	  	
	/**
	 * Get document score
	 */
	public float score() throws IOException {				
		if (scoreType==ScoreType.NORMAL) {		  
			PwaRawFeatureCollector collector=new PwaRawFeatureCollector(reader);
			joiner.collectFeatures(doc(),collector);		
			PwaScores scores=PwaScorerFeatures.score(doc(),queryTimestamp,collector,joiner.getPositionsManager(),searcher,functions);
			return (new PwaLinearRankingModel()).score(functions, scores); // TODO parameterize the ranking model in the future
		}
		else if (scoreType==ScoreType.DATE_SORTED || scoreType==ScoreType.DATE_SORTED_REVERSE) { // results are sorted in TopDocCollector
			PwaDateCache sortCache=new PwaDateCache(reader);
			return sortCache.getTimestamp(doc());			
		}
		else { // flat ranking
			return 1;
		}
	}
	  
	/**
	 * Display ranking data
	 * @param doc document id
	 * @return
	 * @throws IOException
	 * @note a new PwaScorer should be created for each explain call. This method has lack of efficiency, it is just for debugging purposes. 
	 */
	public Explanation explain(int doc) throws IOException {		
		if (!joiner.skipToFromStart(doc)) { 
			throw new IOException("Explain failed skipToFromStart:"+doc);
		}		  
		if (doc!=doc()) {   //sanity check
			throw new IOException("Explain with different doc ids:"+doc+" "+doc());
		}	
		  		  		 
		PwaRawFeatureCollector collector=new PwaRawFeatureCollector(reader);
		joiner.collectFeatures(doc(),collector);					
		return PwaScorerFeatures.explain(doc(),queryTimestamp,collector,joiner.getPositionsManager(),searcher,functions);		       	
	}
	  	  
	/**
	 * Skip to document @doc or superior
	 * @return true if skip to document @targetDoc or superior; false otherwise
	 */
	public boolean skipTo(int targetDoc) throws IOException {		 
		throw new IOException("this method should not be called!");
	}
}
