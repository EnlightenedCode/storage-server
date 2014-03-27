package com.risevision.medialibrary.server.api;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.labs.repackaged.org.json.JSONException;
import com.google.appengine.labs.repackaged.org.json.JSONStringer;
import com.google.appengine.labs.repackaged.org.json.JSONWriter;
import com.risevision.medialibrary.server.info.CompanyInfo;
import com.risevision.medialibrary.server.info.ServiceFailedException;
import com.risevision.medialibrary.server.service.CompanyService;
import com.risevision.medialibrary.server.utils.CacheUtils;

@SuppressWarnings("serial")
public class EnableMediaLibraryServlet extends HttpServlet {
	protected static final Logger log = Logger.getAnonymousLogger();

	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String companyId = request.getParameter("companyId");

		int responseCode;

		try {
			
			CompanyInfo company = CacheUtils.getUserCompany(companyId, null);
			
			company.enableMediaLibrary();
			
			CompanyService.getInstance().saveCompany(company, null);
			
			CacheUtils.updateCompany(company, null);
			
			log.info("Enabled the Media Library for the Company");

			responseCode = ServiceFailedException.OK;
			
		} catch (ServiceFailedException e) {
			log.warning("Enabling the Media Library Failed - Status: " + e.getReason());
			
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
