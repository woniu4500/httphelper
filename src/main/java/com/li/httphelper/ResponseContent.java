package com.li.httphelper;

import java.io.UnsupportedEncodingException;

import org.apache.http.client.CookieStore;

public class ResponseContent {
	private String encoding;

	private byte[] contentBytes;

	private int statusCode;

	private String contentType;

	private String contentTypeString;
	
	private CookieStore cookieStore;
	
	private String cookieString;

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getContentType() {
		return this.contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getContentTypeString() {
		return this.contentTypeString;
	}

	public void setContentTypeString(String contenttypeString) {
		this.contentTypeString = contenttypeString;
	}

	public String getContent() throws UnsupportedEncodingException {
		return this.getContent(this.encoding);
	}

	@SuppressWarnings("hiding")
	public String getContent(String encoding)
			throws UnsupportedEncodingException {
		if (encoding == null) {
			return new String(contentBytes);
//			return new String(contentBytes);
		}
		return new String(contentBytes, encoding);
//		return new String(contentBytes, encoding);
	}

	public String getUTFContent() throws UnsupportedEncodingException {
		return this.getContent("UTF-8");
	}

	public byte[] getContentBytes() {
		return contentBytes;
	}

	public void setContentBytes(byte[] contentBytes) {
		this.contentBytes = contentBytes;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public CookieStore getCookieStore() {
		return cookieStore;
	}

	public void setCookieStore(CookieStore cookieStore) {
		this.cookieStore = cookieStore;
	}

	public String getCookieString() {
		return cookieString;
	}

	public void setCookieString(String cookieString) {
		this.cookieString = cookieString;
	}

}