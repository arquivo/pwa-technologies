package pt.arquivo.logs.arquivo;

import java.util.Vector;


/**
 * Clicks in a URL for a submitted query in a session
 * @author Miguel Costa
 */
public class UrlSessionClicks {
	private int numClicks; // number of clicks in session
	private Vector<Integer> clicksOrderInSession; // click order in session (e.g. first, last)
	private Vector<Integer> clicksOrderInQueryOfSession; // click order in query of session (e.g. first, last)
	
	public UrlSessionClicks() {
		numClicks=0;
		clicksOrderInSession=new Vector<Integer>();
		clicksOrderInQueryOfSession=new Vector<Integer>();		
	}
	
	public int getNumClicks() {
		return numClicks;
	}
	
	public Vector<Integer> getClickOrdersInSession() {
		return clicksOrderInSession;
	}
	
	public int getClickOrderInSession(int index) {
		return clicksOrderInSession.get(index);
	}
	
	public Vector<Integer> getClickOrdersInQueryOfSession() {
		return clicksOrderInQueryOfSession;
	}
	
	public int getClickOrderInQueryOfSession(int index) {
		return clicksOrderInQueryOfSession.get(index);
	}
	
	public void addClick(int clickOrderInSession, int clickOrderInQueryOfSession) {
		clicksOrderInSession.add(clickOrderInSession);
		clicksOrderInQueryOfSession.add(clickOrderInQueryOfSession);
		numClicks++;
	}
}
