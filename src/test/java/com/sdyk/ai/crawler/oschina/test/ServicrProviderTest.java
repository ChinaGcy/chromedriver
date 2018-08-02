package com.sdyk.ai.crawler.oschina.test;

import com.sdyk.ai.crawler.specific.oschina.task.modelTask.ServiceProviderTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import org.junit.Test;

public class ServicrProviderTest {

	@Test
	public void test() throws Exception{

		ChromeDriverAgent agent = new ChromeDriverAgent();

		agent.start();

		ServiceProviderTask serviceProviderTask = new ServiceProviderTask("https://zb.oschina.net/profile/index.html?u=24530&t=d&work4");

		agent.submit(serviceProviderTask);

		Thread.sleep(1000000000);

	}

}
