package com.sdyk.ai.crawler.zbj.task.test;

import com.sdyk.ai.crawler.zbj.model.Project;
import com.sdyk.ai.crawler.zbj.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.zbj.task.Task;

import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.account.AccountImpl;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.chrome.action.ChromeAction;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import org.junit.Test;
import one.rewind.db.Refacter;
import one.rewind.io.requester.chrome.ChromeDriverAgent;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.Queue;

public class ProjectTaskTest {

	@Test
	public void projectTest() throws Exception {

		ChromeDriverRequester requester = ChromeDriverRequester.getInstance();
		Account account = new AccountImpl("zbj.com", "15284812411", "123456");

		ChromeDriverAgent agent = new ChromeDriverAgent();

		requester.addAgent(agent);

		agent.start();

		one.rewind.io.requester.Task task = new one.rewind.io.requester.Task("http://www.zbj.com");
		task.setBuildDom();
		requester.submit(task);
		ChromeAction action = new LoginWithGeetestAction(account);
		task.addAction(action);
		requester.submit(task);

		ProjectTask task1 = new ProjectTask("http://task.zbj.com/13437412/");
		task1.setBuildDom();

		Thread.sleep(5000);
		requester.submit(task1);

		Thread.sleep(1000000);
	}

	@Test
	public void dummyTest() throws Exception {

		Refacter.dropTable(Project.class);
		Refacter.createTable(Project.class);

		Project p = new Project("http://www.baidu.com/test");
		p.insert();
	}
}
