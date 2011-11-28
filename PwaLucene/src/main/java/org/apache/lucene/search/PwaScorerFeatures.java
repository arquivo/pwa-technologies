package org.apache.lucene.search;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.commons.codec.binary.Base64;
import org.apache.lucene.search.caches.PwaDateCache;
import org.apache.lucene.search.caches.PwaIndexStats;
import org.apache.lucene.search.features.*;
import org.apache.lucene.search.features.querydependent.*;
import org.apache.lucene.search.features.queryindependent.*;
import org.apache.lucene.search.features.temporal.*;
import org.apache.lucene.search.memcached.*;
import org.apache.lucene.document.Document;


/**
 * Scores ranking features
 * @author Miguel Costa
 */
public class PwaScorerFeatures {
	
	private final static String BOOST_LABEL="boost";	
	
	private final static String MEMCACHED_ADDRESSES="193.136.192.57:11111"; //memcached TODO parameterize
	//private final static String MEMCACHED_ADDRESSES="127.0.0.1:11211"; //membase
	private static Memcached cache=null;
	private static int maxVersions;
	private static int maxSpan;
	
		
	/**
	 * Ranking model that computes score	 
	 * @param doc document identifier
	 * @param queryTimestamp timestamp when the query was submitted
	 * @param collector ranking features collector
	 * @param posmanagers query term position into the document 
	 * @param searcher searcher
	 * @param functions ranking functions 
	 * @return ranking score
	 */
	public static PwaScores score(int doc, long queryTimestamp, PwaRawFeatureCollector collector, Vector<PwaPositionsManager> posmanagers, Searcher searcher, PwaFunctionsWritable functions) throws IOException {		
		PwaScores scores=new PwaScores();		
		int nDocs=collector.getNumDocs();
		Vector<Integer> vecTfs;
		Vector<Integer> vecIdfs;
		int fieldLength;
		double fieldAvgLength;		
		Vector<Vector<Integer>> tfPerField=new Vector<Vector<Integer>>();
		Vector<Vector<Integer>> idfPerField=new Vector<Vector<Integer>>();
		Vector<Integer> nTermsPerField=new Vector<Integer>();	
		int funct=0; // function index		
		String surl=null; // URL string 
				
		// query dependent features
		if (!collector.isEmpty()) {
			// term features
			for (int i=0;i<PwaIndexStats.FIELDS.length;i++) {				
				vecTfs=collector.getFieldTfs(PwaIndexStats.FIELDS[i]); // vector of all query terms	
				vecIdfs=collector.getFieldIdfs(PwaIndexStats.FIELDS[i]); // vector of all query terms						
				fieldLength=collector.getFieldLength(PwaIndexStats.FIELDS[i]);
				fieldAvgLength=collector.getFieldAvgLength(PwaIndexStats.FIELDS[i]);			
			
				if (functions.hasFunction(funct)) {
					float score=0;
					for (int j=0;j<vecTfs.size();j++) {	// for all terms					
						score+= vecTfs.get(j); 
					}
					scores.addScore(funct, score); // sum of the frequency of each term
				}
				funct++;								
				if (functions.hasFunction(funct)) {
					float score=0;
					for (int j=0;j<vecIdfs.size();j++) {	// for all terms					
						score+= vecIdfs.get(j); 
					}
					scores.addScore(funct, score); // sum of the inverse document frequency of each term
				}
				funct++;							
				if (functions.hasFunction(funct)) {
					scores.addScore(funct, fieldLength); // field length			
				}
				funct++;				
				if (functions.hasFunction(funct)) {
					scores.addScore(funct, (float)fieldAvgLength); // field average length			
				}
				funct++;								
				if (functions.hasFunction(funct)) {
					scores.addScore(funct, (new PwaTFxIDF(vecTfs,vecIdfs,fieldLength,nDocs)).score()); // "TFxIDF-"+PwaIndexStats.FIELDS[i] 					
				}
				funct++;
				if (functions.hasFunction(funct)) {
					scores.addScore(funct, (new PwaBM25(vecTfs,vecIdfs,fieldLength,fieldAvgLength,nDocs)).score()); // "BM25-"+PwaIndexStats.FIELDS[i]				
				}
				funct++;
				
				// add values to vectors for lucene
				tfPerField.add(collector.getFieldTfs(PwaIndexStats.FIELDS[i]));
				idfPerField.add(collector.getFieldIdfs(PwaIndexStats.FIELDS[i]));
				nTermsPerField.add(collector.getFieldLength(PwaIndexStats.FIELDS[i]));							
			}	
			if (functions.hasFunction(funct)) {				
				scores.addScore(funct, (new PwaLuceneSimilarity(tfPerField,idfPerField,nTermsPerField,nDocs)).score()); // Lucene
			}
			funct++;
			if (functions.hasFunction(funct)) {				
				scores.addScore(funct, (new PwaNutchSimilarity(tfPerField,idfPerField,nTermsPerField,nDocs)).score()); // Nutch
			}
			funct++;

			// term distance features
			for (int i=0;i<PwaIndexStats.FIELDS.length;i++) { // or for (i=0;i<posmanagers.length;i++) {  // per field
				if (posmanagers.size()>0 && (functions.hasFunction(funct) || functions.hasFunction(funct+1) || functions.hasFunction(funct+2))) {					
					posmanagers.get(i).computeDistances(doc);
					if (functions.hasFunction(funct)) {						
						scores.addScore(funct, (new PwaMinSpan(posmanagers.get(i).getMinSpanCovOrdered())).score()); // "MinSpanCovOrd-"+PwaIndexStats.FIELDS[i]
					}
					funct++;
					if (functions.hasFunction(funct)) {						
						scores.addScore(funct, (new PwaMinSpan(posmanagers.get(i).getMinSpanCovUnordered())).score()); // "MinSpanCovUnord-"+PwaIndexStats.FIELDS[i]
					}
					funct++;
					if (functions.hasFunction(funct)) {						
						scores.addScore(funct, (new PwaMinSpan(posmanagers.get(i).getMinPairDist())).score()); // "MinPairDist-"+PwaIndexStats.FIELDS[i]
					}
					funct++;
				}
				else {
					funct+=3;
				}
			}			
		}
		else {
			funct+=PwaIndexStats.FIELDS.length*6 + 2 + PwaIndexStats.FIELDS.length*3;
		}
								
        // query independent features
		if (functions.hasFunction(funct) || functions.hasFunction(funct+1) || functions.hasFunction(funct+2) || functions.hasFunction(funct+3) || functions.hasFunction(funct+4)) {										
			Document docMeta=searcher.doc(doc);				
			if (functions.hasFunction(funct)) {
				surl=docMeta.get("url");				
				scores.addScore(funct, (new PwaUrlDepth(surl)).score()); // "UrlDepth"				
			}
			funct++;				
			if (functions.hasFunction(funct)) {
				surl=docMeta.get("url");									
				scores.addScore(funct, (new PwaUrlSlashes(surl)).score()); // "PwaUrlSlashes"				
			}
			funct++;			
			if (functions.hasFunction(funct)) {
				surl=docMeta.get("url");									
				scores.addScore(funct, surl.length()); // "URLLength"				
			}
			funct++;								
			if (functions.hasFunction(funct)) {
				String sinlinks=docMeta.get("inlinks");
				scores.addScore(funct, Integer.parseInt(sinlinks)); // "Inlinks"				
			}
			funct++;
			if (functions.hasFunction(funct)) {
				String sinlinks=docMeta.get("inlinks");
				scores.addScore(funct, (new PwaLinInlinks(Integer.parseInt(sinlinks))).score()); // "LinInlinks"				
			}
			funct++;
			/* do not work properly
			if (functions.hasFunction(funct)) {
				String spagerank=docMeta.get("pagerank");
				boost=functions.getBoost(funct);
				score+= Float.parseFloat(spagerank) * boost; // "Pagerank"			
			}
			funct++;			
			if (functions.hasFunction(funct)) {
				String spagerank=docMeta.get("pagerank");
				boost=functions.getBoost(funct);
				score+= (new PwaLinPagerank(Float.parseFloat(spagerank))).score() * boost; // "LinPagerank"			
			}
			funct++;
			if (functions.hasFunction(funct)) {
				String sboost=docMeta.get("boost");
				boost=functions.getBoost(funct);
				score+= Float.parseFloat(sboost) * boost; // OPIC
			}
			funct++;
			*/
		}
		else {
			funct+=5;
		}
		
		// temporal features - local timestamps 
		if (functions.hasFunction(funct) || functions.hasFunction(funct+1) || functions.hasFunction(funct+2) || functions.hasFunction(funct+3) || functions.hasFunction(funct+4) || functions.hasFunction(funct+5)) {
			PwaDateCache cache=new PwaDateCache(null); // already initialized
			long timestamp=cache.getTimestamp(doc);
			long minTimestamp=cache.getMinTimestamp();
			long maxTimestamp=cache.getMaxTimestamp();					
		
			if (functions.hasFunction(funct)) {				
				scores.addScore(funct, (new PwaBoostNewer(timestamp,maxTimestamp,minTimestamp)).score()); // BoostNewer				
			}
			funct++;
			if (functions.hasFunction(funct)) {
				scores.addScore(funct, (new PwaBoostOlder(timestamp,maxTimestamp,minTimestamp)).score()); // BoostOlder
			}
			funct++;
			if (functions.hasFunction(funct)) {
				scores.addScore(funct, (new PwaBoostNewerAndOlder(timestamp,maxTimestamp,minTimestamp)).score()); // BoostNewerAndOlder
			}
			funct++;
			if (functions.hasFunction(funct)) {
				scores.addScore(funct, (new PwaAge(timestamp,queryTimestamp)).score()); // Age in days
			}
			funct++;				
			if (functions.hasFunction(funct)) {		
				scores.addScore(funct, timestamp / PwaIRankingFunction.DAY_MILLISEC); // Version's timestamp in days
			}
			funct++;			
			if (functions.hasFunction(funct)) {		
				scores.addScore(funct, queryTimestamp / PwaIRankingFunction.DAY_MILLISEC); // Query issue time in days
			}
			funct++;
		}
		else {
			funct+=6;
		}
		
		// temporal features - global timestamps
		if (functions.hasFunction(funct) || functions.hasFunction(funct+1) || functions.hasFunction(funct+2) || functions.hasFunction(funct+3) || functions.hasFunction(funct+4) || functions.hasFunction(funct+5)) {
			Document docMeta=null;
			if (surl==null) {
				docMeta=searcher.doc(doc);				
				surl=docMeta.get("url");
			}	
			
			UrlRow row=null;
			try {
				if (cache==null) {
					cache=new Memcached(MEMCACHED_ADDRESSES); // [address1=127.0.0.1:8091] [address2] ... [addressn]
					maxVersions=(Integer)cache.get(MemcachedTransactions.MAX_VERSIONS);
					maxSpan=(Integer)cache.get(MemcachedTransactions.MAX_SPAN);
				}
			
				String key=MemcachedTransactions.getUrlKey(surl);					
				row=cache.getRow(key);
			}
			catch (ConnectException e) { // error communicating with memcached. It will try to reconnect.
				// ignore
			}
			catch (IOException e) { 
				// ignore
			}				
			if (row==null) { // for urls discarded such as dynamics (there are not space to store everything)		
				if (docMeta==null) {
					docMeta=searcher.doc(doc);
					surl=docMeta.get("url");
				}
				int idate=MemcachedTransactions.stringdateToInt(docMeta.get("date"));	
				row=new UrlRow(1,idate,idate);
System.out.println("URL "+surl+" not cached."); // TODO remove
			}
						
			int nVersions=row.getNVersions();				
			long minTimestamp=MemcachedTransactions.intToLongdate(row.getMin());
			long maxTimestamp=MemcachedTransactions.intToLongdate(row.getMax());					
								
			if (functions.hasFunction(funct)) {
				scores.addScore(funct, minTimestamp / PwaIRankingFunction.DAY_MILLISEC); // Oldest version's timestamp in days
			}
			funct++;				
			if (functions.hasFunction(funct)) {						
				scores.addScore(funct, maxTimestamp / PwaIRankingFunction.DAY_MILLISEC ); // Newest version's timestamp in days
			}
			funct++;
			if (functions.hasFunction(funct)) {						
				scores.addScore(funct, (new PwaSpanVersions(maxTimestamp,minTimestamp)).score()); // Days between Versions
			}
			funct++;				
			if (functions.hasFunction(funct)) {						
				scores.addScore(funct, (new PwaSpanVersions(maxTimestamp,minTimestamp)).score() / ((maxSpan>0) ? maxSpan : 1)); // Span between Versions normalized
			}
			funct++;				
			if (functions.hasFunction(funct)) {							
				scores.addScore(funct, nVersions); // NumberVersions
			}
			funct++;																	
			if (functions.hasFunction(funct)) {							
				scores.addScore(funct, nVersions / maxVersions); // NumberVersions normalized
			}
			funct++;
						
			//cache.close();
		}
		else {
			funct+=6;
		}			
		
		return scores;
	}
		
	
	/**
	 * Display all features	
	 * @param doc document identifier
	 * @param queryTimestamp timestamp when the query was submitted
	 * @param collector ranking features collector
	 * @param posmanagers query term position into the document 
	 * @param searcher searcher
	 * @param functions ranking functions 
	 * @return explanation
	 */
	public static Explanation explain(int doc, long queryTimestamp, PwaRawFeatureCollector collector, Vector<PwaPositionsManager> posmanagers, Searcher searcher, PwaFunctionsWritable functions) throws IOException {					
		int key;		
		StringBuffer bufValue=new StringBuffer("Feature values of document "+doc+": <span class=\"features\">"); // feature values
		StringBuffer bufBoost=new StringBuffer("Feature boosts of document "+doc+": "); // feature boosts
		StringBuffer bufFinal=new StringBuffer("Feature values*boosts of document "+doc+": "); // feature final scores
		PwaScores scores=score(doc, queryTimestamp, collector, posmanagers, searcher, functions);
		
		Vector<Integer> vecKeys = new Vector<Integer>(functions.keySet());
		Collections.sort(vecKeys);
		for(int i=0;i<vecKeys.size();i++) {			
    		key=vecKeys.get(i);             
    		bufValue.append(" "+key+":"+scores.getScore(key));    
    		bufBoost.append(" "+key+":"+functions.getBoost(key));
    		bufFinal.append(" "+key+":"+scores.getScore(key)*functions.getBoost(key));
        }
		bufValue.append("</span>");
		Explanation allExpl = new Explanation(0,bufValue.toString());		   	
		Explanation expAux = new Explanation(0,bufBoost.toString());
		allExpl.addDetail(expAux);
		expAux = new Explanation(0,bufFinal.toString());
		allExpl.addDetail(expAux);
		return allExpl;
		
		/*
		Explanation allExpl = new Explanation(doc,"Document");
		Explanation expAux = null;
		int key;				
		
		PwaScores scores=score(doc, queryTimestamp, collector, posmanagers, searcher, functions);
		
		Vector<Integer> vecKeys = new Vector<Integer>(functions.keySet());
		Collections.sort(vecKeys);
		for(int i=0;i<vecKeys.size();i++) {			
    		key=vecKeys.get(i);             		
    		expAux=getExplainPart(new Explanation(scores.getScore(key),""+key),functions,key);    		
    		allExpl.addDetail(expAux);
        }		
		return allExpl;
		*/
				
		/* TODO remove		
		Vector<Integer> vecTfs;
		Vector<Integer> vecIdfs;
		Vector<String>  vecTermsText;
		int fieldLength;
		double fieldAvgLength;
		int nDocs=collector.getNumDocs();
		Vector<Vector<Integer>> tfPerField=new Vector<Vector<Integer>>();
		Vector<Vector<Integer>> idfPerField=new Vector<Vector<Integer>>();
		Vector<Integer> nTermsPerField=new Vector<Integer>();
		Explanation allExpl = new Explanation(doc,"Document")
		Explanation expAux;
		int funct=0;
							
		if (!collector.isEmpty()) {
			// term features
			for (int i=0;i<PwaIndexStats.FIELDS.length;i++) {				
				vecTfs=collector.getFieldTfs(PwaIndexStats.FIELDS[i]); // vector of all query terms	
				vecIdfs=collector.getFieldIdfs(PwaIndexStats.FIELDS[i]); // vector of all query terms
				fieldLength=collector.getFieldLength(PwaIndexStats.FIELDS[i]);
				fieldAvgLength=collector.getFieldAvgLength(PwaIndexStats.FIELDS[i]);		
				vecTermsText=collector.getFieldTermsText(PwaIndexStats.FIELDS[i]);
			
				expAux=new Explanation(0,"Text Features for field "+PwaIndexStats.FIELDS[i]);
				for (int j=0;j<vecTfs.size();j++) {
					expAux.addDetail(new Explanation(vecTfs.get(j),"tf "+vecTermsText.get(j)));
					expAux.addDetail(new Explanation(vecIdfs.get(j),"idf "+vecTermsText.get(j)));
				}
				expAux.addDetail(new Explanation(fieldLength,"fieldLength"));
				expAux.addDetail(new Explanation((float)fieldAvgLength,"fieldAvgLength"));
				allExpl.addDetail(expAux);
				
				if (functions.hasFunction(funct)) {
					expAux=getExplainPart(new Explanation((float)(new PwaTFxIDF(vecTfs,vecIdfs,fieldLength,nDocs)).score(),"TFxIDF-"+PwaIndexStats.FIELDS[i]),functions,funct);					
					allExpl.addDetail(expAux);
				}
				funct++;
				if (functions.hasFunction(funct)) {
					expAux=getExplainPart(new Explanation((float)(new PwaBM25(vecTfs,vecIdfs,fieldLength,fieldAvgLength,nDocs)).score(),"BM25-"+PwaIndexStats.FIELDS[i]),functions,funct);
					allExpl.addDetail(expAux);
				}
				funct++;
				
				// add vectors for lucene
				tfPerField.add(collector.getFieldTfs(PwaIndexStats.FIELDS[i]));
				idfPerField.add(collector.getFieldIdfs(PwaIndexStats.FIELDS[i]));
				nTermsPerField.add(collector.getFieldLength(PwaIndexStats.FIELDS[i]));							
			}	
			if (functions.hasFunction(funct)) {
				expAux=getExplainPart(new Explanation((float)(new PwaLuceneSimilarity(tfPerField,idfPerField,nTermsPerField,nDocs)).score(),"Lucene"),functions,funct);
				allExpl.addDetail(expAux);
			}
			funct++;
		
			// distance features
			for (int i=0;i<PwaIndexStats.FIELDS.length;i++) { // or for (i=0;i<posmanagers.length;i++) {  // per field
				if (posmanagers.size()>0 && (functions.hasFunction(funct) || functions.hasFunction(funct+1) || functions.hasFunction(funct+2))) {
					posmanagers.get(i).computeDistances(doc);
					
					expAux=new Explanation(0,"Distances for field "+PwaIndexStats.FIELDS[i]);				
					expAux.addDetail(new Explanation(posmanagers.get(i).getMinSpanCovOrdered(),"minSpanCovOrd"));
					expAux.addDetail(new Explanation(posmanagers.get(i).getMinSpanCovUnordered(),"minSpanCovUnord"));
					expAux.addDetail(new Explanation(posmanagers.get(i).getMinPairDist(),"minPairDist"));
					allExpl.addDetail(expAux);
								
					if (functions.hasFunction(funct)) {
						expAux=getExplainPart(new Explanation((float)(new PwaMinSpan(posmanagers.get(i).getMinSpanCovOrdered())).score(),"MinSpanCovOrd-"+PwaIndexStats.FIELDS[i]),functions,funct);
						allExpl.addDetail(expAux);
					}
					funct++;
					if (functions.hasFunction(funct)) {
						expAux=getExplainPart(new Explanation((float)(new PwaMinSpan(posmanagers.get(i).getMinSpanCovUnordered())).score(),"MinSpanCovUnord-"+PwaIndexStats.FIELDS[i]),functions,funct);
						allExpl.addDetail(expAux);
					}
					funct++;
					if (functions.hasFunction(funct)) {
						expAux=getExplainPart(new Explanation((float)(new PwaMinSpan(posmanagers.get(i).getMinPairDist())).score(),"MinPairDist-"+PwaIndexStats.FIELDS[i]),functions,funct);
						allExpl.addDetail(expAux);
					}
					funct++;									
				}
				else {
					funct+=3;
				}
			}
		}
								
        // query independent features
		if (functions.hasFunction(funct) || functions.hasFunction(funct+1) || functions.hasFunction(funct+2) || functions.hasFunction(funct+3)) {								
			Document docMeta=searcher.doc(doc);
			if (functions.hasFunction(funct)) {
				String surl=docMeta.get("url");
				expAux=getExplainPart(new Explanation((float)(new PwaUrlDepth(surl)).score(),"UrlDepth"),functions,funct);
				allExpl.addDetail(expAux);
			}
			funct++;
			if (functions.hasFunction(funct)) {
				String spagerank=docMeta.get("pagerank");
				expAux=getExplainPart(new Explanation((float)(new PwaLinPagerank(Float.parseFloat(spagerank))).score(),"LinPagerank"),functions,funct);
				allExpl.addDetail(expAux);
			}
			funct++;
			if (functions.hasFunction(funct)) {
				String sinlinks=docMeta.get("inlinks");
				expAux=getExplainPart(new Explanation((float)(new PwaLinInlinks(Integer.parseInt(sinlinks))).score(),"LinInlinks"),functions,funct);
				allExpl.addDetail(expAux);
			}
			funct++;
			if (functions.hasFunction(funct)) {
				String sboost=docMeta.get("boost");
				expAux=getExplainPart(new Explanation(Float.parseFloat(sboost),"OPIC"),functions,funct);
				allExpl.addDetail(expAux);
			}
			funct++;									
		}
		
		return allExpl;
		*/
	}		
	
	/**
	 * Get part of explanation
	 * @param expAux part of explanation
	 * @param functions ranking functions 
	 * @param index index of functions array
	 * @return
	 */
	private static Explanation getExplainPart(Explanation expAux, PwaFunctionsWritable functions, int index) {
		float boost=functions.getBoost(index);
		expAux.addDetail(new Explanation(boost,BOOST_LABEL));
		return expAux;
	}
}
