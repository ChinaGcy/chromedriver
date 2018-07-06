package com.sdyk.ai.crawler.clouder.task.test;

import com.sdyk.ai.crawler.Requester;
import com.sdyk.ai.crawler.account.model.AccountImpl;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.jsoup.nodes.Document;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class TendererTaskTest {

    @Test
    public void testTendererTask() throws IOException, URISyntaxException, InterruptedException, ChromeDriverException.IllegalStatusException {

        //初始化Requester
       /* ChromeDriverRequester.instance = new Requester();
        ChromeDriverRequester.requester_executor.submit(ChromeDriverRequester.instance);
*/

        //初始化agent
        ChromeDriverAgent agent = new ChromeDriverAgent();
        /*ChromeDriverRequester.getInstance().addAgent(agent);*/
        agent.start();

        //进行登录
        String url = "https://passport.clouderwork.com/signin";
        String usernameCssPath = "#app > div > div > div > section > dl > dd:nth-child(1) > input[category=\"text\"]";
        String passwordCssPath = "#app > div > div > div > section > dl > dd:nth-child(2) > input[category=\"password\"]";
        String loginButtonCssPath = "#app > div > div > div > section > button:nth-child(3)";
        Account account = new AccountImpl(url,"17152187084","123456");
      /*  Task task = new Task(url);
        task.addAction(new LoginWithGeetestClouderWork(
                account, url, usernameCssPath, passwordCssPath, loginButtonCssPath));
        task.setBuildDom();*/

       /* ChromeDriverRequester.getInstance().submit(task);*/

        //抓取甲方数据
        /*com.sdyk.ai.crawler.task.Task ssTask = new TendererTask("https://www.clouderwork.com/clients/b7e8c0497b082e6f");
        ssTask.setBuildDom();
        ChromeDriverRequester.getInstance().submit(ssTask);*/

        Thread.sleep(10000000);
        agent.stop();

    }
}
