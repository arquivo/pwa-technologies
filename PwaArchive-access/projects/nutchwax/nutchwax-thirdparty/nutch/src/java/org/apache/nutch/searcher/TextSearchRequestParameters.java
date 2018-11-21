package org.apache.nutch.searcher;

import com.google.gson.annotations.SerializedName;

public class TextSearchRequestParameters {
	
	@SerializedName( "q" )
	private String queryTerms;
	@SerializedName( "offset" )
	private String start;
	@SerializedName( "maxItems" )
	private String limit;
	@SerializedName( "itemsPerSite" )
	private String limitPerSite;
	private String sort;
	private String from;
	private String to;
	private String type;
	@SerializedName( "siteSearch" )
	private String site;
	@SerializedName("collection")
	private String collection;
	private String prettyPrint;
	
	public TextSearchRequestParameters( ) { }
	

	public String getQueryTerms() {
		return queryTerms;
	}
	public void setQueryTerms(String queryTerms) {
		this.queryTerms = queryTerms;
	}
	public String getStart() {
		return start;
	}
	public void setStart(String start) {
		this.start = start;
	}
	public String getLimit() {
		return limit;
	}
	public void setLimit(String limit) {
		this.limit = limit;
	}
	public String getLimitPerSite() {
		return limitPerSite;
	}
	public void setLimitPerSite(String limitPerSite) {
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

	public String getCollection() { return collection; }

	public void setCollection(String collection) { this.collection = collection; }

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
