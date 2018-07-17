package com.sdyk.ai.crawler.specific.jfh.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.specific.jfh.task.modelTask.ProjectTask;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
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

		this.setBuildDom();

		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			int page = 0;
			Pattern pattern_url = Pattern.compile("pageNo=(?<page>.+?)&");
			Matcher matcher_url = pattern_url.matcher(url);
			if (matcher_url.find()) {
				page = Integer.parseInt(matcher_url.group("page"));
			}

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
					HttpTaskPoster.getInstance().submit(ProjectTask.class,
							ImmutableMap.of("project_id", projectUrl));
				} catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {

					logger.error("error fro HttpTaskPoster.submit ProjectTask.class", e);
				}

			}

			if( !(project.size() < 10) ){
				int nextPage = page +1;

				try {
					HttpTaskPoster.getInstance().submit(ProjectScanTask.class,
							ImmutableMap.of("page", String.valueOf(nextPage)));
				} catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {

					logger.error("error fro HttpTaskPoster.submit ProjectScanTask.class", e);
				}
			}

		});

	}

	public static void registerBuilder(Class<? extends ChromeTask> clazz, String url_template, Map<String, Class> init_map_class, Map<String, Object> init_map_defaults){
		ChromeTask.registerBuilder( clazz, url_template, init_map_class, init_map_defaults );
	}
}
