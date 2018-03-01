package com.sdyk.ai.crawler.zbj.task.test;

import com.sdyk.ai.crawler.zbj.task.Task;
import com.sdyk.ai.crawler.zbj.task.WorkScanTask;
import org.junit.Test;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class WorkTest {

	/**
	 *
	 * @throws Exception
	 */
	@Test
	public void workTaskTest() throws Exception {

		ChromeDriverAgent agent = new ChromeDriverAgent();

		Queue<Task> queue = new LinkedBlockingQueue<>();

		queue.add(WorkScanTask.generateTask("http://shop.zbj.com/16123923/", 1));

		while(!queue.isEmpty()) {

			Task t = queue.poll();

			agent.fetch(t);

			for (Task task : t.postProc(agent.getDriver())) {
				queue.add(task);
			}

		}
	}
 }
