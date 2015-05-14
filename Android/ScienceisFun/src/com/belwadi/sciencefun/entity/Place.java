package com.belwadi.sciencefun.entity;

import java.io.Serializable;
import java.util.List;

import com.google.api.client.util.Key;

/** Implement this class from "Serializable"
* So that you can pass this class Object to another using Intents
* Otherwise you can't pass to another actitivy
* */
public class Place implements Serializable {

	@Key
	public String id;
	
	@Key
	public String name;
	
	@Key
	public String reference;
	
	@Key
	public String icon;
	
	@Key
	public String vicinity;
	
	@Key
	public Geometry geometry;
	
	@Key
	public String formatted_address;
	
	@Key
	public String formatted_phone_number;
	
	@Key
	public List<AddressComponent> address_components;

	@Override
	public String toString() {
		return name + " - " + id + " - " + reference;
	}
	
	public static class Geometry implements Serializable
	{
		@Key
		public Location location;
	}
	
	public static class Location implements Serializable
	{
		@Key
		public double lat;
		
		@Key
		public double lng;
	}
	
	public static class AddressComponent implements Serializable
	{
		@Key
		public String long_name;
		
		@Key
		public String short_name;
		
		@Key
		public List<String> types;
	}
	
}
