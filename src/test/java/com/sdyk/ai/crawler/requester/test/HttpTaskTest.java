package com.sdyk.ai.crawler.requester.test;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Scheduler;
import one.rewind.io.requester.HttpTaskSubmitter;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.test.TestChromeTask;
import one.rewind.json.JSON;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class HttpTaskTest {

	@Test
	public void httpSubmit() throws Exception {

		Scheduler.Flags = new ArrayList<>();

		Scheduler.getInstance();

		//HttpTaskPoster.getInstance().submit(TestChromeTask.T5.class.getName(), JSON.toJson(ImmutableMap.of("q" ,"ip")));

		HttpTaskSubmitter.getInstance().submit(TestChromeTask.T5.class.getName(), JSON.toJson(ImmutableMap.of("q" ,"ip")));
	}
}
