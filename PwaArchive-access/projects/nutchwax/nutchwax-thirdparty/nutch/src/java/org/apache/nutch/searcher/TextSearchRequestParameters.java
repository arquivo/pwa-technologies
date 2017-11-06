package org.apache.nutch.searcher;

import com.google.gson.annotations.SerializedName;

public class TextSearchRequestParameters {
	
	@SerializedName( "q" ) 
	private String queryTerms;
	@SerializedName( "offset" )
	private int start;
	private int limit;
	private int limitPerSite;
	private String sort;
	private String from;
	private String to;
	private String type;
	private String site;
	private String prettyPrint;
	private String next_page;
	private String previous_page;
	
	public TextSearchRequestParameters( ) { }
	
	
	public String getNext_page() {
		return next_page;
	}
	public void setNext_page(String next_page) {
		this.next_page = next_page;
	}
	public String getPrevious_page() {
		return previous_page;
	}
	public void setPrevious_page(String previous_page) {
		this.previous_page = previous_page;
	}
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

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getPrettyPrint() {
		return prettyPrint;
	}

	public void setPrettyPrint(String prettyPrint) {
		this.prettyPrint = prettyPrint;
	}


	@Override
	public String toString() {
		return "TextSearchRequestParameters [queryTerms=" + queryTerms + ", start=" + start + ", limit=" + limit
				+ ", limitPerSite=" + limitPerSite + ", sort=" + sort + ", from=" + from + ", to=" + to + ", type="
				+ type + ", site=" + site + ", prettyPrint=" + prettyPrint + ", next_page=" + next_page
				+ ", previous_page=" + previous_page + "]";
	}

	
}
