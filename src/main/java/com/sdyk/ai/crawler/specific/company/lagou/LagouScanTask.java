package com.sdyk.ai.crawler.specific.company.lagou;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.task.ScanTask;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskFactory;
import one.rewind.io.requester.task.TaskHolder;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LagouScanTask extends ScanTask {

	public static long MIN_INTERVAL = 1000L;

	static {
		registerBuilder(
				LagouScanTask.class,
				"https://www.lagou.com/jobs/list_"
						+ "{{company_name}}"
						+"?labelWords=&fromSearch=true&suginput=&uId={{uId}}",
				ImmutableMap.of("company_name", String.class,"uId",String.class),
				ImmutableMap.of("company_name", "","uId",""),
				true,
				Priority.HIGHEST
		);
	}

	public static String domain(){
		return "lagou";
	}


	public LagouScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			crawler(doc);

		});

	}

	public void crawler( Document doc){

		String uId = null;
		Pattern pattern_url = Pattern.compile("uId=(?<uId>.+)");
		Matcher matcher_url = pattern_url.matcher(getUrl());
		if (matcher_url.find()) {
			uId = matcher_url.group("uId");
		}

		Elements jobList = doc.select("ul.item_con_list > li.default_list");

		List<com.sdyk.ai.crawler.task.Task> task = new ArrayList<>();

		for(Element element : jobList){

			String jobUrl = element.select("div.list_item_top > div > div > a.position_link")
					.attr("href");

			// 提交拉钩抓取任务
			try {

				//设置参数
				Map<String, Object> init_map = new HashMap<>();
				init_map.put("jobUrl", jobUrl.replace("https://www.lagou.com/jobs/",""));
				init_map.put("uId", uId);

				Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.company.lagou.LagouTask");

				//生成holder
				TaskHolder holder =  ChromeTaskFactory.getInstance().newHolder(clazz, init_map);

				//提交任务
				((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

			} catch ( Exception e) {

				logger.error("error for submit LagouTask.class", e);
			}

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
