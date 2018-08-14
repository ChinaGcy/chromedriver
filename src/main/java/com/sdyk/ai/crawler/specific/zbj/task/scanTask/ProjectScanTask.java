package com.sdyk.ai.crawler.specific.zbj.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.ProjectTask;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取项目信息
 * 1. 登录
 * 2. 找到url
 * 3. 翻页
 */
public class ProjectScanTask extends ScanTask {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		// TODO 全部抓取是否需要在url中添加分类 channel
		registerBuilder(
				ProjectScanTask.class,
				"https://task.zbj.com/page{{page}}.html",
				ImmutableMap.of("page", String.class),
				ImmutableMap.of("page", "1"),
				false,
				Priority.MEDIUM
		);
	}

	String page_;

	/**
	 *
	 * @param url
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public ProjectScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);


		// 设置优先级
		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			int page = 0;
			Pattern pattern_url = Pattern.compile("task.zbj.com/page(?<page>\\d+).html");
			Matcher matcher_url = pattern_url.matcher(url);
			if (matcher_url.find()) {
				page = Integer.parseInt(matcher_url.group("page"));
			}

			page_ = page + "";

			try {

				logger.info("Extract content: {}", getUrl());

				String src = getResponse().getText();

				// 生成任务
				Pattern pattern = Pattern.compile("//task.zbj.com/(?<projectId>\\d+)/");
				Matcher matcher = pattern.matcher(src);

				while (matcher.find()) {

					String project_id = matcher.group("projectId");

					try {

						try {

							//设置参数
							Map<String, Object> init_map  = ImmutableMap.of("project_id", project_id);

							Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.zbj.task.modelTask.ProjectTask");

							//生成holder
							ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

							//提交任务
							ChromeDriverDistributor.getInstance().submit(holder);

						} catch ( Exception e) {

							logger.error("error for submit ProjectTask.class", e);
						}


					} catch (Exception e) {
						logger.error(e);
					}
				}

				// 判断翻页
				if (pageTurning("#utopia_widget_6 > div.trade-list-paging.clearfix > div > ul > li", page)) {

					try {

						//设置参数
						Map<String, Object> init_map = ImmutableMap.of("page", String.valueOf(++page));

						Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.zbj.task.scanTask.ProjectScanTask");

						//生成holder
						ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

						//提交任务
						ChromeDriverDistributor.getInstance().submit(holder);

					} catch ( Exception e) {

						logger.error("error for submit ProjectScanTask.class", e);
					}

				}

			}catch (Exception e) {
				logger.error("projectScanTask ERROR {}", e);
			}
 		});
	}

	@Override
	public TaskTrace getTaskTrace() {
		return new TaskTrace(this.getClass(), "task", page_);
	}

}
