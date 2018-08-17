package com.sdyk.ai.crawler.specific.zbj.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.CaseTask;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
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
 * 乙方服务列表
 * 1. 找到url
 * 2. 翻页
 */
public class CaseScanTask extends ScanTask {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				CaseScanTask.class,
				"https://shop.zbj.com/{{user_id}}/servicelist-p{{page}}.html",
				ImmutableMap.of("user_id", String.class,"page", String.class),
				ImmutableMap.of("user_id", "0", "page", "0"),
				false,
				Priority.MEDIUM
		);
	}

	public static List<String> list = new ArrayList<>();

	String user_Id;

	/**
	 *
	 * @param url
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public CaseScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setBuildDom();

		// http://shop.zbj.com/17788555/servicelist-p1.html
		this.addDoneCallback((t) -> {

			String userId = null;
			int page = 0;
			Pattern pattern_url = Pattern.compile("https://shop.zbj.com/(?<userId>.+?)\\/servicelist-p(?<page>.+?).html");
			Matcher matcher_url = pattern_url.matcher(url);
			if (matcher_url.find()) {
				userId = matcher_url.group("userId");
				page = Integer.parseInt(matcher_url.group("page"));
			}

			user_Id = userId;
			try {

				String src = getResponse().getText();

				// 判断是否翻页
				if (src.contains("暂时还没有此类服务")) {
					return;
				}

				// 获取猪八戒， 天蓬网的服务地址
				Pattern pattern = Pattern.compile("http://shop.zbj.com/\\d+/sid-\\d+.html");
				Matcher matcher = pattern.matcher(src);
				Pattern pattern_tp = Pattern.compile("http://shop.tianpeng.com/\\d+/sid-\\d+.html");
				Matcher matcher_tp = pattern_tp.matcher(src);

				getCaseUrl(matcher, userId);

				getCaseUrl(matcher_tp, userId);

			} catch (Exception e) {
				logger.error(e);
			}

			if (pageTurning("#contentBox > div > div.pagination > ul > li", page)) {

				try {

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					ImmutableMap.of("user_id", userId, "page", String.valueOf(++page));

					Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.zbj.task.scanTask.CaseScanTask");

					//生成holder
					TaskHolder holder = this.getHolder(clazz, init_map);

					//提交任务
					ChromeDriverDistributor.getInstance().submit(holder);

				} catch ( Exception e) {

					logger.error("error for submit CaseScanTask.class", e);
				}

			}
		});
	}

	@Override
	public TaskTrace getTaskTrace() {
		return new TaskTrace(this.getClass(), user_Id, this.getParamString("page"));
	}

	/**
	 * 添加Case任务
	 * @param matcher
	 * @param userId
	 */
	public void getCaseUrl(Matcher matcher, String userId) {
		// 猪八戒url
		while (matcher.find()) {

			String new_url = matcher.group();

			String case_id = new_url.split("/")[4]
					.replace("sid-", "")
					.replace(".html", "");

			if (!list.contains(new_url)) {
				list.add(new_url);

				try {

					try {

						//设置参数
						Map<String, Object> init_map = new HashMap<>();
						ImmutableMap.of("user_id", userId, "case_id", case_id);

						Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.zbj.task.modelTask.CaseTask");

						//生成holder
						TaskHolder holder = this.getHolder(clazz, init_map);

						//提交任务
						ChromeDriverDistributor.getInstance().submit(holder);

					} catch ( Exception e) {

						logger.error("error for submit CaseTask.class", e);
					}


				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}
}
