package com.risevision.medialibrary.server.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.risevision.common.client.utils.RiseUtils;

@SuppressWarnings("serial")
public class UploadCompleteServlet extends HttpServlet {
	protected static final Logger log = Logger.getAnonymousLogger();
	private static final String OUTPUT_PARAM = "%key%";
	private static final String RESPONSE_HTML = "" +
			"<html>" +
			"<head>" +
			"<script language=\"javascript\">" +
			"	var d = window.opener || window.parent;" +
			"	if (d) {" +
			"		d.parent._uploadComplete('" + OUTPUT_PARAM + "');" +
			"	}" +
			"</script>" +
			"</head>" +
			"<body></body>" +
			"</html>";
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String key = request.getParameter("key");
		key = RiseUtils.strIsNullOrEmpty(key) ? "" : key;
		String bucket = request.getParameter("bucket");

	    PrintWriter out = response.getWriter();
	    out.println(RESPONSE_HTML.replace(OUTPUT_PARAM, key));	
	    
	    log.info("File - " + key + " uploaded successfully in the bucket - " + bucket);
	}

}
