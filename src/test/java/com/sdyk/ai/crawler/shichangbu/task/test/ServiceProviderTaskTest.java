package com.sdyk.ai.crawler.shichangbu.task.test;

import com.sdyk.ai.crawler.specific.shichangbu.task.modelTask.ServiceProviderTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.task.ChromeTask;
import org.junit.Test;

public class ServiceProviderTaskTest {

	@Test
	public void test() throws Exception {

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();

		ServiceProviderTask serviceProviderTask = new ServiceProviderTask("http://www.shichangbu.com/agency-2147.html");
		agent.submit(serviceProviderTask);

		Thread.sleep(10000000);

	}
}
