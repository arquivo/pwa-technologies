package pt.arquivo.logs.arquivo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Query clicks
 * @author Miguel Costa
 */
public class QueryClicks {
	
	private String query; 
	private int numResults;
	private int numClicks;
	private HashMap<String,UrlClicks> urlClicksMap; // clicks in a URL
	
	
	/**
	 * 
	 * @param query query submitted
	 * @param numResults number of results returned for query
	 */
	public QueryClicks(String query, int numResults) {
		this.query=query;
		this.numResults=numResults;
		this.numClicks=0;
		this.urlClicksMap=new HashMap<String,UrlClicks>(); 
	}
	
	public String getQuery() {
		return query;
	}
	
	public int getNumResults() {
		return numResults;
	}
	
	public int getNumClicks() {
		return numClicks;
	}	
	
	public Iterator<Map.Entry<String,UrlClicks>> getUrlClicks() {
		return urlClicksMap.entrySet().iterator();		
	}
	
	public UrlClicks getUrlClicks(String url) {
		return urlClicksMap.get(url);
	}
		
	
	public void addUrlClick(String url, int rank, String sessionId, int clickOrderinSession, int clickOrderInQueryOfSession) {
		url=url+" "+rank; // TODO remove when the real URL will be added here *****************************************************************
		
		UrlClicks urlClicks=urlClicksMap.get(url);
		if (urlClicks==null) {
			urlClicks=new UrlClicks(rank);
		}
		// sanity check
		if (rank!=urlClicks.getRank()) {
			throw new RuntimeException("sanity check failed: rank1: "+rank+", rank2: "+urlClicks.getRank());
		}
		
		urlClicks.addUrlSessionClick(sessionId,clickOrderinSession,clickOrderInQueryOfSession);
		numClicks++;
		
		urlClicksMap.put(url,urlClicks);
	}
		
}
