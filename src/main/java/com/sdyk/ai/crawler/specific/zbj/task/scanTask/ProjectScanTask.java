package com.sdyk.ai.crawler.specific.zbj.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.TendererOrderTask;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.TendererRatingTask;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取项目信息
 * 1. 登录
 * 2. 找到url
 * 3. 翻页
 */
public class ProjectScanTask extends ScanTask {

	static {
		/*// init_map_class
		init_map_class = ImmutableMap.of("channel", String.class,"page", String.class);
		// init_map_defaults
		init_map_defaults = ImmutableMap.of("channel", "all", "page", "1");
		// url_template
		url_template = "http://task.zbj.com/{{channel}}/p{{page}}s5.html?o=1";

		need_login = false;

		base_priority = Priority.HIGH;*/
		registerBuilder(
				ProjectScanTask.class,
				"http://task.zbj.com/{{channel}}/p{{page}}s5.html?o=1",
				ImmutableMap.of("channel", String.class,"page", String.class),
				ImmutableMap.of("channel", "all", "page", "1")
		);
	}

	public static String domain() throws MalformedURLException, URISyntaxException {
		return "zbj.com";
	}

	public static List<String> list = new ArrayList<>();



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

						if (!list.contains(project_id)) {
							list.add(project_id);
							/*HttpTaskPoster.getInstance().submit(ProjectTask.class,
									ImmutableMap.of("project_id", project_id));*/

							ChromeDriverDistributor.getInstance().submit(
									this.getHolder(
											ProjectTask.class,
											ImmutableMap.of("project_id", project_id)
											));

							/*for (String key : ImmutableMap.of("project_id", project_id).keySet()) {
								if(ProjectTask.init_map_class.containsKey(key)) {
									System.err.println("123456789");
								}else {
									System.err.println("********************");

								}
							}*/
						}
						} catch(Exception e){
							logger.error(e);
						}
				}

				// 判断翻页
				if (pageTurning("body > div.grid.grid-inverse > div.main-wrap > div > div > div.tab-switch.tab-progress > div > div.pagination > ul > li", page)) {
					/*HttpTaskPoster.getInstance().submit(ProjectScanTask.class,
							ImmutableMap.of("channel", channel, "page", String.valueOf(++page)));*/
					ChromeDriverDistributor.getInstance().submit(
							this.getHolder(
									ProjectScanTask.class,
									ImmutableMap.of("channel", channel, "page", String.valueOf(++page))));

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
