package com.risevision.medialibrary.server;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.risevision.medialibrary.server.info.ServiceFailedException;

@SuppressWarnings("serial")
public class GetFilesServlet extends HttpServlet {
	protected static final Logger log = Logger.getAnonymousLogger();

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String companyId = request.getParameter("companyId");
		
		MediaLibraryService service = new MediaLibraryServiceImpl();
		
		try {
			String bucketName = "risemedialibrary-" + companyId;
			String jsonString = service.getBucketItemsString(bucketName);

			response.setContentType("application/json; charset=UTF-8");
			response.getWriter().println(jsonString);			

		} catch (ServiceFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
