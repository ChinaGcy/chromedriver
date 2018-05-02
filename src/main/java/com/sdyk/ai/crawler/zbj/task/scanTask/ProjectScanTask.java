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

	private static String channel;

	/**
	 * 生成项目翻页采集任务
	 * @param channel
	 * @param page
	 * @return
	 */
	public static ProjectScanTask generateTask(String channel, int page) {

		ProjectScanTask.channel = channel;

		if(page >= 100) return null;

		String url = "http://task.zbj.com/" + channel + "/p" + page + "s5.html?o=7";

		try {
			ProjectScanTask t = new ProjectScanTask(url, page);
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
	public ProjectScanTask(String url, int page) throws MalformedURLException, URISyntaxException {

		super(url);
		this.setParam("page", page);

		// 设置优先级
		this.setPriority(Priority.HIGH);
	}

	/**
	 *
	 * @return
	 */
	public List<Task> postProc() throws Exception {

		String src = getResponse().getText();

		Document document = getResponse().getDoc();

		List<Task> tasks = new ArrayList<>();

		int page = this.getParamInt("page");

		Pattern pattern = Pattern.compile("task.zbj.com/\\d+/");
		Matcher matcher = pattern.matcher(src);

		List<String> list = new ArrayList<>();

		while (matcher.find()) {
			String url = matcher.group();
			// 去重
			if(!list.contains(url)) {
				System.err.println(url);
				list.add(url);
				tasks.add(new ProjectTask("https://"+url));
			}
		}

		if (pageTurning("div.pagination > ul > li",page)) {
			Task t = generateTask(channel, ++page);
			if (t != null) {
				t.setPriority(Priority.HIGH);
				tasks.add(t);
			}
		}

		logger.info("Task num: {}", tasks.size());
		return tasks;
	}
}
