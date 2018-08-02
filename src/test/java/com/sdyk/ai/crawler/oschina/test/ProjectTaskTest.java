package com.sdyk.ai.crawler.oschina.test;

import com.sdyk.ai.crawler.specific.oschina.task.modelTask.ProjectTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import org.junit.Test;

public class ProjectTaskTest {

	@Test
	public void test() throws Exception{

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();

		ProjectTask projectTask = new ProjectTask("https://zb.oschina.net/reward/detail.html?id=17431");
		agent.submit(projectTask);

		Thread.sleep(1000000000);
	}

}
