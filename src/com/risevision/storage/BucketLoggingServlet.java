package com.risevision.storage;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.risevision.storage.queue.QueueName;
import com.risevision.storage.queue.QueueTask;

@SuppressWarnings("serial")
public class BucketLoggingServlet extends HttpServlet {
	protected static final Logger log = Logger.getAnonymousLogger();
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
//		try {
			String jsonString = "Done";

			QueueFactory.getQueue(QueueName.STORAGE_LOG_ENABLE).add(withUrl("/queue")
					.param(QueryParam.TASK, QueueTask.RUN_ENABLE_LOGGING_JOB)
					.method(Method.GET));
			
//			String companyId = request.getParameter("companyId");
//			Companies companies = CacheUtils.get(Companies.class, companyId);
//			
//			for (Company item: companies.companies) {
//				jsonString += item.getId() + "_" + item.name;
//			}

			response.setContentType("application/json; charset=UTF-8");
			response.getWriter().println(jsonString);			

//		} catch (ServiceFailedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
}
