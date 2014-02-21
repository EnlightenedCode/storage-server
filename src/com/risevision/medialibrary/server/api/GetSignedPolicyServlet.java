package com.risevision.medialibrary.server.api;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.risevision.medialibrary.server.MediaLibraryService;
import com.risevision.medialibrary.server.MediaLibraryServiceImpl;

@SuppressWarnings("serial")
public class GetSignedPolicyServlet extends HttpServlet {
	protected static final Logger log = Logger.getAnonymousLogger();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String policyBase64 = request.getParameter("policyBase64");
		
		MediaLibraryService service = new MediaLibraryServiceImpl();
		
		String signedPolicy = service.getSignedPolicy(policyBase64, getServletContext());
		
		response.setContentType("text/plain; charset=UTF-8");
		response.getWriter().println(signedPolicy);			

	}

}
