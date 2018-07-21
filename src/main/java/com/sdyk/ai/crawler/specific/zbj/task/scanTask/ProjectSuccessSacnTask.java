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

public class ProjectSuccessSacnTask extends ScanTask {

	static {
		registerBuilder(
				ProjectSuccessSacnTask.class,
				"http://task.zbj.com/{{channel}}/p{{page}}s5.html?o=1",
				ImmutableMap.of("channel", String.class,"page", String.class),
				ImmutableMap.of("channel", "all", "page", "0"),
				false,
				Priority.MEDIUM
		);
	}

	/**
	 *
	 * @param url
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public ProjectSuccessSacnTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		// 设置优先级
		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			String channel = null;
			int page = 0;
			Pattern pattern_url = Pattern.compile("http://task.zbj.com/(?<channel>.+?)/p(?<page>.+?)s5.html?o=1");
			Matcher matcher_url = pattern_url.matcher(url);
			if (matcher_url.find()) {
				channel = matcher_url.group("channel");
				page = Integer.parseInt(matcher_url.group("page"));
			}

			try {

				logger.info("Extract content: {}", getUrl());

				String src = getResponse().getText();

				// 生成任务
				Map<String, com.sdyk.ai.crawler.task.Task> tasks = new HashMap<>();

				Pattern pattern = Pattern.compile("http://task.zbj.com/\\d+");
				Matcher matcher = pattern.matcher(src);

				while (matcher.find()) {

					String project_id = matcher.group("projectId");

					try {

						//设置参数
						Map<String, Object> init_map = new HashMap<>();
						ImmutableMap.of("project_id", project_id);

						Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.zbj.task.modelTask.ProjectTask");

						//生成holder
						ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

						//提交任务
						ChromeDriverDistributor.getInstance().submit(holder);

					} catch ( Exception e) {

						logger.error("error for submit ProjectTask.class", e);
					}

				}

				// body > div.grid.grid-inverse > div.main-wrap > div > div.list-footer > div > ul
				if (pageTurning("body > div.grid.grid-inverse > div.main-wrap > div > div.list-footer > div > ul > li", page)) {

					try {

						//设置参数
						Map<String, Object> init_map = new HashMap<>();
						ImmutableMap.of("channel", channel, "page", String.valueOf(++page));

						Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.zbj.task.modelTask.ProjectTask");

						//生成holder
						ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

						//提交任务
						ChromeDriverDistributor.getInstance().submit(holder);

					} catch ( Exception e) {

						logger.error("error for submit ProjectTask.class", e);
					}

				}

				logger.info("Task driverCount: {}", tasks.size());

			} catch (Exception e) {
				logger.error(e);
			}

		});
	}

	@Override
	public TaskTrace getTaskTrace() {
		return null;
	}

}
