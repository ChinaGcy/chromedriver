package com.sdyk.ai.crawler.zbj.task.scanTask;

import com.sdyk.ai.crawler.zbj.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.zbj.task.Task;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;
import one.rewind.io.requester.chrome.ChromeDriverRequester;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取项目信息
 * 1. 登录 TODO 待实现
 * 2. 找到url
 * 3. 翻页
 */
public class ProjectScanTask extends ScanTask {

	/**
	 * 生成项目翻页采集任务
	 * @param channel
	 * @param page
	 * @return
	 */
	public static ProjectScanTask generateTask(String channel, int page) {

		String url = "http://task.zbj.com/" + channel + "/p" + page + "s5.html?o=7";

		try {
			ProjectScanTask t = new ProjectScanTask(url, page, channel);
			t.setRequester_class(ChromeDriverRequester.class.getSimpleName());
			return t;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 *
	 * @param url
	 * @param page
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public ProjectScanTask(String url, int page, String channel) throws MalformedURLException, URISyntaxException {

		super(url);

		// 设置优先级
		this.setPriority(Priority.HIGH);
		this.setBuildDom();

		this.addDoneCallback(() -> {

			try {

				logger.info("Extract content: {}", getUrl());

				String src = getResponse().getText();

				// 生成任务
				Map<String, Task> tasks = new HashMap<>();

				Pattern pattern = Pattern.compile("task.zbj.com/\\d+/");
				Matcher matcher = pattern.matcher(src);

				while (matcher.find()) {

					String new_url = "http://" + matcher.group();

					try {
						tasks.put(new_url, new ProjectTask(new_url));
					} catch (Exception e) {
						logger.error(e);
					}
				}

				if (pageTurning("div.pagination > ul > li", page)) {

					Task next_t = generateTask(channel, page + 1);

					logger.info("Next page: {}", next_t.getUrl());

					if (next_t != null) {
						next_t.setBuildDom();
						next_t.setPriority(Priority.HIGH);

						try {
							tasks.put(next_t.getUrl(), next_t);
						} catch (Exception e) {
							logger.error(e);
						}
					}
				}

				logger.info("Task num: {}", tasks.size());

				for (Task t : tasks.values()) {
					ChromeDriverRequester.getInstance().submit(t);
				}

			} catch (Exception e) {
				logger.error(e);
			}

		});
	}
}
