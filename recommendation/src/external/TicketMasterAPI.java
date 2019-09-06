package external;

import entity.Item;
import entity.Item.ItemBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TicketMasterAPI {
	private static final String URL = "https://app.ticketmaster.com/discovery/v2/events.json";
	private static final String DEFAULT_KEYWORD = "";
	private static final String API_KEY = "YeLCyLaPenIkoR941WmLjXD4GVvgTNlP";
	
	private static final String EMBEDDED = "_embedded";
	private static final String EVENTS = "events";
	private static final String NAME = "name";
	private static final String ID = "id";
	private static final String URL_STR = "url";
	private static final String RATING = "rating";
	private static final String DISTANCE = "distance";
	private static final String VENUES = "venues";
	private static final String ADDRESS = "address";
	private static final String LINE1 = "line1";
	private static final String LINE2 = "line2";
	private static final String LINE3 = "line3";
	private static final String CITY = "city";
	private static final String IMAGES = "images";
	private static final String CLASSIFICATIONS = "classifications";
	private static final String SEGMENT = "segment";
	
	/* This returns a itemlist used in the API*/
	private List<Item> getItemList(JSONArray events) throws JSONException{
		List<Item> itemList = new ArrayList<>();
		for (int i = 0;i < events.length();i++)
		{
			JSONObject event = events.getJSONObject(i);
			ItemBuilder builder = new ItemBuilder();
			
			if (!event.isNull(ID))
			{
				builder.setItemId(event.getString(ID));
			}
			
			if (!event.isNull(NAME)) {
				builder.setName(event.getString(NAME));
			}
			if (!event.isNull(RATING)) {
				builder.setRating(event.getDouble(RATING));
			}
			if (!event.isNull(URL_STR)) {
				builder.setUrl(event.getString(URL_STR));
			}
			if (!event.isNull(DISTANCE)) {
				builder.setDistance(event.getDouble(DISTANCE));
			}
			builder.setAddress(getAddress(event));
			builder.setCategories(getCategories(event));
			builder.setImageUrl(getImageUrl(event));
			
			itemList.add(builder.build());
		}
		
		return itemList;
	}
	/*Return the first non-empty address of venue*/
	private String getAddress(JSONObject event) throws JSONException{
		if (!event.isNull(EMBEDDED)) {
			JSONObject embedded = event.getJSONObject(EMBEDDED);
			if (!embedded.isNull(VENUES)) {
				JSONArray venues = embedded.getJSONArray(VENUES);
				
				for (int i = 0; i < venues.length(); i++)
				{
					JSONObject venue = venues.getJSONObject(i);
					StringBuilder sBuilder = new StringBuilder();
					
					if (!venue.isNull(ADDRESS)) {
						JSONObject address = venue.getJSONObject(ADDRESS);
						if (!address.isNull(LINE1))
						{
							sBuilder.append(address.getString(LINE1));
						}
						
						if (!address.isNull(LINE2))
						{
							sBuilder.append('\n');							
							sBuilder.append(address.getString(LINE2));
						}
						
						if (!address.isNull(LINE3))
						{
							sBuilder.append('\n');							
							sBuilder.append(address.getString(LINE3));
						}
						
					}
					
					if (!venue.isNull(CITY))
					{
						JSONObject city = venue.getJSONObject(CITY);
						if (!city.isNull(NAME)) {
							sBuilder.append('\n');
							sBuilder.append(city.getString(NAME));
						}
					}
					String result = sBuilder.toString();
					if (result.length() > 0)
					{
						return result;
					}
				}
				
				
			}
		}
		return "";
		
	}
	
	/*Return a list of string that represent the category names of an event*/
	private Set<String> getCategories(JSONObject event) throws JSONException{
		Set<String> categories = new HashSet<>();
		if (!event.isNull(CLASSIFICATIONS)) {
			JSONArray classifications = event.getJSONArray(CLASSIFICATIONS);
			for (int i = 0;i < classifications.length();i++) {
				JSONObject classification = classifications.getJSONObject(i);
				if (!classification.isNull(SEGMENT)) {
					JSONObject segment = classification.getJSONObject(SEGMENT);
					if (!segment.isNull(NAME)) {
						categories.add(segment.getString(NAME));
					}
				}
			}

		}
		return categories;
	}
	
	private String getImageUrl(JSONObject event) throws JSONException{
		StringBuilder sb = new StringBuilder();
		if (!event.isNull(IMAGES)) {
			JSONArray images = event.getJSONArray(IMAGES);
			for (int i = 0;i< images.length();i++)
			{
				JSONObject image = images.getJSONObject(i);
				if (!image.isNull(URL_STR))
				{
					return image.getString(URL_STR);
				}
			}
		}
		return "";
	}
	
	
	

	public List<Item> search(double lat, double lon, String keyword)
	{
		if (keyword == null)
		{
			keyword = DEFAULT_KEYWORD;
		}
		try {
			keyword = URLEncoder.encode(keyword,"UTF-8"); //Change spaces to 20%
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		String geoHash = GeoHash.encodeGeohash(lat, lon, 9);
		
		String query = String.format("apikey=%s&geoPoint=%s&keyword=%s&radius=50", API_KEY,geoHash,keyword);
		
		try {
			HttpURLConnection connection = (HttpURLConnection) new URL(URL+"?"+query).openConnection();
			connection.setRequestMethod("GET");
			//Specified the type of the request method
			int responseCode = connection.getResponseCode();
			System.out.println("Sending 'GET' Request to URL:"+URL);
			System.out.println("Response Code: " + responseCode);
			// 1. Send the method if the request is not sent.
			// 2. Get the response code.
			// Response Code = 200 if the response goes well
			
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream())); //Response is an input with regard to client, output with regard to server
			//BufferedReader is used to prevent if the stream is very big
			//connection.getOutputStream(); Request is an output with regard to client, input with regard to server
			StringBuilder response =  new StringBuilder();
			String inputLine;
			while ((inputLine =in.readLine()) != null)
			{
				response.append(inputLine);
			}
			in.close();
			
			JSONObject obj = new JSONObject(response.toString());
			if (!obj.isNull("_embedded"))
			{
				JSONObject embedded = obj.getJSONObject("_embedded");
				JSONArray events = embedded.getJSONArray(EVENTS);
				return getItemList(events);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<Item>();
		
	}
	
	public void queryAPI(double lat, double lon) {
		List<Item> itemList = search(lat, lon, null);
		try {
			for (Item item : itemList) {
				JSONObject jsonObject = item.toJSONObject();
				System.out.println(jsonObject);
			}
		}
				catch (Exception e) {
						e.printStackTrace();
				} 
		}
	
	public static void main(String[] args) {
		TicketMasterAPI tmApi = new TicketMasterAPI();
		// Mountain View, CA
		// tmApi.queryAPI(37.38, -122.08);
		// London, UK
		// tmApi.queryAPI(51.503364, -0.12);
		// Houston, TX
		tmApi.queryAPI(29.682684, -95.295410);
	}

}
