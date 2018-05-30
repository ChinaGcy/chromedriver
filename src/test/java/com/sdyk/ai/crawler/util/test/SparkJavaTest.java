package com.sdyk.ai.crawler.util.test;

import org.junit.Test;
import spark.Spark;

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

}
