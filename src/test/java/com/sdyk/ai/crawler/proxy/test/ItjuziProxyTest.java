package com.sdyk.ai.crawler.proxy.test;

import com.sdyk.ai.crawler.proxy.ProxyManager;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.proxy.Proxy;
import one.rewind.io.requester.task.ChromeTask;
import org.junit.Test;
import org.openqa.selenium.chrome.ChromeDriver;
import sun.management.Agent;

public class ItjuziProxyTest {

	@Test
	public void test() throws Exception {

		Proxy proxy = ProxyManager.getInstance().getProxyById("53");

		ChromeDriverAgent agent = new ChromeDriverAgent(proxy);

		agent.start();

		ChromeTask task = new ChromeTask("https://www.itjuzi.com/user/login");

		agent.submit(task);

		Thread.sleep(1000000);

	}

}
