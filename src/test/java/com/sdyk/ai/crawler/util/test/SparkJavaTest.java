package com.sdyk.ai.crawler.util.test;

import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.Scheduler;
import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.account.model.AccountImpl;
import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.model.Project;
import com.sdyk.ai.crawler.specific.zbj.AuthorizedRequester;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.db.DaoManager;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import org.junit.Test;
import spark.Spark;

import java.util.List;

import static spark.Spark.port;
import static spark.route.HttpMethod.get;

public class SparkJavaTest {

	@Test
	public void post() throws InterruptedException {
		/*post("/", (request, respones) -> {
			return ;
		});*/

		port(80);

		// matches "GET /hello/foo" and "GET /hello/bar"
		// request.params(":name") is 'foo' or 'bar'
		/*Spark.get("/hello/:name", (request, response) -> {
			request.body();

			return "Hello: " + request.params(":name")+ response.body();
		});*/

		Spark.post("/hello/:name", (request, response) -> {

		return "Hello: " + request.params(":name")+ response.body();
	});

		Thread.sleep(500000);
	}

	@Test
	public void getPhone() throws Exception {

		Account account = AccountManager.getAccountByDomain("zbj.com", "select");

		com.sdyk.ai.crawler.task.Task task = new Task("https://www.zbj.com");

		task.addAction(new LoginWithGeetestAction(account));

		// 不使用代理
		ChromeDriverAgent agent = new ChromeDriverAgent();

		// agent 添加异常回调
		agent.addAccountFailedCallback(()->{

		}).addTerminatedCallback(()->{

		}).addNewCallback(()->{

			try {
				agent.submit(task, 300000);
			} catch (Exception e) {
			}
		});

		AuthorizedRequester.getInstance().addAgent(agent);
		agent.start();

		AuthorizedRequester.getInstance().submit(task);

		ServiceWrapper.getInstance();

		Thread.sleep(1000000);
	}

	@Test
	public void testRount() throws InterruptedException {
		ServiceWrapper.getInstance();
		Thread.sleep(1000000);
	}

	@Test
	public void testDao() throws Exception {
		Project project = DaoManager.getDao(Project.class).queryForId("984f9e1224cad0f27bcbaea18aef85d4");

		System.err.println(project.toJSON());
	}
}
