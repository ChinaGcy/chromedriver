package com.sdyk.ai.crawler.specific.zbj.task.scanTask;

import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;

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

		String url = "http://task.zbj.com/" + channel + "/p" + page + "s5.html?o=1";

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
		this.setParam("page", page);
		this.setParam("channel", channel);

		this.addDoneCallback(() -> {

			try {

				logger.info("Extract content: {}", getUrl());

				String src = getResponse().getText();

				// 生成任务
				Map<String, com.sdyk.ai.crawler.task.Task> tasks = new HashMap<>();

				Pattern pattern = Pattern.compile("task.zbj.com/\\d+");
				Matcher matcher = pattern.matcher(src);

				while (matcher.find()) {

					String new_url = "http://" + matcher.group();

					try {
						tasks.put(new_url, new ProjectTask(new_url));
					} catch (Exception e) {
						logger.error(e);
					}
				}

				if (pageTurning("body > div.grid.grid-inverse > div.main-wrap > div > div > div.tab-switch.tab-progress > div > div.pagination > ul > li", page)) {

					com.sdyk.ai.crawler.task.Task next_t = generateTask(channel, page + 1);

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

				logger.info("Task driverCount: {}", tasks.size());

				for (com.sdyk.ai.crawler.task.Task t : tasks.values()) {
					ChromeDriverRequester.getInstance().submit(t);
				}

			} catch (Exception e) {
				logger.error(e);
			}

		});
	}

	@Override
	public TaskTrace getTaskTrace() {
		return new TaskTrace(this.getClass(), this.getParamString("channel"), this.getParamString("page"));
	}

	@Override
	public one.rewind.io.requester.Task validate() throws ProxyException.Failed, AccountException.Failed, AccountException.Frozen {
		return null;
	}
}
