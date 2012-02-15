package pt.arquivo.logs.arquivo;

import java.util.Date;

/**
 * Log entry
 * @author Miguel Costa
 */
public class LogEntry {
	String query;
	String referrer;
	LogAnalyzer.UserAction action;
	Date date;
	
    public LogEntry(String query, String referrer, LogAnalyzer.UserAction action, Date date) {
		this.query=query;
		this.referrer=referrer;
		this.action=action;
		this.date=date;	       
	}
	
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public String getReferrer() {
		return referrer;
	}
	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}
	public LogAnalyzer.UserAction getAction() {
		return action;
	}
	public void setClick(LogAnalyzer.UserAction action) {
		this.action = action;
	}	
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
}