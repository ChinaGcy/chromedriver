package com.sdyk.ai.crawler.zbj.task.test;

import com.sdyk.ai.crawler.zbj.ChromeDriverWithLogin;
import com.sdyk.ai.crawler.zbj.task.ProjectScanTask;
import com.sdyk.ai.crawler.zbj.task.Task;
import com.sdyk.ai.crawler.zbj.util.StatManager;
import org.junit.Test;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;

import java.util.LinkedList;
import java.util.Queue;

public class ProjectScanTaskTest {

	@Test
	public void testProjectScanTest() {

		StatManager statManager = StatManager.getInstance();

		try {
			ChromeDriverAgent agent = (new ChromeDriverWithLogin("zbj.com")).login();
			Queue<Task> taskQueue = new LinkedList<>();
			taskQueue.add(ProjectScanTask.generateTask("t-dhsjzbj", 1, null));

			while (!taskQueue.isEmpty()) {
				Task t = taskQueue.poll();
				if (t != null) {
					try {
						statManager.count();
						agent.fetch(t);
						for (Task t_ : t.postProc(agent.getDriver())) {
							taskQueue.add(t_);
							/*agent.fetch(t_);*/
						}

					} catch (Exception e) {
						e.printStackTrace();
						taskQueue.add(t);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
