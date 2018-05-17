package com.sdyk.ai.crawler.zbj.task.test;

import com.sdyk.ai.crawler.zbj.model.Project;
import com.sdyk.ai.crawler.zbj.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.zbj.task.Task;

import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.account.AccountImpl;
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
		Account account = new AccountImpl("zbj.com", "15284812411", "123456");
		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();

		one.rewind.io.requester.Task task = new one.rewind.io.requester.Task("http://www.zbj.com");
		task.setBuildDom();
		agent.submit(task);
		ChromeAction action = new LoginWithGeetestAction(account);
		task.addAction(action);
		agent.submit(task);

		ProjectTask task1 = new ProjectTask("https://task.zbj.com/13371103/");
		task1.setBuildDom();
		agent.submit(task1);
	}

	@Test
	public void dummyTest() throws Exception {

		Refacter.dropTable(Project.class);
		Refacter.createTable(Project.class);

		Project p = new Project("http://www.baidu.com/test");
		p.insert();
	}
}
