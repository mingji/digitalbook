package com.belwadi.sciencefun.utils;

import org.apache.http.client.HttpResponseException;

import android.util.Log;

import com.belwadi.sciencefun.entity.PlaceDetails;
import com.belwadi.sciencefun.entity.PlacesAutoComp;
import com.belwadi.sciencefun.entity.PlacesList;
import com.google.api.client.googleapis.GoogleHeaders;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.jackson.JacksonFactory;

@SuppressWarnings("deprecation")
public class GooglePlaces {

	/** Global instance of the HTTP transport. */
	private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	
	// Google API Key
//	private static final String API_KEY = "AIzaSyCRLa4LQZWNQBcjCYcIVYA45i9i8zfClqc"; // place your API key here
	private static final String API_KEY = "AIzaSyCeaszxxg90dmZg4fnKPAEx6bN07GwXuEY"; // place your API key here

	// Google Places search url's
	private static final String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?";
	private static final String PLACES_NEARBY_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
	private static final String PLACES_TEXT_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/textsearch/json?";
	private static final String PLACES_DETAILS_URL = "https://maps.googleapis.com/maps/api/place/details/json?";
	private static final String PLACES_AUTOCOMPLETE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/json?";

	private double _latitude;
	private double _longitude;
	private double _radius;
	
	/**
	 * Searching places
	 * @param latitude - latitude of place
	 * @param longitude - longitude of place
	 * @param radius - radius of searchable area
	 * @param types - type of place to search
	 * @return list of places
	 * */
	public PlacesList search(double latitude, double longitude, double radius, String types, String name)
			throws Exception {

		this._latitude = latitude;
		this._longitude = longitude;
		this._radius = radius;
		
		// temp
//		this._latitude = 12.971599;
//		this._longitude = 77.594563;

		try {

			HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
			HttpRequest request = httpRequestFactory
					.buildGetRequest(new GenericUrl(PLACES_SEARCH_URL));
			request.getUrl().put("key", API_KEY);
			request.getUrl().put("location", _latitude + "," + _longitude);
			request.getUrl().put("radius", _radius); // in meters
			request.getUrl().put("sensor", "false");
			if (name != null)
				request.getUrl().put("name", name);
			if(types != null)
				request.getUrl().put("types", types);
			
			PlacesList list = request.execute().parseAs(PlacesList.class);
			// Check log cat for places response status
			Log.d("Places Status", "" + list.status);
			return list;

		} catch (HttpResponseException e) {
			Log.e("Error:", e.getMessage());
			return null;
		}

	}
	
	/**
	 * Searching places by text
	 * @param latitude - latitude of place
	 * @param longitude - longitude of place
	 * @param types - type of place to search
	 * @return list of places
	 * */
	public PlacesList searchByText(String query, String types, String pageToken)
			throws Exception {

		try {

			HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
			HttpRequest request = httpRequestFactory
					.buildGetRequest(new GenericUrl(PLACES_TEXT_SEARCH_URL));
			
			if (pageToken != null && !pageToken.isEmpty())
				request.getUrl().put("pagetoken", pageToken);

			request.getUrl().put("key", API_KEY);
			
			if (types != null)
				request.getUrl().put("query", query);
			if (types != null)
				request.getUrl().put("types", types);
			
			PlacesList list = request.execute().parseAs(PlacesList.class);
			// Check log cat for places response status
			Log.d("Places Status", "" + list.status);
			return list;

		} catch (HttpResponseException e) {
			Log.e("Error:", e.getMessage());
			return null;
		}

	}
	
	/**
	 * Searching places by location
	 * @param latitude - latitude of place
	 * @param longitude - longitude of place
	 * @param radius - radius of search area
	 * @param types - type of place to search
	 * @return list of places
	 * */
	public PlacesList searchNearBy(double latitude, double longitude, int radius, String types, String pageToken)
			throws Exception {

		this._latitude = latitude;
		this._longitude = longitude;
		
		// temp
//		this._latitude = 12.971599;
//		this._longitude = 77.594563;

		try {

			HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
			HttpRequest request = httpRequestFactory
					.buildGetRequest(new GenericUrl(PLACES_NEARBY_SEARCH_URL));
			
			if (pageToken != null && !pageToken.isEmpty())
				request.getUrl().put("pagetoken", pageToken);

			request.getUrl().put("key", API_KEY);
			request.getUrl().put("location", _latitude + "," + _longitude);
			request.getUrl().put("radius", radius); // in meters

			if(types != null)
				request.getUrl().put("types", types);

			PlacesList list = request.execute().parseAs(PlacesList.class);
			// Check log cat for places response status
			Log.d("Places Status", "" + list.status);
			return list;

		} catch (HttpResponseException e) {
			Log.e("Error:", e.getMessage());
			return null;
		}

	}
	

	/**
	 * Searching single place full details
	 * @param refrence - reference id of place
	 * 				   - which you will get in search api request
	 * */
	public PlaceDetails getPlaceDetails(String reference) throws Exception {
		try {

			HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
			HttpRequest request = httpRequestFactory
					.buildGetRequest(new GenericUrl(PLACES_DETAILS_URL));
			request.getUrl().put("key", API_KEY);
			request.getUrl().put("reference", reference);
			request.getUrl().put("sensor", "false");

			PlaceDetails place = request.execute().parseAs(PlaceDetails.class);
			
			return place;

		} catch (HttpResponseException e) {
			Log.e("Error in Perform Details", e.getMessage());
			throw e;
		}
	}
	
	public PlacesAutoComp getAutoCompletePlaces(double latitude, double longitude, String input, String types, String components)
			throws Exception {
		try {
			
			this._latitude = latitude;
			this._longitude = longitude;
			
			// temp
//			this._latitude = 12.971599;
//			this._longitude = 77.594563;

			HttpRequestFactory httpRequestFactory = createRequestFactory(HTTP_TRANSPORT);
			HttpRequest request = httpRequestFactory
					.buildGetRequest(new GenericUrl(PLACES_AUTOCOMPLETE_URL));
			request.getUrl().put("key", API_KEY);
			if (input != null)
				request.getUrl().put("input", input);
			request.getUrl().put("location", _latitude + "," + _longitude);
			if (types != null)
				request.getUrl().put("types", types);
			if (components != null)
				request.getUrl().put("components", components);

			PlacesAutoComp places = request.execute().parseAs(PlacesAutoComp.class);
			
			return places;

		} catch (HttpResponseException e) {
			Log.e("Error in Perform Details", e.getMessage());
			throw e;
		}
	}

	/**
	 * Creating http request Factory
	 * */
	public static HttpRequestFactory createRequestFactory(
			final HttpTransport transport) {
		return transport.createRequestFactory(new HttpRequestInitializer() {
			public void initialize(HttpRequest request) {
				GoogleHeaders headers = new GoogleHeaders();
				headers.setApplicationName("AndroidHive-Places-Test");
				request.setHeaders(headers);
				JsonHttpParser parser = new JsonHttpParser(new JacksonFactory());
				request.addParser(parser);
			}
		});
	}

}
