package com.sdyk.ai.crawler.clouder.task.test;

import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.WorkTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import org.junit.Test;

public class WorkTaskTest  {

	@Test
	public void test() throws Exception{

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();

		WorkTask workTask = new WorkTask("https://www.clouderwork.com/project/216");

		agent.submit(workTask);

		Thread.sleep(1000000);
	}
}
