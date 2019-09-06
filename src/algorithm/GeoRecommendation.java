package algorithm;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import scala.collection.JavaConversions.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

import algorithm.scala.Recommendation;
import db.DBConnection;
import db.DBConnectionFactory;
import entity.Item;


// Recommendation based on geo distance and similar categories.
public class GeoRecommendation {

  public List<Item> recommendItems(String userId, double lat, double lon) {
		// Step 1, given a user, fetch all the events (ids) this user has visited.
		DBConnection connection = DBConnectionFactory.getConnection();
		Set<String> favoritedItemIds = connection.getFavoriteItemIds(userId);
		
		// Step 2, given all these events, fetch all categories
		// {music: 5, art: 3, sports: 1}
		Map<String, Integer> allCategories = new HashMap<>();
		for (String favoritedItemId : favoritedItemIds) {
			Set<String> categories = connection.getCategories(favoritedItemId);
			for (String category : categories) {
				allCategories.put(category, allCategories.getOrDefault(category, 0) + 1);
			}
		}

	    List<Item> recommendedItems = Recommendation.getRecommendedItems(allCategories,lat,lon);


		connection.close();



		
		
		return recommendedItems;

  }
}

