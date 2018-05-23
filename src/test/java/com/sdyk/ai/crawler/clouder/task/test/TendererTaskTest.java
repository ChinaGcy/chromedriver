package com.sdyk.ai.crawler.clouder.task.test;

import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.TendererTask;
import com.sdyk.ai.crawler.specific.clouderwork.util.ClouderWorkLogin;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class TendererTaskTest {

    @Test
    public void testTendererTask() throws IOException, URISyntaxException, InterruptedException, ChromeDriverException.IllegalStatusException {
        ChromeDriverAgent agent = new ChromeDriverAgent();
        TendererTask tendererTask = new TendererTask("https://www.clouderwork.com/clients/eece46691b11d040");
        tendererTask.setBuildDom();
        agent.start();
        ClouderWorkLogin.login(agent);
        agent.submit(tendererTask);
        agent.stop();

    }
}
