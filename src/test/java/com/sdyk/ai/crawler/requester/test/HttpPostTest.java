package com.sdyk.ai.crawler.requester.test;

import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.Scheduler;
import one.rewind.io.requester.BasicRequester;
import one.rewind.io.requester.HttpTaskSubmitter;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.task.ChromeTask;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class HttpPostTest {

	@Test
	public void test() throws Exception {

		Scheduler.Flags = new ArrayList<>();

		// Scheduler 初始化
		Scheduler.getInstance();

		String class_name = "com.sdyk.ai.crawler.specific.clouderwork.task.scanTask.ProjectScanTask";
		String init_map_json = "{\"page\":\"2\",\"max_page\":\"\"}";

		HttpTaskSubmitter.getInstance().submit(class_name, init_map_json);

		Thread.sleep(1000000);
	}

	@Test
	public void testBasicRequester() throws Exception {

		Scheduler.Flags = new ArrayList<>();

		// Scheduler 初始化
		Scheduler.getInstance();

		String url = "https://www.baidu.com/s?wd=ip";
		ChromeTask task = new ChromeTask(url);
		task.setPost();

		BasicRequester.getInstance().submit(task);

		Thread.sleep(1000000);
	}
}
