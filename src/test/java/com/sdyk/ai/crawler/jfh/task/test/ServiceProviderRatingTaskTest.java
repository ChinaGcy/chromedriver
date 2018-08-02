package com.sdyk.ai.crawler.jfh.task.test;

import com.sdyk.ai.crawler.specific.jfh.task.modelTask.ServiceProviderRatingTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import org.junit.Test;

public class ServiceProviderRatingTaskTest {

	@Test
	public void test() throws Exception{

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();

		ServiceProviderRatingTask ratingTask = new ServiceProviderRatingTask("http://shop.jfh.com/1210/feedback/");

		agent.submit(ratingTask);

		Thread.sleep(1000000);

	}
}
