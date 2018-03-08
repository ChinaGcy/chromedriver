package com.sdyk.ai.crawler.zbj.task.test;

import com.sdyk.ai.crawler.zbj.task.WorkScanTask;
import org.junit.Test;
import org.tfelab.io.requester.BasicRequester;
import org.tfelab.io.requester.Task;
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

		Queue<com.sdyk.ai.crawler.zbj.task.Task> queue = new LinkedBlockingQueue<>();

		queue.add(WorkScanTask.generateTask("http://shop.zbj.com/16123923/", 1));

		while(!queue.isEmpty()) {

			com.sdyk.ai.crawler.zbj.task.Task t = queue.poll();

			agent.fetch(t);

			for (com.sdyk.ai.crawler.zbj.task.Task task : t.postProc(agent.getDriver())) {
				queue.add(task);
			}
		}
	}

	@Test
	public void testBasicRequester() {
		Task t = null;
		try {
			t = new Task("http://www.baidu.com");
			BasicRequester.getInstance().fetch(t);
			t.setDelete();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

	}
 }
