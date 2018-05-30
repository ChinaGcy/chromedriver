package com.sdyk.ai.crawler.specific.zbj.task.test;

import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.specific.zbj.AuthorizedRequester;
import com.sdyk.ai.crawler.specific.zbj.Scheduler;
import com.sdyk.ai.crawler.specific.zbj.task.action.GetProjectContactAction;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class ProjectTest {


	// https://ucenter.zbj.com/phone/getANumByTask
	/**
	 * 测试获取最优需求数据
	 */
	@Test
	public void test() throws Exception {

		Account account = AccountManager.getAccountByDomain("zbj.com", "A");

		//ChromeDriverAgent agent = new ChromeDriverAgent(container.getRemoteAddress());

		com.sdyk.ai.crawler.specific.zbj.task.Task task = new com.sdyk.ai.crawler.specific.zbj.task.Task("zbj.com");

		task.addAction(new LoginWithGeetestAction(account));

		ChromeDriverAgent agent = new ChromeDriverAgent();

		AuthorizedRequester.getInstance().addAgent(agent);

		agent.start();

		AuthorizedRequester.getInstance().submit(task);

		ProjectTask task1 = new ProjectTask("https://task.zbj.com/13501948/");

		task.addAction(new GetProjectContactAction(task1.project));

		AuthorizedRequester.getInstance().submit(task1);

		Thread.sleep(10000000);

	}
}
