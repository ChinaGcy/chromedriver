package com.sdyk.ai.crawler.specific.jfh.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.jfh.task.modelTask.ProjectTask;
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


public class ProjectScanTask extends ScanTask {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				ProjectScanTask.class,
				"https://www.jfh.com/jfportal/workMarket/getRequestData"
						+"?buId=&" + "city=&" + "fitCode=0&" + "isRemoteWork=&" + "jfId=234895673&"
						+"jieBaoType=&" + "login=1&" + "maxPrice=&" + "minPrice=&" + "orderCondition=0&"
						+"orderConfig=1&" + "orderType=&" + "pageNo={{page}}&" + "putTime=&" + "searchName=&"
						+"serviceTypeKey=&" + "webSign=&" + "webSite=&" + "workStyleCode=",
				ImmutableMap.of("page", String.class),
				ImmutableMap.of("page", "")
		);
	}

	public ProjectScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setPriority(Priority.HIGH);

		this.setParam("page", url.split("pageNo=")[1].split("&putTime")[0]);

		this.addDoneCallback((t) -> {

			int page = Integer.valueOf(url.split("pageNo=")[1].split("&putTime")[0]);

			String src = getResponse().getText();
			Set<String> project = new HashSet<>();

			Pattern pattern = Pattern.compile("\"orderNo\":\"(?<Id>.+?)\",\"orderType\"");
			Matcher matcher = pattern.matcher(src);

			while (matcher.find()) {
				try {
					project.add("https://www.jfh.com/jfportal/orders/jf" + matcher.group("Id"));
				} catch (Exception e) {
					logger.error("error for matcher.group", e);
				}
			}

			for( String projectUrl : project ){

				try {

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					init_map.put("project_id", projectUrl);

					Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.jfh.task.modelTask.ProjectTask");

					//生成holder
					ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

					//提交任务
					((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

				} catch ( Exception e) {

					logger.error("error for submit ProjectTask.class", e);
				}

			}

			if( !(project.size() < 10) ){
				int nextPage = page +1;

				try {

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					init_map.put("page", String.valueOf(nextPage));

					Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.jfh.task.scanTask.ProjectScanTask");

					//生成holder
					ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

					//提交任务
					((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

				} catch ( Exception e) {

					logger.error("error for submit ProjectScanTask.class", e);
				}

			}

		});

	}

	@Override
	public TaskTrace getTaskTrace() {

		return new TaskTrace(this.getClass(), "all", this.getParamString("page"));
	}
}
