package org.apache.nutch.searcher;

import com.google.gson.annotations.SerializedName;

public class Item {
	@SerializedName( "version_id" )
	private String key;
	@SerializedName( "versionTitle" )
	private String title;
	private String source;
	@SerializedName( "linkToArchive" )
	private String link;
	private String tstamp;
	private String contentLength;
	private String digest;
	private String primaryType;
	private String subType;
	@SerializedName( "downloadImage" )
	private String screenShotLink;
	private String itemText;
	private String date; //epoch format
	private String encoding;
	//details
	private String arcname;
	private String arcoffset;
	private String collection;
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
	public String getPrimaryType() {
		return primaryType;
	}
	public void setPrimaryType(String primaryType) {
		this.primaryType = primaryType;
	}
	public String getSubType() {
		return subType;
	}
	public void setSubType(String subType) {
		this.subType = subType;
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

	@Override
	public String toString() {
		return "Item [key=" + key + ", title=" + title + ", source=" + source + ", link=" + link + ", tstamp=" + tstamp
				+ ", contentLength=" + contentLength + ", digest=" + digest + ", primaryType=" + primaryType
				+ ", subType=" + subType + ", screenShotLink=" + screenShotLink + ", itemText=" + itemText + ", date="
				+ date + ", encoding=" + encoding + ", arcname=" + arcname + ", arcoffset=" + arcoffset
				+ ", collection=" + collection + ", idDoc=" + idDoc + ", index=" + index + ", segment="+ segment +"]";
	}



}
