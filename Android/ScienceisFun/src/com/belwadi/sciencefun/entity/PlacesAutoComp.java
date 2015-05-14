package com.belwadi.sciencefun.entity;

import java.io.Serializable;
import java.util.List;

import com.google.api.client.util.Key;

/** Implement this class from "Serializable"
* So that you can pass this class Object to another using Intents
* Otherwise you can't pass to another actitivy
* */
public class PlacesAutoComp implements Serializable {

	@Key
	public String status;
	
	@Key
	public List<PrePlace> predictions;

	@Override
	public String toString() {
		if (predictions!=null) {
			return predictions.toString();
		}
		return super.toString();
	}
	
	public static class PrePlace implements Serializable
	{
		@Key
		public String id;
		
		@Key
		public String description;
		
		@Key
		public String reference;
		
		@Key
		public String place_id;		
	}
}
