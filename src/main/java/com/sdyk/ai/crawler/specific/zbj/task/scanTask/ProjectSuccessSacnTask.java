package com.sdyk.ai.crawler.specific.zbj.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.ProjectTask;
import one.rewind.io.requester.exception.ProxyException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectSuccessSacnTask extends ScanTask {

	static {
		/*// init_map_class
		init_map_class = ImmutableMap.of("channel", String.class,"page", String.class);
		// init_map_defaults
		init_map_defaults = ImmutableMap.of("channel", "all", "page", "0");
		// url_template
		url_template = "http://task.zbj.com/{{channel}}/p{{page}}s5.html?o=1";*/
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
						HttpTaskPoster.getInstance().submit(ProjectTask.class,
								ImmutableMap.of("project_id", project_id));

					} catch (Exception e) {
						logger.error(e);
					}
				}

				// body > div.grid.grid-inverse > div.main-wrap > div > div.list-footer > div > ul
				if (pageTurning("body > div.grid.grid-inverse > div.main-wrap > div > div.list-footer > div > ul > li", page)) {

					HttpTaskPoster.getInstance().submit(ProjectTask.class,
							ImmutableMap.of("channel", channel, "page", String.valueOf(++page)));
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
