package org.apache.nutch.searcher;

public class OpenSearchRequestParameters {
	
	private String queryTerms;
	private int start;
	private int limit;
	private int limitPerSite;
	private String sort;
	
	public OpenSearchRequestParameters( ) { }
	
	public String getQueryTerms() {
		return queryTerms;
	}
	public void setQueryTerms(String queryTerms) {
		this.queryTerms = queryTerms;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getLimit() {
		return limit;
	}
	public void setLimit(int limit) {
		this.limit = limit;
	}
	public int getLimitPerSite() {
		return limitPerSite;
	}
	public void setLimitPerSite(int limitPerSite) {
		this.limitPerSite = limitPerSite;
	}
	public String getSort() {
		return sort;
	}
	public void setSort(String sort) {
		this.sort = sort;
	}

	@Override
	public String toString() {
		return "OpenSearchRequestParameters [queryTerms=" + queryTerms + ", start=" + start + ", limit=" + limit
				+ ", limitPerSite=" + limitPerSite + ", sort=" + sort + "]";
	}
	
	
	
}
