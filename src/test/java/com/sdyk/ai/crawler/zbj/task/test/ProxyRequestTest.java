package com.sdyk.ai.crawler.zbj.task.test;

import com.sdyk.ai.crawler.zbj.model.Proxy;
import org.junit.Test;
import org.tfelab.io.requester.Task;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;
import org.tfelab.io.requester.proxy.ProxyWrapperImpl;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class ProxyRequestTest {

	@Test
	public void testRequestByProxy() throws MalformedURLException, URISyntaxException {

		Task t = new Task("https://www.baidu.com/s?wd=ip");

	}
}
