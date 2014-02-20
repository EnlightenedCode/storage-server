package com.risevision.medialibrary.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONStringer;
import com.google.appengine.labs.repackaged.org.json.JSONWriter;
import com.risevision.medialibrary.server.info.ServiceFailedException;
import com.risevision.medialibrary.server.service.AuthenticationService;

@SuppressWarnings("serial")
public class GetFilesServlet extends HttpServlet {
	protected static final Logger log = Logger.getAnonymousLogger();

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String companyId = request.getParameter("companyId");
		
		AuthenticationService authService = new AuthenticationService();
		
		String jsonString = "";
		if (authService.isAuthorized(companyId)) {
			log.warning("Retrieving Files");

			MediaLibraryService service = new MediaLibraryServiceImpl();
			
			try {
				String bucketName = "risemedialibrary-" + companyId;
				jsonString = service.getBucketItemsString(bucketName);
	
			} catch (ServiceFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			log.warning("Unauthorized");
			
			try {
				JSONWriter stringer = new JSONStringer();
				
				stringer.object();
				stringer.key("status").value("401");
				stringer.endObject();

				jsonString = stringer.toString();		

			} catch (JSONException e) {
				log.severe("Error - " + e.getMessage());

				e.printStackTrace();
			}
		}

		response.setContentType("application/json; charset=UTF-8");
		response.getWriter().println(jsonString);
	}

}
