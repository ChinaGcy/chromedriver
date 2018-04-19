package com.sdyk.ai.crawler;

import com.sdyk.ai.crawler.zbj.model.Project;
import com.sdyk.ai.crawler.zbj.model.TendererRating;
import com.sdyk.ai.crawler.zbj.task.modelTask.*;
import com.sdyk.ai.crawler.zbj.task.scanTask.ProjectScanTask;
import one.rewind.io.requester.Task;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.account.AccountImpl;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.chrome.action.ChromeAction;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.List;


public class JsoupText {

	/**
	 * 测试projectTask
	 * @throws Exception
	 */
	@Test
	public void projectTest() throws Exception {
		Account account = new AccountImpl("zbj.com", "15284812411", "123456");
		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();

		Task task = new Task("http://www.zbj.com");
		task.setBuildDom();
		agent.submit(task);
		ChromeAction action = new LoginWithGeetestAction(account);
		task.addAction(action);
		agent.submit(task);

		ProjectTask task1 = new ProjectTask("https://task.zbj.com/13234141/");
		task1.setBuildDom();
		agent.submit(task1);
		task1.postProc();
	}


	/**
	 * 测试tendererTask
	 * @throws Exception
	 */
	@Test
	public void tendererTest() throws Exception {

		Account account = new AccountImpl("zbj.com", "15284812411", "123456");
		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();

		Task task = new Task("http://www.zbj.com");
		task.setBuildDom();
		agent.submit(task);
		ChromeAction action = new LoginWithGeetestAction(account);
		task.addAction(action);
		agent.submit(task);

		TendererTask task1 = new TendererTask("https://home.zbj.com/16120380");
		task1.setBuildDom();
		agent.submit(task1);
		task1.postProc();
	}

	/**
	 * 测试tendererRatingTask
	 */
	@Test
	public void tendererRatingTaskTest() throws MalformedURLException, URISyntaxException, ChromeDriverException.IllegalStatusException, ParseException {

		Account account = new AccountImpl("zbj.com", "15284812411", "123456");
		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();

		Task task = new Task("http://www.zbj.com");
		task.setBuildDom();
		agent.submit(task);
		ChromeAction action = new LoginWithGeetestAction(account);
		task.addAction(action);
		agent.submit(task);

		TendererRatingTask task1 = new TendererRatingTask("https://home.zbj.com/16120380", 1 , "16120380");
		task1.setBuildDom();
		agent.submit(task1);
		task1.postProc();
	}

	/**
	 * 测试casetask
	 */
	@Test
	public void CaseTest() throws Exception {
		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();
		CaseTask caseTask = new CaseTask("https://shop.zbj.com/4696791/sid-983087.html");
		caseTask.setBuildDom();
		agent.submit(caseTask);
		caseTask.postProc();
	}

	/**
	 * 测试ServiceSupplierTask
	 */
	@Test
	public void ServiceSupplierTaskTest() throws ChromeDriverException.IllegalStatusException, MalformedURLException, URISyntaxException {

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();
		ServiceSupplierTask serviceSupplierTask = new ServiceSupplierTask("http://shop.zbj.com/18751471/");
		serviceSupplierTask.setBuildDom();
		agent.submit(serviceSupplierTask);
		serviceSupplierTask.postProc();
	}

	/**
	 * 测试workTask
	 */
	@Test
	public void WorkTaskTest() throws ChromeDriverException.IllegalStatusException, MalformedURLException, URISyntaxException {

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();
		WorkTask workTask = new WorkTask("http://shop.zbj.com/works/detail-wid-329637.html","329637");
		workTask.setBuildDom();
		agent.submit(workTask);
		workTask.postProc();
	}

	/**
	 * 测试workTask
	 */
	@Test
	public void ServiceRatingTaskTest() throws ChromeDriverException.IllegalStatusException, MalformedURLException, URISyntaxException {

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();
		ServiceRatingTask serviceRatingTask = new ServiceRatingTask("http://shop.zbj.com/evaluation/evallist-uid-7394304-type-1-isLazyload-0-page-10.html",10);
		serviceRatingTask.setBuildDom();
		agent.submit(serviceRatingTask);
		serviceRatingTask.postProc();
	}


}
