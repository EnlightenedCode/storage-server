package com.risevision.storage.gcs;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.extensions.appengine.auth.oauth2.AbstractAppEngineAuthorizationCodeServlet;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.google.common.base.Strings;
import com.risevision.storage.QueryParam;
import com.risevision.storage.queue.QueueName;
import com.risevision.storage.queue.QueueTask;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoggingServlet extends AbstractAppEngineAuthorizationCodeServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {

		String lastCompanyId = req.getParameter(QueryParam.JOB_CURSOR);
		
		if (Strings.isNullOrEmpty(lastCompanyId)) {
			QueueFactory.getQueue(QueueName.STORAGE_LOG_ENABLE).add(withUrl("/queue")
					.param(QueryParam.TASK, QueueTask.RUN_ENABLE_LOGGING_JOB)
					.param(QueryParam.USER_ID, getUserId(req))
					.method(Method.GET));
		}
		else {
			QueueFactory.getQueue(QueueName.STORAGE_LOG_ENABLE).add(withUrl("/queue")
					.param(QueryParam.TASK, QueueTask.RUN_ENABLE_LOGGING_JOB)
					.param(QueryParam.JOB_CURSOR, lastCompanyId)
					.param(QueryParam.USER_ID, getUserId(req))
					.method(Method.GET));
		}

		// Send the results as the response
		PrintWriter respWriter = resp.getWriter();
		resp.setStatus(200);
		resp.setContentType("text/html");

		respWriter.println("Done");

	}

	@Override
	protected AuthorizationCodeFlow initializeFlow() throws ServletException, IOException {
		return AuthUtils.initializeFlow();
	}

	@Override
	protected String getRedirectUri(HttpServletRequest req)	throws ServletException, IOException {
		return AuthUtils.getRedirectUri(req);
	}
}
