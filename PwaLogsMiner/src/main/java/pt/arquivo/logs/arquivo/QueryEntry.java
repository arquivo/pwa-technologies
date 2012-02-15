package pt.arquivo.logs.arquivo;


/**
 * Query entry
 * @author Miguel Costa
 */
public class QueryEntry extends ProcessedEntry {
	private String query;
    private int numResults;
    private boolean isDatesChanged; // whether start or end dates were changed
	
    public QueryEntry(String query, int numResults, boolean isDatesChanged) {
    	super(LogAnalyzer.UserAction.QUERY);
		this.query=query;
		this.numResults=numResults;
		this.isDatesChanged=isDatesChanged;
	}
	
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public int getNumResults() {
		return numResults;
	}
	public void setNumResults(int numResults) {
		this.numResults = numResults;
	}	
	public boolean isDatesChanged() {
		return isDatesChanged;
	}
}