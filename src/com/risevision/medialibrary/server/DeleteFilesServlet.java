package com.risevision.medialibrary.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.labs.repackaged.org.json.JSONArray;
import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.risevision.medialibrary.server.info.ServiceFailedException;

@SuppressWarnings("serial")
public class DeleteFilesServlet extends HttpServlet {
	protected static final Logger log = Logger.getAnonymousLogger();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		String companyId = request.getParameter("companyId");
		String bucketName = "risemedialibrary-" + companyId;

		MediaLibraryService service = new MediaLibraryServiceImpl();
		
		String jsonString = "";
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
		    	jsonString += line;
		} catch (Exception e) { /*report an error*/ }
		
		ArrayList<String> itemNames = new ArrayList<String>();     
		JSONArray jsonArray = null;
		try {
			jsonArray = new JSONArray(jsonString);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		if (jsonArray != null) { 
			for (int i = 0; i < jsonArray.length(); i++){ 
				try {
					itemNames.add(jsonArray.get(i).toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} 
		}

		try {
			service.deleteMediaItems(bucketName, itemNames);
		
			jsonString = service.getBucketItemsString(bucketName);

			response.setContentType("application/json; charset=UTF-8");
			response.getWriter().println(jsonString);			

		} catch (ServiceFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
