package org.apache.nutch.searcher;

public class Item {
	
	private String key;
	private String title;
	private String source;
	private String link;
	private String tstamp;
	private String contentLength;
	private String digest;
	private String primaryType;
	private String subType;
	private String screenShotLink;
	private String itemText;
	private String date; //epoch format

	public Item(String key, String title, String source, String link, String tstamp, String contentLength,
			String digest, String primaryType, String subType, String screenShotLink, String itemText, String date) {
		super();
		this.key 			= key;
		this.title 			= title;
		this.source 		= source;
		this.link 			= link;
		this.tstamp 		= tstamp;
		this.contentLength 	= contentLength;
		this.digest 		= digest;
		this.primaryType 	= primaryType;
		this.subType 		= subType;
		this.screenShotLink = screenShotLink;
		this.itemText 		= itemText;
		this.date			= date;
	}
	
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
	
	@Override
	public String toString() {
		return "Item [key=" + key + ", title=" + title + ", source=" + source + ", link=" + link + ", tstamp=" + tstamp
				+ ", contentLength=" + contentLength + ", digest=" + digest + ", primaryType=" + primaryType
				+ ", subType=" + subType + ", screenShotLink=" + screenShotLink + ", textItem=" + itemText + "]";
	}


	
}
