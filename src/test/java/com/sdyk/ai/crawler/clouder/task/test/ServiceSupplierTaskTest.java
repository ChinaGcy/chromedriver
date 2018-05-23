package com.sdyk.ai.crawler.clouder.task.test;

import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ServiceSupplierTask;
import com.sdyk.ai.crawler.specific.clouderwork.util.ClouderWorkLogin;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class ServiceSupplierTaskTest {

    @Test
    public void testServiceSupplierTask() throws MalformedURLException, URISyntaxException, InterruptedException, ChromeDriverException.IllegalStatusException {
        ChromeDriverAgent agent = new ChromeDriverAgent();
        agent.start();
        ServiceSupplierTask serviceSupplierTask = new ServiceSupplierTask("https://www.clouderwork.com/freelancers/ade04973c8af2099");
        ClouderWorkLogin.login(agent);
        serviceSupplierTask.setBuildDom();
        agent.submit(serviceSupplierTask);
        Document doc = serviceSupplierTask.getResponse().getDoc();
        serviceSupplierTask.crawlerJob(doc);
        agent.stop();
    }
}
