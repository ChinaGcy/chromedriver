package com.sdyk.ai.crawler.proginn.task.test;

import com.sdyk.ai.crawler.specific.proginn.task.modelTask.ServiceProviderTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.txt.DateFormatUtil;
import org.junit.Test;

import java.text.ParseException;

public class ServiceProviderTaskTest {

	@Test
	public void test() throws Exception{

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();

		ServiceProviderTask serviceProviderTask = new ServiceProviderTask("https://www.proginn.com/wo/138970");

		agent.submit(serviceProviderTask);

		Thread.sleep(10000000);
	}

	@Test
	public void test1() throws ParseException {

		String s = "2016-06-01";
		System.out.println(DateFormatUtil.parseTime(s));

	}
}
