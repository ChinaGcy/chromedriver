package com.sdyk.ai.crawler.zbj.task.scanTask;

import com.sdyk.ai.crawler.zbj.task.modelTask.CaseTask;
import com.sdyk.ai.crawler.zbj.task.Task;
import org.openqa.selenium.WebDriver;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 乙方项目列表
 * 1. 找到url
 * 2. 翻页
 */
public class CaseScanTask extends ScanTask {

	public static List<String> list = new ArrayList<>();

	//   http://shop.zbj.com/7523816/
	public static CaseScanTask generateTask(String header, int page) {

		String url = header + "servicelist-p" + page + ".html";

		try {
			CaseScanTask t = new CaseScanTask(url, page);
			return t;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;
	}

	public CaseScanTask(String url, int page) throws MalformedURLException, URISyntaxException {
		super(url);
		this.setParam("page", page);
	}

	public List<Task> postProc() throws Exception {

		String src = getResponse().getText();

		// http://shop.zbj.com/17788555/servicelist-p1.html
		String webId = this.getUrl().split("/")[3];

		List<Task> tasks = new ArrayList<>();

		int page = this.getParamInt("page");

		// 判断是否翻页
		if (!src.contains("暂时还没有此类服务！") && backtrace) {
			Task t = generateTask("https://shop.zbj.com/" + webId + "/", ++page);
			if (t != null) {
				t.setPriority(Priority.HIGH);
				tasks.add(t);
			}
		}

		// 获取猪八戒， 天蓬网的服务地址
		Pattern pattern = Pattern.compile("http://shop.zbj.com/\\d+/sid-\\d+.html");
		Matcher matcher = pattern.matcher(src);
		Pattern pattern_tp = Pattern.compile("http://shop.tianpeng.com/\\d+/sid-\\d+.html");
		Matcher matcher_tp = pattern_tp.matcher(src);

		// 猪八戒url
		while (matcher.find()) {

			String url = matcher.group();

			if(!list.contains(url)) {
				list.add(url);
				tasks.add(new CaseTask(url));
			}
		}

		// 天蓬网url
		while (matcher_tp.find()) {

			String url = matcher_tp.group();

			if(!list.contains(url)) {
				list.add(url);
				tasks.add(new CaseTask(url));
			}
		}

		return tasks;
	}
}
