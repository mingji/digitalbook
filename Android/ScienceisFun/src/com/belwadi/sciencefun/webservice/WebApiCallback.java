package com.belwadi.sciencefun.webservice;

public interface WebApiCallback {

	void onPreProcessing(WebApiInstance.Type type, Object parameter);
	
	void onResultProcessing(WebApiInstance.Type type, Object parameter, Object result);
}
