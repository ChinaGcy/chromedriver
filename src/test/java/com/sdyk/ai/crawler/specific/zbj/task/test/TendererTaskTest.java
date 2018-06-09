package com.sdyk.ai.crawler.specific.zbj.task.test;

import com.sdyk.ai.crawler.Requester;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.TendererRatingTask;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.TendererTask;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.account.AccountImpl;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.chrome.action.ChromeAction;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.junit.Before;
import org.junit.Test;
import one.rewind.io.requester.chrome.ChromeDriverAgent;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;

public class TendererTaskTest {

	@Before
	public void setup() {
		Requester.URL_VISITS.clear();
		ChromeDriverRequester.instance = new Requester();
		ChromeDriverRequester.requester_executor.submit(ChromeDriverRequester.instance);
	}

	/**
	 * 测试tendererTask
	 * @throws Exception
	 */
	@Test
	public void tendererTest() throws Exception {

		Account account = new AccountImpl("zbj.com", "15284812411", "123456");
		// Proxy proxy = ProxyManager.getInstance().getProxyById("6");
		// proxy.validate();

		ChromeDriverAgent agent = new ChromeDriverAgent();
		ChromeDriverRequester.getInstance().addAgent(agent);

		agent.start();

		one.rewind.io.requester.Task task = new one.rewind.io.requester.Task("http://www.zbj.com");
		ChromeAction action = new LoginWithGeetestAction(account);
		task.addAction(action);

		ChromeDriverRequester.getInstance().submit(task);


		task = new TendererTask("https://home.zbj.com/13553582");
		task.setBuildDom();
		ChromeDriverRequester.getInstance().submit(task);

		Thread.sleep(10000000);

	}

	/**
	 * 测试tendererRatingTask
	 */
	@Test
	public void tendererRatingTaskTest() throws MalformedURLException, URISyntaxException, ChromeDriverException.IllegalStatusException, ParseException {

		Account account = new AccountImpl("zbj.com", "15284812411", "123456");
		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();

		one.rewind.io.requester.Task task = new one.rewind.io.requester.Task("http://www.zbj.com");
		task.setBuildDom();
		agent.submit(task);
		ChromeAction action = new LoginWithGeetestAction(account);
		task.addAction(action);
		agent.submit(task);

		TendererRatingTask task1 = new TendererRatingTask("https://home.zbj.com/16120380", 1 , "16120380");
		task1.setBuildDom();
		agent.submit(task1);
	}
}
