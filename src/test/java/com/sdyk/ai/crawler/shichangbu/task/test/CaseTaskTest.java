package com.sdyk.ai.crawler.shichangbu.task.test;

import com.sdyk.ai.crawler.specific.shichangbu.task.modelTask.CaseTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import org.junit.Test;

public class CaseTaskTest {

	@Test
	public void test() throws Exception{

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();

		CaseTask caseTask = new CaseTask("http://www.shichangbu.com/portal.php?mod=product&op=view&id=7");
		agent.submit(caseTask);

		Thread.sleep(1000000);

	}
}
