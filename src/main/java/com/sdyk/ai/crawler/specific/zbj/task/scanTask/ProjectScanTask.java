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
 * 1. 登录 TODO 待实现
 * 2. 找到url
 * 3. 翻页
 */
public class ProjectScanTask extends ScanTask {

	static {
		registerBuilder(
				ProjectScanTask.class,
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
	 * @param i
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public ProjectScanTask(String url, int i) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		// 设置优先级
		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			String channel = null;
			int page = 0;
			Pattern pattern_url = Pattern.compile("http://task.zbj.com/(?<channel>.+?)/p(?<page>.+?)s5.html\\?o=1");
			Matcher matcher_url = pattern_url.matcher(getUrl());
			if (matcher_url.find()) {
				channel = matcher_url.group("channel");
				page = Integer.parseInt(matcher_url.group("page"));

			}

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


					} catch (Exception e) {
						logger.error(e);
					}
				}

				// 判断翻页
				if (pageTurning("body > div.grid.grid-inverse > div.main-wrap > div > div > div.tab-switch.tab-progress > div > div.pagination > ul > li", page)) {

					try {

						//设置参数
						Map<String, Object> init_map = new HashMap<>();
						ImmutableMap.of("channel", channel, "page", String.valueOf(++page));

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
		return new TaskTrace(this.getClass(), this.getParamString("channel"), this.getParamString("page"));
	}

}
