package com.belwadi.sciencefun.entity;


public class School {

//	public String name, address, city, country, postalCode;
	public Place googleInfo;

//	public School(String name, String address, String city, String code, String country) {
	public School(Place place) {
		super();
//		this.name = name;
//		this.address = address;
//		this.city = city;
//		this.postalCode = code;
//		this.country = country;
		
		this.googleInfo = new Place();
		googleInfo = place;
	}

}
