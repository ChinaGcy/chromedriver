package com.sdyk.ai.crawler.specific.zbj.task.test;

import com.sdyk.ai.crawler.Requester;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.TendererRatingTask;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.TendererTask;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.account.AccountImpl;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
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
		ChromeDriverDistributor.instance = new Requester();
	}

	/**
	 * 测试tendererTask
	 * @throws Exception
	 */
	@Test
	public void tendererTest() throws Exception {



	}

	/**
	 * 测试tendererRatingTask
	 */
	@Test
	public void tendererRatingTaskTest() throws MalformedURLException, URISyntaxException, ChromeDriverException.IllegalStatusException, ParseException {


	}
}
