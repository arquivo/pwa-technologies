package pt.arquivo.logs.arquivo;


/**
 * Entry processed with data extracted from logs
 * @author Miguel Costa
 */
public class ProcessedEntry {
	
	private LogAnalyzer.UserAction action;
	
	public ProcessedEntry(LogAnalyzer.UserAction action) {
		this.action=action;
	}
	   
	public LogAnalyzer.UserAction getAction() {
		return action;
	}
}