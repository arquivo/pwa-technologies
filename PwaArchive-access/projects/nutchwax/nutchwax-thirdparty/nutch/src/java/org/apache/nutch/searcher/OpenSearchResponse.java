package org.apache.nutch.searcher;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class OpenSearchResponse {
	
	private String serviceName;
	private String linkToService;
	@SerializedName( "request-parameters" ) 
	private OpenSearchRequestParameters requestParameters; //request input parameters
	@SerializedName( "response-items" )
	private List<Item> itens;

	
	public OpenSearchResponse( ) { }

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
	
	public OpenSearchRequestParameters getRequestParameters() {
		return requestParameters;
	}
	
	public void setRequestParameters(OpenSearchRequestParameters requestParameters) {
		this.requestParameters = requestParameters;
	}

	@Override
	public String toString() {
		return "OpenSearchResponse [serviceName=" + serviceName + ", linkToService=" + linkToService
				+ ", requestParameters=" + requestParameters + ", itens=" + itens + "]";
	}

}
