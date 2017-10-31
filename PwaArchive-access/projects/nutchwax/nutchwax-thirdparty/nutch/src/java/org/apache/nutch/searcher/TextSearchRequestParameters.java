package org.apache.nutch.searcher;

import com.google.gson.annotations.SerializedName;

public class TextSearchRequestParameters {
	
	@SerializedName( "q" ) 
	private String queryTerms;
	private int start;
	private int limit;
	private int limitPerSite;
	private String sort;
	private String from;
	private String to;
	private String type;
	private String site;
	private String prettyPrint;
	
	public TextSearchRequestParameters( ) { }
	
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
				+ type + ", site=" + site + ", prettyPrint=" + prettyPrint + "]";
	}

	
}
