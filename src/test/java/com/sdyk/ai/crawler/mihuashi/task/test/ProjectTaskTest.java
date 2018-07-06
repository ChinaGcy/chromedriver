package com.sdyk.ai.crawler.mihuashi.task.test;

import com.sdyk.ai.crawler.Requester;
import com.sdyk.ai.crawler.account.model.AccountImpl;

import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;

import one.rewind.io.requester.exception.ChromeDriverException;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class ProjectTaskTest {

	@Test
	public void test() throws ChromeDriverException.IllegalStatusException, MalformedURLException, URISyntaxException, InterruptedException {

		/*ChromeDriverRequester.instance = new Requester();
		ChromeDriverRequester.requester_executor.submit(ChromeDriverRequester.instance);
		ChromeDriverAgent agent = new ChromeDriverAgent();
		ChromeDriverRequester.getInstance().addAgent(agent);
*/
		/*agent.start();
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
		com.sdyk.ai.crawler.task.Task projectTask = new ProjectTask("https://www.mihuashi.com/projects/17035");
		ChromeDriverRequester.getInstance().submit(projectTask);
		Thread.sleep(10000000);*/



	}

}
