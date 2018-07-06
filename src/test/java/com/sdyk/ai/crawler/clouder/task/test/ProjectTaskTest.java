package com.sdyk.ai.crawler.clouder.task.test;

import com.sdyk.ai.crawler.Requester;
import com.sdyk.ai.crawler.account.model.AccountImpl;

import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;

import one.rewind.io.requester.exception.ChromeDriverException;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class ProjectTaskTest {

    @Test
    public void testProjectTask() throws MalformedURLException, URISyntaxException, InterruptedException, ChromeDriverException.IllegalStatusException {

  /*      ChromeDriverRequester.instance = new Requester();
        ChromeDriverRequester.requester_executor.submit(ChromeDriverRequester.instance);
        ChromeDriverAgent agent = new ChromeDriverAgent();
        ChromeDriverRequester.getInstance().addAgent(agent);*/

       /* agent.start();
*/
        String url = "https://passport.clouderwork.com/signin";
        String usernameCssPath = "#app > div > div > div > section > dl > dd:nth-child(1) > input[type=\"text\"]";
        String passwordCssPath = "#app > div > div > div > section > dl > dd:nth-child(2) > input[type=\"password\"]";
        String loginButtonCssPath = "#app > div > div > div > section > button:nth-child(3)";

        Account account = new AccountImpl(url,"18618490756","123456");

       /* Task task = new Task(url);
        task.addAction(new LoginWithGeetestClouderWork(account,url,usernameCssPath,passwordCssPath,loginButtonCssPath));
        ChromeDriverRequester.getInstance().submit(task);

        ProjectTask pTask = new ProjectTask("https://www.clouderwork.com/jobs/a0875a6a46f88399");
        ChromeDriverRequester.getInstance().submit(pTask);

        Thread.sleep(100000000);
        agent.stop();*/

    }

}
