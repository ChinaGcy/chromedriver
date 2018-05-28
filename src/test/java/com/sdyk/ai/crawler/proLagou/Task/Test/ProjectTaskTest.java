package com.sdyk.ai.crawler.proLagou.Task.Test;

import com.sdyk.ai.crawler.Requester;
import com.sdyk.ai.crawler.account.model.AccountImpl;
import com.sdyk.ai.crawler.specific.clouderwork.LoginWithGeetestClouderWork;
import com.sdyk.ai.crawler.specific.proLagou.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class ProjectTaskTest {

	@Test
	public void test() throws ChromeDriverException.IllegalStatusException, MalformedURLException, URISyntaxException, InterruptedException {

		ChromeDriverRequester.instance = new Requester();
		ChromeDriverRequester.requester_executor.submit(ChromeDriverRequester.instance);
		ChromeDriverAgent agent = new ChromeDriverAgent();
		ChromeDriverRequester.getInstance().addAgent(agent);

		agent.start();
		String url = "https://passport.lagou.com/pro/login.html";
		String usernameCssPath = "#user_name";
		String passwordCssPath = "#main > form > div:nth-child(2) > input[type=\"password\"]";
		String loginButtonCssPath = "#main > form > div.clearfix.btn_login > input[type=\"submit\"]";
		Account account = new AccountImpl(url,"17600485107","123456");
		Task task = new Task(url);
		task.addAction(new LoginWithGeetestClouderWork(account,url,usernameCssPath,passwordCssPath,loginButtonCssPath));
		task.setBuildDom();
		ChromeDriverRequester.getInstance().submit(task);
		Thread.sleep(5000);
		com.sdyk.ai.crawler.task.Task projectTask = new ProjectTask("https://pro.lagou.com/project/8084.html");
		projectTask.setBuildDom();
		ChromeDriverRequester.getInstance().submit(projectTask);
		Thread.sleep(1000000);
	}

}
