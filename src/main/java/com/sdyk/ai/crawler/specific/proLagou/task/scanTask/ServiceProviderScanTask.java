/*
package com.sdyk.ai.crawler.specific.proLagou.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.proLagou.task.modelTask.ServiceProviderTask;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceProviderScanTask extends com.sdyk.ai.crawler.task.ScanTask {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				ServiceScanTask.class,
				"https://pro.lagou.com/project/getProjectRelationUserId.json?req={{scan_id}}",
				ImmutableMap.of("scan_id", String.class),
				ImmutableMap.of("scan_id","")
		);
	}

	public static String domain(){
		return "proLagou";
	}

	public ServiceProviderScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setBuildDom();

		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			int projectId = 0;
			Pattern pattern_url = Pattern.compile("\"projectId\":(?<projectId>.+?),");
			Matcher matcher_url = pattern_url.matcher(url);
			if (matcher_url.find()) {
				projectId = Integer.parseInt(matcher_url.group("projectId"));
			}

			int page = 0;
			Pattern pattern_page = Pattern.compile("\"pageId\":(?<page>.+?),");
			Matcher matcher_page = pattern_page.matcher(url);
			if (matcher_page.find()) {
				page = Integer.parseInt(matcher_page.group("page"));
			}

			String src = getResponse().getText();

			crawler1(src, page, projectId);

		});

	}

	public void crawler1(String src, int page, int projectId){


		Pattern pattern = Pattern.compile("userId\":(?<userId>.+?),\"userTitle\"");
		Matcher matcher = pattern.matcher(src);

		Set<String> userIdSet = new HashSet<>();
		while( matcher.find() ) {
			userIdSet.add("https://pro.lagou.com/user/" + matcher.group("userId") + ".html");
		}

		for(String user : userIdSet) {

			try {

				//设置参数
				Map<String, Object> init_map = new HashMap<>();
				ImmutableMap.of("user_id", user);

				Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.proLagou.task.modelTask.ServiceProviderTask");

				//生成holder
				ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

				//提交任务
				ChromeDriverDistributor.getInstance().submit(holder);

			} catch ( Exception e) {

				logger.error("error for submit ServiceProviderTask.class", e);
			}

		}

		if(userIdSet.size() == 10){

			String[] url = getUrl().split("req=");

			String reUrl = url[1].split("pageId")[0] + "pageId\":" + (page + 1) + ",\"total\"" + url[1].split(",\"total\"")[1];

			try {

				//设置参数
				Map<String, Object> init_map = new HashMap<>();
				ImmutableMap.of("scan_id", reUrl);

				Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.proLagou.task.scanTask.ServiceProviderScanTask");

				//生成holder
				ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

				//提交任务
				ChromeDriverDistributor.getInstance().submit(holder);

			} catch ( Exception e) {

				logger.error("error for submit ServiceProviderTask.class", e);
			}

		}

	}

	*/
/**
	 * 判断是否为最大页数
	 *
	 * @param path
	 * @param page
	 * @return
	 *//*

	@Override
	public boolean pageTurning(String path, int page) {
		return false;
	}

	*/
/**
	 * 获取ScanTask 标识
	 *
	 * @return
	 *//*

	@Override
	public TaskTrace getTaskTrace() {
		return null;
	}

}
*/
