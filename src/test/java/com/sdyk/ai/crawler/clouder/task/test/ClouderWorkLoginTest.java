package com.sdyk.ai.crawler.clouder.task.test;


import com.sdyk.ai.crawler.specific.clouderwork.util.ClouderWorkLogin;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.MalformedURLException;
import java.net.URISyntaxException;


public class ClouderWorkLoginTest {

    @Test
    public void clouderWorkLoginTest () throws MalformedURLException, URISyntaxException, ChromeDriverException.IllegalStatusException, InterruptedException {

        ChromeDriverAgent agent = new ChromeDriverAgent();
        agent.start();
        ClouderWorkLogin.login(agent);
        agent.stop();

    }
}
