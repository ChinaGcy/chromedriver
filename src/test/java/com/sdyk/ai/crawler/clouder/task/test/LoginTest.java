package com.sdyk.ai.crawler.clouder.task.test;

import com.sdyk.ai.crawler.specific.clouderwork.LoginWithGeetestClouderWork;
import com.sdyk.ai.crawler.account.model.AccountImpl;
import one.rewind.io.requester.Task;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class LoginTest {

    @Test
    public void test() throws MalformedURLException, URISyntaxException, ChromeDriverException.IllegalStatusException, InterruptedException {

/*        String domain = "passport.clouderwork.com";
        AccountImpl account = new AccountImpl(domain,"17600485107","123456");
        ChromeDriverAgent agent = new ChromeDriverAgent();
        Task t = new Task("https://passport.clouderwork.com/signin");
        agent.start();
        t.setBuildDom();
        t.addAction(new LoginWithGeetestClouderWork(account));
        agent.submit(t);
        Thread.sleep(10000);
        agent.stop();*/
    }

}
