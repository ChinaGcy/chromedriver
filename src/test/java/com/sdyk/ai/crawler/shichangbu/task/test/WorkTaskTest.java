package com.sdyk.ai.crawler.shichangbu.task.test;

import com.sdyk.ai.crawler.specific.shichangbu.task.modelTask.WorkTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import org.junit.Test;

public class WorkTaskTest {

	@Test
	public void test() throws Exception{

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();
		// http://www.shichangbu.com/portal.php?mod=case&op=view&id=2085 版本1
		// http://www.shichangbu.com/portal.php?mod=case&op=view&id=103  版本2
		WorkTask workTask = new WorkTask("http://www.shichangbu.com/portal.php?mod=case&op=view&id=103");

		agent.submit(workTask);

		Thread.sleep(1000000);

	}
}
