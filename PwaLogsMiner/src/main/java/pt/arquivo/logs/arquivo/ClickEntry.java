package pt.arquivo.logs.arquivo;


/**
 * Click entry
 * @author Miguel Costa
 */
public class ClickEntry extends ProcessedEntry {	
    private int rank;
	
    public ClickEntry(int rank) {
    	super(LogAnalyzer.UserAction.SERP_CLICK);
		this.rank=rank;		
	}
	
	public int getRank() {
		return rank;
	}
	public void setRank(int rank) {
		this.rank = rank;
	}
}