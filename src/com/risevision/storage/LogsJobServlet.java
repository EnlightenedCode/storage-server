package com.risevision.storage;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.risevision.storage.info.MediaItemInfo;
import com.risevision.storage.info.ServiceFailedException;
import com.risevision.storage.queue.QueueTask;
import com.risevision.storage.queue.tasks.ImportFiles;

@SuppressWarnings("serial")
public class LogsJobServlet extends HttpServlet {
	protected static final Logger log = Logger.getAnonymousLogger();

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String companyId = request.getParameter("companyId");
		
//		MediaLibraryService service = new MediaLibraryServiceImpl();
		
		try {
			String jsonString = "Done";
			
			QueueFactory.getDefaultQueue().add(withUrl("/queue")
					.param(QueryParam.TASK, QueueTask.RUN_BQ_JOB)
					.method(Method.GET));
			
//			String dataCounter;
//			for (MediaItemInfo item: service.getBucketItems("risemedialibrary-" + companyId)) {
//				dataCounter += item.getSize();
//			}
			
//			BigQueryImportJob.runStorageJob();
//			dataCounter = ImportFiles.runJob();
//			
//			jsonString = "Storage Logs: \n";
//			jsonString += "Usage Files Read: " + dataCounter + "\n";

//			jsonString += MediaLibraryLogReader.parseBucketLogs(companyId);

			
//			String companyId = request.getParameter("companyId");
//			Companies companies = CacheUtils.get(Companies.class, companyId);
//			
//			for (Company item: companies.companies) {
//				jsonString += item.getId() + "_" + item.name;
//			}

			response.setContentType("application/json; charset=UTF-8");
			response.getWriter().println(jsonString);			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//		} catch (ServiceFailedException e) {
//			e.printStackTrace();
		}
	}

}
