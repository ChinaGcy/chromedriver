package com.sdyk.ai.crawler.clouder.task.test;

import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class CaseTaskTest {

    @Test
    public void testCaseTask() throws InterruptedException, MalformedURLException, URISyntaxException, ChromeDriverException.IllegalStatusException {
        ChromeDriverAgent agent = new ChromeDriverAgent();
        agent.start();
       /* ClouderWorkLogin.login(agent);
        String userUrl = "https://www.clouderwork.com/freelancers/f8fb36043e78a1b0";
        CaseTask caseTask = new CaseTask("https://www.clouderwork.com/project/347",userUrl);
        agent.submit(caseTask);*/
        agent.stop();
    }
}
