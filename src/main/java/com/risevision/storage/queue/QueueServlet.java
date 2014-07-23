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
import org.apache.commons.lang3.math.NumberUtils;
import com.risevision.storage.QueryParam;
import com.risevision.storage.Utils;
import com.risevision.storage.queue.tasks.BQUtils;
import com.risevision.storage.queue.tasks.EnableLogging;
import com.risevision.storage.queue.tasks.ImportFiles;
import com.google.common.base.Strings;


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
			if (task.equals(QueueTask.START_BQ_JOB)) {
				ImportFiles.runJob();
			}
			else if (task.equals(QueueTask.RUN_IMPORT_JOB)) {

				int jobType = NumberUtils.toInt(req.getParameter(QueryParam.JOB_TYPE), 0);
				
				ImportFiles.runJob(jobType);
				
			} else if (task.equals(QueueTask.RUN_ENABLE_LOGGING_JOB)) {
				
				String cursor = req.getParameter(QueryParam.JOB_CURSOR);
				String userId = req.getParameter(QueryParam.USER_ID);
				EnableLogging.runJob(userId, cursor);

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
				
				if (Strings.isNullOrEmpty(jobId)) {
					
					String jobFiles = req.getParameter(QueryParam.JOB_FILES);
					int jobType = NumberUtils.toInt(req.getParameter(QueryParam.JOB_TYPE), 0);
					
					ImportFiles.postProcess(jobFiles, jobType);
					
				} else {
					
					Job job = BQUtils.getJob(jobId);
					
					
					if (job.getStatus().getErrorResult() != null) {
						// throw error by logging it, re-start initial import job
						log.severe("Import Error - " + job.getStatus().getErrorResult().getMessage());
						
						enqueueJob(req.getParameter(QueryParam.JOB_TYPE));

					} 
					else if (job.getStatus().getState().equals("DONE")) {

						String jobFiles = req.getParameter(QueryParam.JOB_FILES);
						int jobType = NumberUtils.toInt(req.getParameter(QueryParam.JOB_TYPE), 0);
						
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
				
			} else if (task.equals(QueueTask.CHECK_MOVE_JOB)) {
				
				String jobId = req.getParameter(QueryParam.JOB_ID);

				Job job = BQUtils.getJob(jobId);
				
				if (job.getStatus().getErrorResult() != null) {
					int jobType = NumberUtils.toInt(req.getParameter(QueryParam.JOB_TYPE), 0);

					// throw error by logging it, if needed, re-start post-processing Job
					log.severe("Move Error - " + job.getStatus().getErrorResult().getMessage());
					
					ImportFiles.postProcess("", jobType);

				} else if (job.getStatus().getState().equals("DONE")) {

					// run next job
					enqueueJob(req.getParameter(QueryParam.JOB_TYPE));
					
					log.info("Job done, Scheduled next job. Job Type:" + req.getParameter(QueryParam.JOB_TYPE));

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
	
	public static void enqueueJob(String jobType) {
		QueueFactory.getQueue(QueueName.STORAGE_LOG_TRANSFER).add(withUrl("/queue")
				.param(QueryParam.TASK, QueueTask.RUN_IMPORT_JOB)
				.param(QueryParam.JOB_TYPE, jobType)
				.countdownMillis(1000 * 30)
				.method(Method.GET));
	}
	
	public static void enqueueCheckImportJob(String jobId, String jobType, String filesString) {
		QueueFactory.getQueue(QueueName.STORAGE_LOG_TRANSFER).add(withUrl("/queue")
				.param(QueryParam.TASK, QueueTask.CHECK_IMPORT_JOB)
				.param(QueryParam.JOB_ID, jobId)
				.param(QueryParam.JOB_TYPE, jobType)
				.param(QueryParam.JOB_FILES, filesString)
				.countdownMillis(1000 * 30)
				.method(Method.POST));
	}
	
	public static void enqueueCheckMoveJob(String jobId, String jobType) {
		QueueFactory.getQueue(QueueName.STORAGE_LOG_TRANSFER).add(withUrl("/queue")
				.param(QueryParam.TASK, QueueTask.CHECK_MOVE_JOB)
				.param(QueryParam.JOB_ID, jobId)
				.param(QueryParam.JOB_TYPE, jobType)
//				.param(QueryParam.JOB_FILES, filesString)
				.countdownMillis(1000 * 10)
				.method(Method.POST));
	}
	
//	static public void Enqueue() {
//		
//		long now = System.currentTimeMillis();
//		long logRangeEndMs = round(now, INTERVAL_MSEC);
//		long logRangeStartMs = logRangeEndMs - INTERVAL_MSEC;
//		
//		Enqueue(logRangeStartMs, logRangeEndMs);
//	}
	
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
