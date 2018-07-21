package com.sdyk.ai.crawler.scheduler.test;

import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.ClickAction;
import one.rewind.io.requester.task.ChromeTask;
import org.junit.Test;

public class ClickActionTest {

	@Test
	public void testClickAction() throws Exception {

		ChromeDriverAgent agent = new ChromeDriverAgent();

		agent.start();

		ChromeTask t = new ChromeTask("https://www.clouderwork.com/jobs");

		for( int i = 0; i< 20; i++ ){

			t.addAction(new ClickAction(
					"#search > div.container > div.search-main > div.search-result > div.page-div > nav > span.page-down",
					3000)
			);
		}

		agent.submit(t);

		Thread.sleep(1000000000);

	}

}
