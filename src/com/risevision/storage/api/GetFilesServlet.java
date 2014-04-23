package com.risevision.storage.api;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONStringer;
import com.google.appengine.labs.repackaged.org.json.JSONWriter;
import com.risevision.storage.MediaLibraryService;
import com.risevision.storage.QueryParam;
import com.risevision.storage.info.ServiceFailedException;
import com.risevision.storage.security.AccessResource;

@SuppressWarnings("serial")
public class GetFilesServlet extends HttpServlet {
	protected static final Logger log = Logger.getAnonymousLogger();

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String authKey = request.getParameter(QueryParam.AUTH_KEY);
		
		String jsonString = "";
		
		try {
			AccessResource resource = new AccessResource(authKey);
			resource.verify();
			
			log.info("Retrieving Files");

			MediaLibraryService service = MediaLibraryService.getInstance();
		
			String bucketName = MediaLibraryService.getBucketName(resource.getResourceCompanyId());
			
			jsonString = service.getBucketItemsString(bucketName);

		} catch (ServiceFailedException e) {
			log.warning("File retrieval failed - Status: " + e.getReason());
			
			try {
				JSONWriter stringer = new JSONStringer();
				
				stringer.object();
				stringer.key("status").value(e.getReason());
				stringer.endObject();

				jsonString = stringer.toString();		

			} catch (JSONException e1) {
				log.severe("Error - " + e1.getMessage());

				e1.printStackTrace();
			}
		}

		response.setContentType("application/json; charset=UTF-8");
		response.getWriter().println(jsonString);
	}

}
