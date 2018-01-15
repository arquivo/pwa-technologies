package org.apache.nutch.searcher;

public class ItemLucene {
	
	private int id;
	private int index;
	private String arcname;
	private String arcoffset;
	private String segment;
	private String digest;
	private String tstamp;
	private String date;
	private String encoding;
	private String collection;
	private String contentLength;
	private String primaryType;
	private String subType;
	
	public ItemLucene( ) {  }
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
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
	public String getSegment() {
		return segment;
	}
	public void setSegment(String segment) {
		this.segment = segment;
	}
	public String getDigest() {
		return digest;
	}
	public void setDigest(String digest) {
		this.digest = digest;
	}
	public String getTstamp() {
		return tstamp;
	}
	public void setTstamp(String tstamp) {
		this.tstamp = tstamp;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getEncoding() {
		return encoding;
	}
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	public String getCollection() {
		return collection;
	}
	public void setCollection(String collection) {
		this.collection = collection;
	}
	public String getContentLength() {
		return contentLength;
	}
	public void setContentLength(String contentLength) {
		this.contentLength = contentLength;
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
	@Override
	public String toString() {
		return "ItemLucene [id=" + id + ", index=" + index + ", arcname=" + arcname + ", arcoffset=" + arcoffset
				+ ", segment=" + segment + ", digest=" + digest + ", tstamp=" + tstamp + ", date=" + date
				+ ", encoding=" + encoding + ", collection=" + collection + ", contentLength=" + contentLength
				+ ", primaryType=" + primaryType + ", subType=" + subType + "]";
	}
	
	
}
