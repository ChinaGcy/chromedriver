package com.sdyk.ai.crawler.zbj.task.scanTask;

import com.sdyk.ai.crawler.zbj.task.Task;
import com.sdyk.ai.crawler.zbj.task.modelTask.WorkTask;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
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

		this.addDoneCallback(() -> {
			String src = getResponse().getText();

			List<Task> tasks = new ArrayList<>();

			//http://shop.zbj.com/works/detail-wid-131609.html
			Pattern pattern = Pattern.compile("https://shop.zbj.com/works/detail-wid-\\d+.html");
			Matcher matcher = pattern.matcher(src);
			Pattern pattern_tp = Pattern.compile("https://shop.tianpeng.com/works/detail-wid-\\d+.html");
			Matcher matcher_tp = pattern_tp.matcher(src);

			List<String> list = new ArrayList<>();

			while (matcher.find()) {

				String new_url = matcher.group();

				if(!list.contains(url)) {
					list.add(url);
					try {
						tasks.add(new WorkTask(new_url, userId));
					} catch (MalformedURLException | URISyntaxException e) {
						e.printStackTrace();
					}
				}
			}

			while (matcher_tp.find()) {

				String new_url = matcher_tp.group();

				if(!list.contains(new_url)) {
					list.add(new_url);
					try {
						tasks.add(new WorkTask(new_url, userId));
					} catch (MalformedURLException | URISyntaxException e) {
						e.printStackTrace();
					}
				}
			}

			if (pageTurning("div.pagination > ul > li", page)) {
				//http://shop.zbj.com/18115303/works-p2.html
				Task t = WorkScanTask.generateTask("https://shop.zbj.com/" + this.getParamString("userId") + "/works", page + 1);
				if (t != null) {
					t.setPriority(Priority.HIGH);
					tasks.add(t);
				}
			}

			for(Task t : tasks) {
				ChromeDriverRequester.getInstance().submit(t);
			}
		});

	}

}
