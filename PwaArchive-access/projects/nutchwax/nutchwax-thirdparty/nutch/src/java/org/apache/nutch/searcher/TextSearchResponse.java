package org.apache.nutch.searcher;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class TextSearchResponse {
	
	private String serviceName;
	private String linkToService;
	@SerializedName( "request-parameters" ) 
	private TextSearchRequestParameters requestParameters; //request input parameters
	@SerializedName( "response-items" )
	private List<Item> itens;

	
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

	@Override
	public String toString() {
		return "OpenSearchResponse [serviceName=" + serviceName + ", linkToService=" + linkToService
				+ ", requestParameters=" + requestParameters + ", itens=" + itens + "]";
	}

}
