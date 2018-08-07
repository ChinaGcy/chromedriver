package com.sdyk.ai.crawler.proginn.task.test;

import com.sdyk.ai.crawler.specific.proginn.task.modelTask.TendererTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import org.junit.Test;

public class TendererTaskTest {

	@Test
	public void test() throws  Exception{

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();

		TendererTask tendererTask = new TendererTask("https://www.proginn.com/u/188797");

		agent.submit(tendererTask);

		Thread.sleep(1000000);

	}
}
