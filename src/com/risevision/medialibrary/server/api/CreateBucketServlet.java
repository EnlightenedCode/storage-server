package com.risevision.medialibrary.server.api;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONStringer;
import com.google.appengine.labs.repackaged.org.json.JSONWriter;
import com.risevision.medialibrary.server.MediaLibraryService;
import com.risevision.medialibrary.server.MediaLibraryServiceImpl;
import com.risevision.medialibrary.server.info.ServiceFailedException;

@SuppressWarnings("serial")
public class CreateBucketServlet extends HttpServlet {
	protected static final Logger log = Logger.getAnonymousLogger();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String bucketName = request.getParameter("bucketName");

		int responseCode;

		try {
			
			MediaLibraryService service = new MediaLibraryServiceImpl();
			
			service.createBucket(bucketName);
			
			log.info("Bucket Created");

			responseCode = ServiceFailedException.OK;
			
		} catch (ServiceFailedException e) {
			log.warning("Bucket Creation Failed - Status: " + e.getReason());
			
			responseCode = e.getReason();
		}
		
		String jsonString = "";
		try {
			JSONWriter stringer = new JSONStringer();
			
			stringer.object();
			stringer.key("status").value(responseCode);
			stringer.endObject();

			jsonString = stringer.toString();		

		} catch (JSONException e) {
			log.severe("Error - " + e.getMessage());

			e.printStackTrace();
		}

		response.setContentType("application/json; charset=UTF-8");
		response.getWriter().println(jsonString);
		
	}

}
