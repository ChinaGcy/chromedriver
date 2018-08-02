package com.sdyk.ai.crawler.clouder.task.test;


import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ProjectTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class ProjectTaskTest {

    @Test
    public void testProjectTask() throws Exception {

	    ChromeDriverAgent agent = new ChromeDriverAgent();
	    agent.start();

	    // 不同类型页面有差异
	    // 驻场    https://www.clouderwork.com/jobs/0231ba693af8ea5b
	    // 非驻场  https://www.clouderwork.com/jobs/5322791db8d612b6

	    ProjectTask projectTask = new ProjectTask("https://www.clouderwork.com/jobs/ac12c52490097d84");

	    agent.submit(projectTask);

		Thread.sleep(1000000);
    }

}
