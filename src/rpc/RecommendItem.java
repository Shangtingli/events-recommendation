package rpc;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONArray;
import org.json.JSONObject;


import algorithm.GeoRecommendation;
import entity.Item;


@WebServlet("/recommendation")
public class RecommendItem extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public RecommendItem() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    	HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}

  		double lat = Double.parseDouble(request.getParameter("lat"));
  		double lon = Double.parseDouble(request.getParameter("lon"));
  		String userId = request.getParameter("user_id");

  		GeoRecommendation recommendation = new GeoRecommendation();
  		try {
  			List<Item> items = recommendation.recommendItems(userId, lat, lon);
  			JSONArray array = new JSONArray();
  			for (Item item : items) {
  				JSONObject obj = item.toJSONObject();
  				array.put(obj);
  			}
  			RpcHelper.writeJsonArray(response, array);
  		} catch (Exception e) {
  			e.printStackTrace();
  		}	
   }



	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(false);
		if (session == null) {
			response.setStatus(403);
			return;
		}

		doGet(request, response);
	}

}
