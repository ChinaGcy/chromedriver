package com.sdyk.ai.crawler.specific.zbj.task.test;

import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.model.witkey.Tenderer;
import one.rewind.db.DaoManager;
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
		Dao dao = DaoManager.getDao(Tenderer.class);

		Tenderer tenderer = (Tenderer) dao.queryForId("00117af92e06a72ad0c096b492282329");

		tenderer.insertES();

	}

	/**
	 * 测试tendererRatingTask
	 */
	@Test
	public void tendererRatingTaskTest() throws MalformedURLException, URISyntaxException, ChromeDriverException.IllegalStatusException, ParseException {


	}
}
