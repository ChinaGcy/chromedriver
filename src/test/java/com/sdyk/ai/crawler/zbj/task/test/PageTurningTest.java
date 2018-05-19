package com.sdyk.ai.crawler.zbj.task.test;

import com.sdyk.ai.crawler.zbj.account.model.AccountImpl;
import com.sdyk.ai.crawler.zbj.task.Task;
import com.sdyk.ai.crawler.zbj.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.zbj.task.scanTask.ProjectScanTask;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class PageTurningTest {

	// 翻页测试
	@Test
	public void pageTest() throws ChromeDriverException.IllegalStatusException, MalformedURLException, URISyntaxException, InterruptedException {


		Account account = new one.rewind.io.requester.account.AccountImpl("zbj.com", "15284812411", "123456");

		ChromeDriverRequester requester = ChromeDriverRequester.getInstance();

		ChromeDriverAgent agent = new ChromeDriverAgent();

		requester.addAgent(agent);

		agent.start();

		Task task = new Task("http://www.zbj.com");
		task.addAction(new LoginWithGeetestAction(account));
		requester.submit(task);

		task = ProjectScanTask.generateTask("t-wzkf", 1);

		requester.submit(task);

		Thread.sleep(1000000);
	}
}
