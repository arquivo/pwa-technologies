package pt.arquivo.logs.arquivo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;


/**
 * Record all information regarding the clicks of queries and their rank positions
 * Structure: query, numResults, numClicks, <url, rank, numClicks <sessionid, numClicks, clicksOrderInSession, clicksOrderInQueryOfSession>>
 *  
 * @author Miguel Costa
 */
public class Clicks {
	
	private final static int MIN_CLICKS=20; // minimum number of clicks considered for analysis
	private int numClicks;
	private HashMap<String,QueryClicks> queryClicksMap; // clicks per query
	
	public Clicks() {
		this.numClicks=0;
		queryClicksMap=new HashMap<String,QueryClicks>();
	}
	
	public int getNumClicks() {
		return numClicks;
	}
	
	/**
	 * Add query
	 * @param query query submitted
	 * @param numResults number of results returned for query	
	 * @param url URL clicked
	 * @param rank rank of the URL ranked
	 * @param sessionId session id
	 * @param clickOrderinSession order of click in session for this URL in this session
	 * @param clickOrderinQueryofSession order of click in query of session for this URL in this session
	 */
	public void addQueryClick(String query, int numResults, String url, int rank, String sessionId, int clickOrderinSession, int clickOrderinQueryofSession) {
		QueryClicks queryClicks=queryClicksMap.get(query);
		if (queryClicks==null) {
			queryClicks=new QueryClicks(query, numResults);
		}		
		// sanity check
		if (numResults!=queryClicks.getNumResults()) { // this value varies as we add more collections
			throw new RuntimeException("sanity check failed in query "+query+": numResults1: "+numResults+", numResults2: "+queryClicks.getNumResults());
		}
		
		queryClicks.addUrlClick(url,rank,sessionId,clickOrderinSession,clickOrderinQueryofSession);	
		numClicks++;
		
		queryClicksMap.put(query,queryClicks);
	}
	
	
	/**
	 * Write click statistics to stdout 
	 */
	public void writeStats() {
		System.out.println("Clicks stats:");
		System.out.println(" Total clicks: "+numClicks);
		
		/*
		for (Iterator<Map.Entry<String,QueryClicks>> iter=queryClicksMap.entrySet().iterator();iter.hasNext();) { 
			Map.Entry<String,QueryClicks> entry = (Map.Entry<String,QueryClicks>) iter.next();
			QueryClicks queryClicks=entry.getValue();
			System.out.println(" query:"+entry.getKey()+" nResults:"+queryClicks.getNumResults()+" nClicks:"+queryClicks.getNumClicks());
			*/
		Object queryClicksArray[]=QueryClicksSorter.sortQueryClicks(queryClicksMap); // sort by number of clicks		
		for (int j=0;j<queryClicksArray.length;j++) {
			QueryClicks queryClicks=(QueryClicks)((Map.Entry)queryClicksArray[j]).getValue();	
			
			if (queryClicks.getNumClicks()>=MIN_CLICKS) {
				System.out.println(" query:"+(String)((Map.Entry)queryClicksArray[j]).getKey()+" nResults:"+queryClicks.getNumResults()+" nClicks:"+queryClicks.getNumClicks());
								
				for (Iterator<Map.Entry<String,UrlClicks>> iter2=queryClicks.getUrlClicks();iter2.hasNext();) {
					Map.Entry<String,UrlClicks> entry2 = (Map.Entry<String,UrlClicks>) iter2.next();
					UrlClicks urlClicks=entry2.getValue();			
					System.out.println("  url:"+entry2.getKey()+" rank:"+urlClicks.getRank()+" nClicks:"+urlClicks.getNumClicks()+" nSessions:"+urlClicks.getNumSessions());
				
					for (Iterator<Map.Entry<String,UrlSessionClicks>> iter3=urlClicks.getUrlSessionClicks();iter3.hasNext();) {
						Map.Entry<String,UrlSessionClicks> entry3 = (Map.Entry<String,UrlSessionClicks>) iter3.next();
						UrlSessionClicks urlSessionClicksClicks=entry3.getValue();						
						System.out.println("   sessionId:"+entry3.getKey()+" nClicks:"+urlSessionClicksClicks.getNumClicks());
					
						Vector<Integer> clicksOrderInSession=urlSessionClicksClicks.getClickOrdersInSession();
						Vector<Integer> clicksOrderInQueryOfSession=urlSessionClicksClicks.getClickOrdersInQueryOfSession();					
						for (int i=0;i<clicksOrderInSession.size();i++) {
							System.out.println("    orderInSession:"+clicksOrderInSession.get(i)+" orderInQueryOfSession:"+clicksOrderInQueryOfSession.get(i));
						}
					}				
				}
			}
		}
	}
}
