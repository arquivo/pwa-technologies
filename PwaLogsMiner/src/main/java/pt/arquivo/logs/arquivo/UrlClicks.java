package pt.arquivo.logs.arquivo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * URL clicks for a query
 * @author Miguel Costa
 */
public class UrlClicks {
	//private String url;
	private int rank;
	private int numClicks;	
	private HashMap<String,UrlSessionClicks> urlSessionClicksMap; // list of clicks in a URL per session
	
	
	public UrlClicks(int rank) {
		this.rank=rank;
		this.numClicks=0;
		urlSessionClicksMap=new HashMap<String,UrlSessionClicks>();
	}	
		
	public int getRank() {
		return rank;
	}
	
	public int getNumClicks() {
		return numClicks;
	}		
	
	public Iterator<Map.Entry<String,UrlSessionClicks>> getUrlSessionClicks() {
		return urlSessionClicksMap.entrySet().iterator();		
	}
	
	public UrlSessionClicks getUrlSessionClicks(String sessionId) {
		return urlSessionClicksMap.get(sessionId);
	}
	
	/**
	 * Get number of sessions with clicks for this query,URL pair
	 * @return number of sessions
	 */
	public int getNumSessions() {
		return urlSessionClicksMap.size();
	}
	
	
	public void addUrlSessionClick(String sessionId, int clickOrderInSession, int clickOrderInQueryOfSession) {
		UrlSessionClicks urlSessionClicks=urlSessionClicksMap.get(sessionId);
		if (urlSessionClicks==null) {
			urlSessionClicks=new UrlSessionClicks();
		}
		urlSessionClicks.addClick(clickOrderInSession,clickOrderInQueryOfSession);
		numClicks++;
		
		urlSessionClicksMap.put(sessionId,urlSessionClicks);
	}
		
}
