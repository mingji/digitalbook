package com.belwadi.sciencefun.webservice;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

public class HttpHeaders {
	private List<NameValuePair> params = new ArrayList<NameValuePair>();

	public List<NameValuePair> getHeaders() {
		return params;
	}

	public void addHeader(String field, String value) {
		params.add(new BasicNameValuePair(field, value));
	}
}
