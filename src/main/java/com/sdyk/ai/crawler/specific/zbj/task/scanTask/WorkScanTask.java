package com.sdyk.ai.crawler.specific.zbj.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.CaseTask;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.WorkTask;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;

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

	static {
		registerBuilder(
				ProjectSuccessSacnTask.class,
				"https://www.zbj.com/{{channel}}/pk{page}.html",
				ImmutableMap.of("channel", String.class,"page", String.class),
				ImmutableMap.of("channel", "all", "page", "0"),
				false,
				Priority.MEDIUM
		);
	}

	public static List<String> list = new ArrayList<>();

	public WorkScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setBuildDom();

		this.addDoneCallback((t) -> {

			String user_id = null;
			int page = 0;
			Pattern pattern_url = Pattern.compile("http://shop.zbj.com/(?<userId>.+?)\\/works-p(?<page>.+?).html");
			Matcher matcher_url = pattern_url.matcher(url);
			if (matcher_url.find()) {
				user_id = matcher_url.group("userId");
				page = Integer.parseInt(matcher_url.group("page"));
			}


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
						ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

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
		return new TaskTrace(this.getClass(), this.getParamString("userId"), this.getParamString("page"));
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
					ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

					//提交任务
					ChromeDriverDistributor.getInstance().submit(holder);

				} catch (Exception e) {

					logger.error("error for submit WorkTask.class", e);
				}

			}
		}
	}
}
