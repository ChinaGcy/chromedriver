package com.sdyk.ai.crawler.pro.lahou.task.test;

import com.sdyk.ai.crawler.specific.proLagou.task.modelTask.ProjectTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import org.junit.Test;

public class ProjectTaskTest {

	@Test
	public void test() throws Exception{

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();

		ProjectTask projectTask = new ProjectTask("https://pro.lagou.com/project/7742.html");
		agent.submit(projectTask);

		Thread.sleep(1000000);

	}
}
