package org.apache.nutch.searcher;

import java.util.List;

public class OpenSearchResponse {
	
	private String serviceName;
	private String linkToService;
	private String queryTerms;
	private int start;
	private int limit;
	private int limitPerSite;
	private String sort;
	private List<Item> itens;
	
	public OpenSearchResponse(String serviceName, String linkToService, String queryTerms, int start, int limit,
			int limitPerSite, String sort, List<Item> itens) {
		super();
		this.serviceName 	= serviceName;
		this.linkToService 	= linkToService;
		this.queryTerms 	= queryTerms;
		this.start 			= start;
		this.limit 			= limit;
		this.limitPerSite 	= limitPerSite;
		this.sort 			= sort;
		this.itens 			= itens;
	}
	
	public OpenSearchResponse( ) { }

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

	public String getQueryTerms() {
		return queryTerms;
	}

	public void setQueryTerms(String queryTerms) {
		this.queryTerms = queryTerms;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getLinkToService() {
		return linkToService;
	}
	public void setLinkToService(String linkToService) {
		this.linkToService = linkToService;
	}
	public String getQuery() {
		return queryTerms;
	}
	public void setQuery(String queryTerms) {
		this.queryTerms = queryTerms;
	}

	public List< Item > getItens() {
		return itens;
	}

	public void setItens(List< Item > itens) {
		this.itens = itens;
	}

	@Override
	public String toString( ) {
		return "OpenSearchResponse [serviceName=" + serviceName + ", linkToService=" + linkToService + ", query="
				+ queryTerms + ", itens=" + itens + "]";
	}
	
}
