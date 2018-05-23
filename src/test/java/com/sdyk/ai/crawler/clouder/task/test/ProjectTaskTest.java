package com.sdyk.ai.crawler.clouder.task.test;

import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.TendererTask;
import com.sdyk.ai.crawler.specific.clouderwork.util.ClouderWorkLogin;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class ProjectTaskTest {

    @Test
    public void testProjectTask() throws MalformedURLException, URISyntaxException, InterruptedException, ChromeDriverException.IllegalStatusException {

        ChromeDriverAgent agent = new ChromeDriverAgent();
        agent.start();
        ClouderWorkLogin.login(agent);
        ProjectTask projectTask = new ProjectTask("https://www.clouderwork.com/jobs/4d38d5968070c2be");
        agent.submit(projectTask);
        agent.stop();

    }

}
