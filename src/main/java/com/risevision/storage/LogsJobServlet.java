package com.risevision.storage;


import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.risevision.storage.queue.QueueServlet;

@SuppressWarnings("serial")
public class LogsJobServlet extends HttpServlet {
	protected static final Logger log = Logger.getAnonymousLogger();

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		try {
			String jsonString = "Done";
			
			QueueServlet.enqueueJob(request.getParameter(QueryParam.JOB_TYPE));

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
