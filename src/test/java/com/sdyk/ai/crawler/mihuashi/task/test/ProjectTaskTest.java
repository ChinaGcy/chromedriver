package com.sdyk.ai.crawler.mihuashi.task.test;


import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.task.LoginTask;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.LoginAction;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.exception.ProxyException;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static one.rewind.util.FileUtil.readFileByLines;

public class ProjectTaskTest {

	@Test
	public void test() throws Exception {

		ChromeDriverAgent agent = new ChromeDriverAgent();

		agent.start();

		LoginTask loginTask = LoginTask.buildFromJson(readFileByLines("login_tasks/mihuashi.com.json"));

		// 添加账号
		Account a = AccountManager.getInstance().getAccountByDomain("clouderwork.com");
		((LoginAction)loginTask.getActions().get(loginTask.getActions().size() - 1)).setAccount(a);

		agent.submit(loginTask);

		ProjectTask projectTask = new ProjectTask("https://www.mihuashi.com/projects/19970/");

		agent.submit(projectTask);

		Thread.sleep(100000000);
	}

}
