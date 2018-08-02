package com.sdyk.ai.crawler.clouder.task.test;


import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ServiceProviderTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class ServiceProviderTaskTest {

    @Test
    public void testServiceSupplierTask() throws Exception {

	    ChromeDriverAgent agent = new ChromeDriverAgent();
	    agent.start();

	    ServiceProviderTask serviceProviderTask = new ServiceProviderTask("https://www.clouderwork.com/freelancers/028eb6757a7a7d6a");

	    agent.submit(serviceProviderTask);

	    Thread.sleep(10000000);

    }
}
