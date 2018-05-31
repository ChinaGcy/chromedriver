package com.sdyk.ai.crawler.util.test;

import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.account.model.AccountImpl;
import one.rewind.db.DaoManager;
import one.rewind.io.requester.account.Account;
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
	public void te() throws Exception {
		Dao<AccountImpl, String> dao = DaoManager.getDao(AccountImpl.class);

		List<AccountImpl> accounts = dao.queryBuilder().limit(1L).
				where().eq("domain", "zbj.com")
				.and().eq("status", Account.Status.Free)
				.and().eq("group", null)
				.query();

		System.err.println(accounts.size());
	}

}
