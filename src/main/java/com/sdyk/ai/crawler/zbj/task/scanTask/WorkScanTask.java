package com.sdyk.ai.crawler.zbj.task.scanTask;

import com.sdyk.ai.crawler.zbj.task.Task;
import com.sdyk.ai.crawler.zbj.task.modelTask.WorkTask;
import org.openqa.selenium.WebDriver;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 服务商案例列表
 * 1. 找到url
 * 2. 翻页
 */
public class WorkScanTask extends ScanTask {

	public static WorkScanTask generateTask(String header, int page) {

		String url = header + "works-p" + page + ".html";
		String userId = header.split("/")[3];

		try {
			WorkScanTask t = new WorkScanTask(url, page, userId);
			return t;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return null;
	}

	public WorkScanTask(String url, int page, String userId) throws MalformedURLException, URISyntaxException {
		super(url);
		this.setParam("page", page);
		this.setParam("userId", userId);

	}

	public List<Task> postProc(WebDriver driver) throws Exception {

		String src = getResponse().getText();

		List<Task> tasks = new ArrayList<>();

		int page = this.getParamInt("page");

		String userId = this.getParamString("userId");

		//http://shop.zbj.com/works/detail-wid-131609.html
		Pattern pattern = Pattern.compile("http://shop.zbj.com/works/detail-wid-\\d+.html");
		Matcher matcher = pattern.matcher(src);
		Pattern pattern_tp = Pattern.compile("http://shop.tianpeng.com/works/detail-wid-\\d+.html");
		Matcher matcher_tp = pattern_tp.matcher(src);

		List<String> list = new ArrayList<>();

		while (matcher.find()) {

			String url = matcher.group();

			if(!list.contains(url)) {
				list.add(url);
				tasks.add(new WorkTask(url,userId));
			}
		}

		while (matcher_tp.find()) {

			String url = matcher_tp.group();

			if(!list.contains(url)) {
				list.add(url);
				tasks.add(new WorkTask(url,userId));
			}
		}

		if (pageTurning(driver, "body > div.prod-bg.clearfix > div > div.pagination > ul", page)) {
			//http://shop.zbj.com/18115303/works-p2.html
			Task t = WorkScanTask.generateTask("http://shop.zbj.com/" + this.getParamString("userId") + "/works", ++page);
			if (t != null) {
				t.setPrior();
				tasks.add(t);
			}
		}
		return tasks;
	}

}
