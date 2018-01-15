package org.apache.nutch.searcher;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class TextSearchResponse {
	
	private String serviceName;
	private String linkToService;
	private String next_page;
	private String previous_page;
	@SerializedName( "request_parameters" ) 
	private TextSearchRequestParameters requestParameters; //request input parameters
	@SerializedName( "response_items" )
	private List<Item> itens;
	@SerializedName( "total_items" )
	private String totalItems;

	
	public TextSearchResponse( ) { }

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

	public List< Item > getItens() {
		return itens;
	}

	public void setItens(List< Item > itens) {
		this.itens = itens;
	}
	
	public TextSearchRequestParameters getRequestParameters() {
		return requestParameters;
	}
	
	public void setRequestParameters(TextSearchRequestParameters requestParameters) {
		this.requestParameters = requestParameters;
	}

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

	
	
	public String getTotalItems() {
		return totalItems;
	}

	public void setTotalItems(String totalItems) {
		this.totalItems = totalItems;
	}

	@Override
	public String toString() {
		return "TextSearchResponse [serviceName=" + serviceName + ", linkToService=" + linkToService + ", next_page="
				+ next_page + ", previous_page=" + previous_page + ", requestParameters=" + requestParameters
				+ ", itens=" + itens + ", totalItems=" + totalItems + "]";
	}

}
