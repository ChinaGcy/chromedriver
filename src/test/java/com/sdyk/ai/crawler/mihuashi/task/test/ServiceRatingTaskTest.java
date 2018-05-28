package com.sdyk.ai.crawler.mihuashi.task.test;

import com.sdyk.ai.crawler.Requester;
import com.sdyk.ai.crawler.account.model.AccountImpl;
import com.sdyk.ai.crawler.specific.clouderwork.LoginWithGeetestClouderWork;
import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ServiceRatingTask;
import com.sdyk.ai.crawler.specific.mihuashi.action.TendererRatingActive;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class ServiceRatingTaskTest {

    @Test
    public void test() throws ChromeDriverException.IllegalStatusException, MalformedURLException, URISyntaxException, InterruptedException {

        ChromeDriverRequester.instance = new Requester();
        ChromeDriverRequester.requester_executor.submit(ChromeDriverRequester.instance);
        ChromeDriverAgent agent = new ChromeDriverAgent();
        ChromeDriverRequester.getInstance().addAgent(agent);

        agent.start();
        String url = "https://www.mihuashi.com/login";
        String usernameCssPath = "#login-app > main > section > section.session__form-wrapper > section > div:nth-child(1) > input";
        String passwordCssPath = "#login-app > main > section > section.session__form-wrapper > section > div:nth-child(2) > input";
        String loginButtonCssPath = "#login-app > main > section > section.session__form-wrapper > section > div:nth-child(3) > button";
        Account account = new AccountImpl(url,"zhangsheng@315free.com","123456");
        Task task = new Task(url);
        task.addAction(new LoginWithGeetestClouderWork(account,url,usernameCssPath,passwordCssPath,loginButtonCssPath));
        task.setBuildDom();
        ChromeDriverRequester.getInstance().submit(task);
        Thread.sleep(5000);

        com.sdyk.ai.crawler.task.Task sr = new ServiceRatingTask("https://www.mihuashi.com/users/%E6%9C%A8%E5%AD%90%E5%B2%9A%E5%B7%A5%E4%BD%9C%E5%AE%A4?role=painter");
        sr.addAction(new TendererRatingActive("https://www.mihuashi.com/users/%E6%9C%A8%E5%AD%90%E5%B2%9A%E5%B7%A5%E4%BD%9C%E5%AE%A4?role=painter"));
        sr.setBuildDom();
        ChromeDriverRequester.getInstance().submit(sr);

        Thread.sleep(10000000);
        agent.stop();

    }

}
