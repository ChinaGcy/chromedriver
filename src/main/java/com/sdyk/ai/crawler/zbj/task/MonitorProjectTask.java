package com.sdyk.ai.crawler.zbj.task;

import com.sdyk.ai.crawler.zbj.ChromeDriverLoginWrapper;
import org.openqa.selenium.WebDriver;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 监控任务
 */
public class MonitorProjectTask extends Task{

	public static List<String> oldURL = new ArrayList<>();

	public MonitorProjectTask(String url) throws MalformedURLException, URISyntaxException {
		super(url);
	}

	public List<Task> postProc(WebDriver driver) {

		String src = getResponse().getText();

		List<Task> tasks = new ArrayList<>();

		int page = this.getParamInt("page");

		Pattern pattern = Pattern.compile("http://task.zbj.com/\\d+/");
		Matcher matcher = pattern.matcher(src);

		List<String> list = new ArrayList<>();

		while (matcher.find()) {

			String url = matcher.group();

			// 去重
			if(!list.contains(url)) {
				list.add(url);
			}
		}

		// 判断是否有新的项目发布
		for (String url : list) {

			if (!oldURL.contains(url)) {
				try {
					tasks.add(new ProjectTask(url));
				} catch (MalformedURLException | URISyntaxException e) {
					logger.error("MonitorProjectTest add Task error {}", e);
				}
			}

		}
		// 更新项目地址
		oldURL = list;

		return tasks;
	}
}
