package com.sdyk.ai.crawler.pro.lahou.task.test;

import com.sdyk.ai.crawler.specific.proLagou.task.modelTask.ServiceProviderTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import org.junit.Test;

public class ServiceProviderTaskTest {

	@Test
	public void test() throws Exception{

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();

		ServiceProviderTask providerTask = new ServiceProviderTask("https://pro.lagou.com/user/1298289106.html");

		agent.submit(providerTask);

		Thread.sleep(1000000);

	}
}
