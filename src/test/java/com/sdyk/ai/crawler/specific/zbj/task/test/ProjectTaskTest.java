package com.sdyk.ai.crawler.specific.zbj.task.test;

import com.sdyk.ai.crawler.model.Project;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.ProjectTask;

import one.rewind.io.requester.Task;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.account.AccountImpl;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.chrome.action.ChromeAction;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import org.junit.Test;
import one.rewind.db.Refacter;
import one.rewind.io.requester.chrome.ChromeDriverAgent;

public class ProjectTaskTest {

	@Test
	public void projectTest() throws Exception {

		Account account = new AccountImpl("zbj.com", "15284812411", "123456");

		ChromeDriverAgent agent = new ChromeDriverAgent();

		ChromeDriverRequester.getInstance().addAgent(agent);

		agent.start();

		Task task = new Task("http://www.zbj.com");
		ChromeAction action = new LoginWithGeetestAction(account);
		task.addAction(action);
		ChromeDriverRequester.getInstance().submit(task);

		task = new ProjectTask("https://task.zbj.com/7506601");
		task.setBuildDom();

		ChromeDriverRequester.getInstance().submit(task);
		Thread.sleep(100000000);
	}

	@Test
	public void dummyTest() throws Exception {

		Refacter.dropTable(Project.class);
		Refacter.createTable(Project.class);

		Project p = new Project("http://www.baidu.com/NumberExtractTest");
		p.insert();
	}
}
