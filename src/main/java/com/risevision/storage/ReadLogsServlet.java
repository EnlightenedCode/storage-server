package com.risevision.storage;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.risevision.storage.info.MediaItemInfo;
import com.risevision.storage.info.ServiceFailedException;

@SuppressWarnings("serial")
public class ReadLogsServlet extends HttpServlet {
	protected static final Logger log = Logger.getAnonymousLogger();

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String companyId = request.getParameter("companyId");
		
		MediaLibraryService service = new MediaLibraryServiceImpl();
		
		try {
			String jsonString = "";
			
			long dataCounter = 0;

                        List<MediaItemInfo> items = service.getBucketItems("risemedialibrary-" + companyId);
			for (MediaItemInfo item: items) {
				dataCounter += item.getSize();
			}
			
			jsonString = "Storage Logs: \n";
			jsonString += "Data Counter:" + dataCounter + "\n";

			jsonString += MediaLibraryLogReader.parseBucketLogs(companyId);

			
//			String companyId = request.getParameter("companyId");
//			Companies companies = CacheUtils.get(Companies.class, companyId);
//			
//			for (Company item: companies.companies) {
//				jsonString += item.getId() + "_" + item.name;
//			}

			response.setContentType("application/json; charset=UTF-8");
			response.getWriter().println(jsonString);			

		} catch (ServiceFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
