package com.sdyk.ai.crawler.specific.zbj.task.test;

import com.sdyk.ai.crawler.model.witkey.Project;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.ProjectTask;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.account.AccountImpl;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.ChromeAction;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import org.junit.Test;
import one.rewind.db.Refacter;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class ProjectTaskTest {

	@Test
	public void projectTest() throws Exception {


	}

	@Test
	public void dummyTest() throws Exception {

		Refacter.dropTable(Project.class);
		Refacter.createTable(Project.class);

		Project p = new Project("http://www.baidu.com/NumberExtractTest");
		p.insert();
	}

	@Test
	public void projectTaskTest() throws ChromeDriverException.IllegalStatusException, InterruptedException, MalformedURLException, URISyntaxException, ProxyException.Failed {


		Account account = new AccountImpl("zbj.com", "15284812411", "123456");

		for (int i = 0; i < 10; i++) {

			ChromeDriverAgent agent = new ChromeDriverAgent();
			agent.start();

			ChromeTask task = new ChromeTask("https://login.zbj.com/login");
			ChromeAction action = new LoginWithGeetestAction(account);
			task.addAction(action);
			try {
				agent.submit(task);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Thread.sleep(5000);

			task = new ProjectTask("https://task.zbj.com/13860317/");

			agent.submit(task);

			agent.stop();
		}
	}
}
