package com.sdyk.ai.crawler.zbj.task.test;

import com.sdyk.ai.crawler.zbj.requester.ChromeDriverLoginWrapper;
import com.sdyk.ai.crawler.zbj.task.Task;
import com.sdyk.ai.crawler.zbj.task.modelTask.TendererTask;
import org.junit.Test;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.PriorityBlockingQueue;

public class TendererTaskTest {

	/**
	 *
	 * @throws Exception
	 */
	@Test
	public  void TendererTaskTest() throws Exception {

		PriorityBlockingQueue<Task> queue = new PriorityBlockingQueue<>();
		Set<String> set = new HashSet<>();

		ChromeDriverAgent agent = new ChromeDriverLoginWrapper("zbj.com").login(); //future
		Thread.sleep(1000);

		TendererTask tendererTask = new TendererTask("http://home.zbj.com/15087337");

		queue.add(tendererTask);

		while (!queue.isEmpty()) {
			Task t = queue.poll();

			System.err.println(t.getUrl());
			if (!set.contains(t.getUrl())) {
				set.add(t.getUrl());
				agent.fetch(t);
				for (Task tt : t.postProc(agent.getDriver())) {
					queue.add(tt);
				}
			}

		}
		agent.close();
	}
}
