package com.sdyk.ai.crawler.clouder.task.test;

import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class ProjectScanTaskTest {

    @Test
    public void testProjectScanTask() throws IOException, URISyntaxException, ChromeDriverException.IllegalStatusException, InterruptedException {

        int page = 1;

        ChromeDriverAgent agent = new ChromeDriverAgent();
        agent.start();
       /* ClouderWorkLogin.login(agent);
        ProjectScanTask projectScanTask = new ProjectScanTask("https://www.clouderwork.com/",page);

        agent.submit(projectScanTask);*/
        agent.stop();

    }
}
