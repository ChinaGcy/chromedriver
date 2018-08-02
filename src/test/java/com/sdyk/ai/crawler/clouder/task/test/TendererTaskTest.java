package com.sdyk.ai.crawler.clouder.task.test;


import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.TendererTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class TendererTaskTest {

    @Test
    public void testTendererTask() throws Exception {

        ChromeDriverAgent agent = new ChromeDriverAgent();
        agent.start();

	    TendererTask tendererTask = new TendererTask("https://www.clouderwork.com/clients/055b9c963bf4e206");
	    agent.submit(tendererTask);

	    Thread.sleep(10000000);

    }
}
