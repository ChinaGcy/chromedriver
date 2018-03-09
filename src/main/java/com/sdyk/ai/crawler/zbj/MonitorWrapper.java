package com.sdyk.ai.crawler.zbj;

import com.sdyk.ai.crawler.zbj.task.MonitorProjectTask;
import com.sdyk.ai.crawler.zbj.task.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * 监控
 */
public class MonitorWrapper extends Thread{

	public static final Logger logger = LogManager.getLogger(MonitorWrapper.class.getName());

	public static PriorityBlockingQueue<Task> taskQueueMontor = new PriorityBlockingQueue<>();

	public void run() {

		ChromeDriverAgent agent = null;
		try {
			agent = new ChromeDriverLoginWrapper("zbj.com").login();
		} catch (Exception e) {
			e.printStackTrace();
		}

		while (true) {
			try {

				Task task = new MonitorProjectTask("http://task.zbj.com/s5.html?o=7");

				agent.fetch(task);

				for (Task task_p : task.postProc(agent.getDriver())) {
					taskQueueMontor.add(task);
				}
				Thread.sleep(60 * 1000);
			} catch (MalformedURLException | URISyntaxException e) {
				logger.error("Monitor add ProjectTask error",e);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}



	}

}
