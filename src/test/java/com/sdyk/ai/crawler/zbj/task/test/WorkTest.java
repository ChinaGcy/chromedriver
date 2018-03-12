package com.sdyk.ai.crawler.zbj.task.test;

import com.sdyk.ai.crawler.zbj.task.Task;
import com.sdyk.ai.crawler.zbj.task.scanTask.WorkScanTask;
import org.junit.Test;
import org.tfelab.io.requester.BasicRequester;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
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


	@Test
	public void testBasicRequester() {
		org.tfelab.io.requester.Task t = null;
		try {
			t = new org.tfelab.io.requester.Task("http://www.baidu.com");
			BasicRequester.getInstance().fetch(t);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

	}
 }
