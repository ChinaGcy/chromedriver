package com.sdyk.ai.crawler.specific.company.itjuzi;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.task.ScanTask;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.TaskHolder;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItjuziScanTAsk extends ScanTask {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				ItjuziScanTAsk.class,
				"https://www.itjuzi.com/search?word={{company_name}}&uId={{uId}}",
				ImmutableMap.of("company_name", String.class, "uId", String.class),
				ImmutableMap.of("company_name", "", "uId", "")
		);
	}

	public ItjuziScanTAsk(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setNoFetchImages();

		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			crawler(doc);

		});

	}

	public void crawler(Document doc){

		String uId = null;
		Pattern pattern_url = Pattern.compile("uId=(?<uId>.+?)");
		Matcher matcher_url = pattern_url.matcher(getUrl());
		if (matcher_url.find()) {
			uId = matcher_url.group("uId");
		}

		String companyUrl = doc.select("#the_search_list > li:nth-child(1) > a").attr("href");

		// 提交IT橘子任务
		try {

			//设置参数
			Map<String, Object> init_map = new HashMap<>();
			init_map.put("companyUrl", companyUrl);
			init_map.put("uId", uId);

			Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.company.itjuzi.ItjuziTask");

			//生成holder
			TaskHolder holder = this.getHolder(clazz, init_map);

			//提交任务
			((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

		} catch ( Exception e) {

			logger.error("error for submit ItjuziTask.class", e);
		}

	}

	/**
	 * 判断是否为最大页数
	 *
	 * @param path
	 * @param page
	 * @return
	 */
	@Override
	public boolean pageTurning(String path, int page) {
		return false;
	}

	/**
	 * 获取ScanTask 标识
	 *
	 * @return
	 */
	@Override
	public TaskTrace getTaskTrace() {
		return null;
	}

}
