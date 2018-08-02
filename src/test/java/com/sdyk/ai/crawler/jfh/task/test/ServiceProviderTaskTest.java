package com.sdyk.ai.crawler.jfh.task.test;

import com.sdyk.ai.crawler.specific.jfh.task.modelTask.ServiceProviderTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import org.junit.Test;

public class ServiceProviderTaskTest {

	@Test
	public void test() throws Exception{

		ChromeDriverAgent agent = new ChromeDriverAgent();

		agent.start();

		ServiceProviderTask providerTask = new ServiceProviderTask("http://shop.jfh.com/1008/bu/");

		agent.submit(providerTask);

		Thread.sleep(1000000);

	}
}
