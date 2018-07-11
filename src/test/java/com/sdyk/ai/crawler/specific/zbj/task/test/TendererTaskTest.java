package com.sdyk.ai.crawler.specific.zbj.task.test;

import com.sdyk.ai.crawler.Distributor;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;

public class TendererTaskTest {

	@Before
	public void setup() {
		Distributor.URL_VISITS.clear();
		ChromeDriverDistributor.instance = new Distributor();
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
