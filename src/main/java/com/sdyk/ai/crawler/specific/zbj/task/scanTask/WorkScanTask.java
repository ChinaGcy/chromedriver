package com.sdyk.ai.crawler.specific.zbj.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.CaseTask;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.WorkTask;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskFactory;
import one.rewind.io.requester.task.TaskHolder;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 乙方案例列表
 * 1. 找到url
 * 2. 翻页
 */
public class WorkScanTask extends ScanTask {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				WorkScanTask.class,
				"https://shop.zbj.com/{{user_id}}/works-p{{page}}.html",
				ImmutableMap.of("user_id", String.class,"page", String.class),
				ImmutableMap.of("user_id", "0", "page", "1"),
				false,
				Priority.MEDIUM
		);
	}

	String user_id;
	String page_;

	public static List<String> list = new ArrayList<>();

	public WorkScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setBuildDom();

		this.addDoneCallback((t) -> {



			String userId = null;
			int page = 0;
			Pattern pattern_url = Pattern.compile("https://shop.zbj.com/(?<userId>\\d+)/works-p(?<page>\\d+).html");
			Matcher matcher_url = pattern_url.matcher(url);
			if (matcher_url.find()) {
				userId = matcher_url.group("userId");
				page = Integer.parseInt(matcher_url.group("page"));
			}

			user_id = userId;

			page_ = String.valueOf(page);
			String src = getResponse().getText();

			if (src.contains(" 暂无项目案例")) {
				return;
			}
			//http://shop.zbj.com/works/detail-wid-131609.html
			Pattern pattern = Pattern.compile("http://shop.zbj.com/works/detail-wid-\\d+.html");
			Matcher matcher = pattern.matcher(src);
			Pattern pattern_tp = Pattern.compile("http://shop.tianpeng.com/works/detail-wid-\\d+.html");
			Matcher matcher_tp = pattern_tp.matcher(src);

			getWorkUrl(matcher);

			getWorkUrl(matcher_tp);

			// body > div.prod-bg.clearfix > div > div.pagination > ul > li
			if (pageTurning("body > div.prod-bg.clearfix > div > div.pagination > ul > li", page)) {
				//http://shop.zbj.com/18115303/works-p2.html
				try {

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					ImmutableMap.of("user_id", user_id, "page", String.valueOf(++page));

					Class<? extends ChromeTask> clazz = (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.zbj.task.scanTask.WorkScanTask");

					//生成holder
					TaskHolder holder = ChromeTaskFactory.getInstance().newHolder(clazz, init_map);

					//提交任务
					ChromeDriverDistributor.getInstance().submit(holder);

				} catch (Exception e) {

					logger.error("error for submit WorkScanTask.class", e);
				}
			}
		});

	}

	@Override
	public TaskTrace getTaskTrace() {
		return new TaskTrace(this.getClass(), user_id, page_);
	}

	/**
	 * 获取work任务
	 * @param matcher
	 */
	public void getWorkUrl(Matcher matcher) {

		while (matcher.find()) {

			String new_url = matcher.group();

			String work_webId = new_url.split("/")[4]
					.replace("detail-wid-","")
					.replace(".html","");

			if (!list.contains(new_url)) {
				list.add(new_url);

				try {

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					ImmutableMap.of("work_webId", work_webId);

					Class<? extends ChromeTask> clazz = (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.zbj.task.modelTask.WorkTask");

					//生成holder
					TaskHolder holder = ChromeTaskFactory.getInstance().newHolder(clazz, init_map);

					//提交任务
					ChromeDriverDistributor.getInstance().submit(holder);

				} catch (Exception e) {

					logger.error("error for submit WorkTask.class", e);
				}

			}
		}
	}
}
