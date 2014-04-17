package com.risevision.storage.queue;

import static com.google.appengine.api.taskqueue.TaskOptions.Builder.withUrl;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.api.services.bigquery.model.Job;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions.Method;
import com.risevision.common.client.utils.RiseUtils;
import com.risevision.storage.QueryParam;
import com.risevision.storage.Utils;
import com.risevision.storage.queue.tasks.BQUtils;
import com.risevision.storage.queue.tasks.EnableLogging;
import com.risevision.storage.queue.tasks.ImportFiles;


@SuppressWarnings("serial")
public class QueueServlet extends HttpServlet {

	private Logger log = Logger.getAnonymousLogger();

	public void doGet(HttpServletRequest req, HttpServletResponse resp)	throws IOException {

		String task = req.getParameter(QueryParam.TASK);

		if (task == null || task.isEmpty()) {
			log.severe("Task is not supplied, exiting.");
			return;
		}

		log.info("task: " + task);
		
//		Environment env = ApiProxy.getCurrentEnvironment();
//		String appId = env.getAppId();
//		log.info("AppID: " + appId);
		
		try {
			
//			if (task.equals(QueueTask.ENQUEUE)) {
//				
//				String taskName = req.getParameter(QueryParam.TASK_NAME);
//				
//				QueueFactory.getDefaultQueue().add(withUrl("/queue")
//						.param(QueryParam.TASK, taskName)
//						.method(Method.GET));
//				
//			} else 
			if (task.equals(QueueTask.RUN_BQ_JOB)) {
				ImportFiles.runJob();
//			}
//			else if (task.equals(QueueTask.CHECK_BQ_JOB)) {
//				
//				String id = req.getParameter(QueryParam.ID);
//				
//				if (id == null) {
//					
////					Enqueue();
//			
//				} else {
//					
////					ImportFiles.runJob();
//					Job job = BQUtils.checkResponse(id);
//					
//					if (job.getStatus().getState().equals("DONE")) {
//						// delete the files
//					}
//				}
//				
			} else if (task.equals(QueueTask.RUN_ENABLE_LOGGING_JOB)) {
				
//				String entityKind = req.getParameter(QueryParam.KIND);
//				String offsetStr = req.getParameter(QueryParam.OFFSET);
				
//				if (offsetStr == null) {
//					
//					EnableLogging.Enqueue(entityKind, 0);
//					
//				} else {
				
//					Integer offset = Integer.parseInt(offsetStr);
					String cursor = req.getParameter(QueryParam.JOB_CURSOR);
					EnableLogging.runJob(cursor);
//				}

			} else {

				log.warning("Task " + task + " is not recognized, exiting.");
				return;
			}

		} catch (Exception e) {
			log.severe("Error: " + e.toString());
			Utils.logStackTrace(e, log);

			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		String task = req.getParameter(QueryParam.TASK);

		if (task == null || task.isEmpty()) {
			log.severe("Task is not supplied, exiting.");
			return;
		}

		log.info("task: " + task);
		
		try {
			if (task.equals(QueueTask.CHECK_IMPORT_JOB)) {
				
				String jobId = req.getParameter(QueryParam.JOB_ID);
				
				if (jobId == null) {
					
//					Enqueue();
			
				} else {
					
//					ImportFiles.runJob();
					Job job = BQUtils.checkResponse(jobId);
					
					
					if (job.getStatus().getErrorResult() != null) {
						// throw error by logging it, eliminate job from queue
						log.severe("Import Error - " + job.getStatus().getErrorResult().getMessage());
					} 
					else if (job.getStatus().getState().equals("DONE")) {

						String jobFiles = req.getParameter(QueryParam.JOB_FILES);
						int jobType = RiseUtils.strToInt(req.getParameter(QueryParam.JOB_TYPE), 0);
						
						ImportFiles.postProcess(jobFiles, jobType);

					}
					else {
//						QueueFactory.getDefaultQueue().add(withUrl("/queue")
//								.param(QueryParam.TASK, QueueTask.CHECK_BQ_JOB)
//								.param(QueryParam.ID, jobId)
//								.countdownMillis(1000 * 10)
//								.param("payload", payload)
//								.method(Method.POST));
						
						resp.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
					}
				}
				
			} else if (task.equals(QueueTask.CHECK_STORAGE_MOVE_JOB) || task.equals(QueueTask.CHECK_USAGE_MOVE_JOB)) {
				
				String jobId = req.getParameter(QueryParam.JOB_ID);
				
				Job job = BQUtils.checkResponse(jobId);
				
				if (job.getStatus().getErrorResult() != null) {
					// throw error by logging it, eliminate job from queue
					log.severe("Move Error - " + job.getStatus().getErrorResult().getMessage());

				} else if (job.getStatus().getState().equals("DONE")) {

					// run next job
					QueueFactory.getQueue(QueueName.STORAGE_LOG_TRANSFER).add(withUrl("/queue")
							.param(QueryParam.TASK, QueueTask.RUN_BQ_JOB)
							.method(Method.GET));

				}
				else {
//						QueueFactory.getDefaultQueue().add(withUrl("/queue")
//								.param(QueryParam.TASK, QueueTask.CHECK_BQ_JOB)
//								.param(QueryParam.ID, jobId)
//								.countdownMillis(1000 * 10)
//								.param("payload", payload)
//								.method(Method.POST));
					
					resp.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
				}
				
			} else {
				log.warning("Task " + task + " is not recognized, exiting.");
				return;
			}
			
		} catch (Exception e) {
			log.severe("Error: " + e.toString());
			Utils.logStackTrace(e, log);

			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
		
	}
	
	static public void Enqueue() {
		
//		long now = System.currentTimeMillis();
//		long logRangeEndMs = round(now, INTERVAL_MSEC);
//		long logRangeStartMs = logRangeEndMs - INTERVAL_MSEC;
//		
//		Enqueue(logRangeStartMs, logRangeEndMs);
	}
	
//	static public void Enqueue(Long logRangeStartMs, Long logRangeEndMs) {
//		
//		String taskName = TASK_NAME_PREFIX + "_" +Long.toString(logRangeStartMs) + "_" + Long.toString(logRangeEndMs);
//		
//		Queue queue = QueueFactory.getQueue(QueueName.LOG);
//		
//		TaskOptions taskOptions = TaskOptions.Builder.withUrl("/queue")
//				.param(QueryParam.TASK, QueueTask.RECORD_LOGS)
//				.param(QueryParam.START, Long.toString(logRangeStartMs))
//				.param(QueryParam.END, Long.toString(logRangeEndMs))
//				.taskName(taskName)
//				.method(Method.GET);
//		
//		try {
//			queue.add(taskOptions);
//			log.info("Task " + taskName + " enqueued");
//		}
//		catch (TaskAlreadyExistsException te) {
//			// we've already enqueued a task for this window, so don't worry about it
//			log.warning("Task " + taskName + " NOT enqueued");
//		}
//	}

}
