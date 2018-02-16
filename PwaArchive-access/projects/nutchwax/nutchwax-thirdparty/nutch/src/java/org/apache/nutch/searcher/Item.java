package org.apache.nutch.searcher;

import com.google.gson.annotations.SerializedName;

public class Item {
	@SerializedName( "versionID" )
	private String key;
	@SerializedName( "title" )
	private String title;
	@SerializedName( "originalURL" )
	private String source;
	@SerializedName( "linkToArchive" )
	private String link;
	private String tstamp;
	private String contentLength;
	private String digest;
	private String mimeType;
	@SerializedName( "linkToScreenshot" )
	private String screenShotLink;
	@SerializedName( "linkToExtractedText" )
	private String itemText;
	private String date; //epoch format
	private String encoding;
	@SerializedName( "linkToNoFrame" )
	private String noFrameLink;
	@SerializedName( "snippet" )
	private String snippetForTerms;
	private String status;
	private String collection;
	@SerializedName( "extractedText" )
	private String parseText;
	
	//details
	@SerializedName( "filename" )
	private String arcname;
	@SerializedName( "offset" )
	private String arcoffset;
	private String idDoc;
	private String index;
	private String segment;


	public Item( ) { }
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	public String getLink() {
		return link;
	}
	public void setLink(String link) {
		this.link = link;
	}
	public String getTstamp() {
		return tstamp;
	}
	public void setTstamp(String tstamp) {
		this.tstamp = tstamp;
	}
	public String getContentLength() {
		return contentLength;
	}
	public void setContentLength(String contentLength) {
		this.contentLength = contentLength;
	}
	public String getDigest() {
		return digest;
	}
	public void setDigest(String digest) {
		this.digest = digest;
	}

	public String getScreenShotLink() {
		return screenShotLink;
	}
	public void setScreenShotLink(String screenShotLink) {
		this.screenShotLink = screenShotLink;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getItemText() {
		return itemText;
	}
	public void setItemText(String itemText) {
		this.itemText = itemText;
	}
	public String getEncoding() {
		return encoding;
	}
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	public String getArcname() {
		return arcname;
	}
	public void setArcname(String arcname) {
		this.arcname = arcname;
	}
	public String getArcoffset() {
		return arcoffset;
	}
	public void setArcoffset(String arcoffset) {
		this.arcoffset = arcoffset;
	}
	public String getCollection() {
		return collection;
	}
	public void setCollection(String collection) {
		this.collection = collection;
	}
	public String getIdDoc() {
		return idDoc;
	}
	public void setIdDoc(String idDoc) {
		this.idDoc = idDoc;
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public String getSegment() {
		return segment;
	}
	public void setSegment(String segment) {
		this.segment = segment;
	}
	public String getNoFrameLink() {
		return noFrameLink;
	}
	public void setNoFrameLink(String noFrameLink) {
		this.noFrameLink = noFrameLink;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public String getSnippetForTerms() {
		return snippetForTerms;
	}
	public void setSnippetForTerms(String snippetForTerms) {
		this.snippetForTerms = snippetForTerms;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getParseText() {
		return parseText;
	}
	public void setParseText(String parseText) {
		this.parseText = parseText;
	}

	@Override
	public String toString() {
		return "Item [key=" + key + ", title=" + title + ", source=" + source + ", link=" + link + ", tstamp=" + tstamp
				+ ", contentLength=" + contentLength + ", digest=" + digest + ", mimeType=" + mimeType
				+ ", screenShotLink=" + screenShotLink + ", itemText=" + itemText + ", date=" + date + ", encoding="
				+ encoding + ", noFrameLink=" + noFrameLink + ", snippetForTerms=" + snippetForTerms + ", status="
				+ status + ", collection=" + collection + ", parseText=" + parseText + ", arcname=" + arcname
				+ ", arcoffset=" + arcoffset + ", idDoc=" + idDoc + ", index=" + index + ", segment=" + segment + "]";
	}
	
	
}
