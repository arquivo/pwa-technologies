package org.apache.lucene.search;

import java.io.IOException;
import java.util.Vector;

import org.apache.lucene.search.caches.PwaIndexStats;
import org.apache.lucene.search.rankers.querydependent.*;
import org.apache.lucene.search.rankers.queryindependent.*;
import org.apache.lucene.document.Document;


/**
 * Ranking model
 * @author Miguel Costa
 * 
 * @note having all in the same class enables to optimize processing and choose a linear or non-linear model
 */
public class PwaRanker {
	
	private final static String BOOST_LABEL="boost";	
	
		
	/**
	 * Ranking model that computes score	 
	 * @param doc document identifier
	 * @param collector ranking features collector
	 * @param posmanagers query term position into the document 
	 * @param searcher searcher
	 * @param functions ranking functions 
	 * @return ranking score
	 */
	public static float score(int doc, PwaRawFeatureCollector collector, Vector<PwaPositionsManager> posmanagers, Searcher searcher, PwaFunctionsWritable functions) throws IOException {
		float score=0;		
		Vector<Integer> vecTfs;
		Vector<Integer> vecIdfs;
		int fieldLength;
		double fieldAvgLength;
		int nDocs=collector.getNumDocs();
		Vector<Vector<Integer>> tfPerField=new Vector<Vector<Integer>>();
		Vector<Vector<Integer>> idfPerField=new Vector<Vector<Integer>>();
		Vector<Integer> nTermsPerField=new Vector<Integer>();	
		int funct=0;
		float boost;				
				
		if (!collector.isEmpty()) {
			// term features
			for (int i=0;i<PwaIndexStats.FIELDS.length;i++) {				
				vecTfs=collector.getFieldTfs(PwaIndexStats.FIELDS[i]); // vector of all query terms	
				vecIdfs=collector.getFieldIdfs(PwaIndexStats.FIELDS[i]); // vector of all query terms						
				fieldLength=collector.getFieldLength(PwaIndexStats.FIELDS[i]);
				fieldAvgLength=collector.getFieldAvgLength(PwaIndexStats.FIELDS[i]);			
			
				if (functions.hasFunction(funct)) {
					boost=functions.getBoost(funct);
					score+= (new PwaTFxIDF(vecTfs,vecIdfs,fieldLength,nDocs)).score() * boost; // "TFxIDF-"+PwaIndexStats.FIELDS[i]					
				}
				funct++;
				if (functions.hasFunction(funct)) {
					boost=functions.getBoost(funct);
					score+= (new PwaBM25(vecTfs,vecIdfs,fieldLength,fieldAvgLength,nDocs)).score() * boost; // "BM25-"+PwaIndexStats.FIELDS[i]				
				}
				funct++;
				
				// add vectors for lucene
				tfPerField.add(collector.getFieldTfs(PwaIndexStats.FIELDS[i]));
				idfPerField.add(collector.getFieldIdfs(PwaIndexStats.FIELDS[i]));
				nTermsPerField.add(collector.getFieldLength(PwaIndexStats.FIELDS[i]));							
			}	
			if (functions.hasFunction(funct)) {
				boost=functions.getBoost(funct);
				score+= (new PwaLuceneSimilarity(tfPerField,idfPerField,nTermsPerField,nDocs)).score() * boost; // Lucene
			}
			funct++;

			// distance features
			for (int i=0;i<PwaIndexStats.FIELDS.length;i++) { // or for (i=0;i<posmanagers.length;i++) {  // per field
				if (posmanagers.size()>0 && (functions.hasFunction(funct) || functions.hasFunction(funct+1) || functions.hasFunction(funct+2))) {					
					posmanagers.get(i).computeDistances(doc);
					if (functions.hasFunction(funct)) {
						boost=functions.getBoost(funct);
						score+= (new PwaMinSpan(posmanagers.get(i).getMinSpanCovOrdered())).score() * boost; // "MinSpanCovOrd-"+PwaIndexStats.FIELDS[i]
					}
					funct++;
					if (functions.hasFunction(funct)) {
						boost=functions.getBoost(funct);
						score+= (new PwaMinSpan(posmanagers.get(i).getMinSpanCovUnordered())).score() * boost; // "MinSpanCovUnord-"+PwaIndexStats.FIELDS[i]
					}
					funct++;
					if (functions.hasFunction(funct)) {
						boost=functions.getBoost(funct);
						score+= (new PwaMinSpan(posmanagers.get(i).getMinPairDist())).score() * boost; // "MinPairDist-"+PwaIndexStats.FIELDS[i]
					}
					funct++;
				}
				else {
					funct+=3;
				}
			}			
		}
								
        // query independent features
		if (functions.hasFunction(funct) || functions.hasFunction(funct+1) || functions.hasFunction(funct+2) || functions.hasFunction(funct+4)) {										
			Document docMeta=searcher.doc(doc);				
			if (functions.hasFunction(funct)) {
				String surl=docMeta.get("url");
				boost=functions.getBoost(funct);
				score+= (new PwaUrlDepth(surl)).score() * boost; // "UrlDepth"			
			}
			funct++;
			if (functions.hasFunction(funct)) {
				String spagerank=docMeta.get("pagerank");
				boost=functions.getBoost(funct);
				score+= (new PwaLinPagerank(Float.parseFloat(spagerank))).score() * boost; // "LinPagerank"			
			}
			funct++;
			if (functions.hasFunction(funct)) {
				String sinlinks=docMeta.get("inlinks");
				boost=functions.getBoost(funct);
				score+= (new PwaLinInlinks(Integer.parseInt(sinlinks))).score() * boost; // "LinInlinks"		
			}
			funct++;
			if (functions.hasFunction(funct)) {
				String sboost=docMeta.get("boost");
				boost=functions.getBoost(funct);
				score+= Float.parseFloat(sboost) * boost; // OPIC
			}
			funct++;
		}

		return score;
	}
		
	
	/**
	 * Display all features	
	 * @param doc document identifier
	 * @param collector ranking features collector
	 * @param posmanagers query term position into the document 
	 * @param searcher searcher
	 * @param functions ranking functions 
	 * @return explanation
	 */
	public static Explanation explain(int doc, PwaRawFeatureCollector collector, Vector<PwaPositionsManager> posmanagers, Searcher searcher, PwaFunctionsWritable functions) throws IOException {			
		Vector<Integer> vecTfs;
		Vector<Integer> vecIdfs;
		Vector<String>  vecTermsText;
		int fieldLength;
		double fieldAvgLength;
		int nDocs=collector.getNumDocs();
		Vector<Vector<Integer>> tfPerField=new Vector<Vector<Integer>>();
		Vector<Vector<Integer>> idfPerField=new Vector<Vector<Integer>>();
		Vector<Integer> nTermsPerField=new Vector<Integer>();
		Explanation allExpl = new Explanation(doc,"Document");
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
		if (functions.hasFunction(funct) || functions.hasFunction(funct+1) || functions.hasFunction(funct+2) || functions.hasFunction(funct+4)) {								
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
