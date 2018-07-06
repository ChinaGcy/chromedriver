package com.sdyk.ai.crawler.mihuashi.task.test;

import com.sdyk.ai.crawler.Requester;
import com.sdyk.ai.crawler.account.model.AccountImpl;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class TendererProjectActiveTest {

    @Test
    public void test() throws MalformedURLException, URISyntaxException, ChromeDriverException.IllegalStatusException, InterruptedException {

        /*ChromeDriverRequester.instance = new Requester();
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

        com.sdyk.ai.crawler.task.Task taskT = new TendererTask("https://www.mihuashi.com/users/%E7%BD%91%E6%98%93%EF%BC%88%E6%9D%AD%E5%B7%9E%EF%BC%89-%E7%A5%9E%E9%83%BD%E5%A4%9C%E8%A1%8C%E5%BD%95%E9%A1%B9%E7%9B%AE%E7%BB%84?role=employer");
        taskT.addAction(new TendererProjectActive("https://www.mihuashi.com/users/%E7%BD%91%E6%98%93%EF%BC%88%E6%9D%AD%E5%B7%9E%EF%BC%89-%E7%A5%9E%E9%83%BD%E5%A4%9C%E8%A1%8C%E5%BD%95%E9%A1%B9%E7%9B%AE%E7%BB%84?role=employer"));
        taskT.setBuildDom();
        ChromeDriverRequester.getInstance().submit(taskT);

        Thread.sleep(1000000);

        agent.stop();*/

    }


}
