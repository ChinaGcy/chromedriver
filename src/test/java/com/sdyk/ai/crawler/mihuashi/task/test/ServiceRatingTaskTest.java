package com.sdyk.ai.crawler.mihuashi.task.test;


import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ServiceRatingTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class ServiceRatingTaskTest {

    @Test
    public void test() throws Exception {

	    ChromeDriverAgent agent = new ChromeDriverAgent();
	    agent.start();

	    ServiceRatingTask serviceRatingTask = new ServiceRatingTask("https://www.mihuashi.com/users/playerjsc?role=painter&rating=true&num=13");

	    agent.submit(serviceRatingTask);

	    Thread.sleep(10000000);
    }

}
