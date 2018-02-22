package com.sdyk.ai.crawler.zbj.task;

import org.openqa.selenium.WebDriver;
import org.tfelab.io.requester.account.AccountWrapper;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;
import org.tfelab.io.requester.chrome.ChromeDriverRequester;

import javax.management.Query;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 服务商案例列表
 * 1. 找到url
 * 2. 翻页
 */
public class WorkScanTask extends Task {

	public static WorkScanTask generateTask(String url_, int page) {

		String url = url_ + "works-p" + page + ".html";
		String userId = url_.split("/")[3];

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
		Pattern pattern1 = Pattern.compile("http://shop.tianpeng.com/works/detail-wid-\\d+.html");
		Matcher matcher1 = pattern1.matcher(src);

		List<String> list = new ArrayList<>();

		while (matcher.find()) {

			String url = matcher.group();

			if(!list.contains(url)) {
				list.add(url);
				tasks.add(new WorkTask(url,userId));
			}
		}
		while (matcher1.find()) {

			String url = matcher1.group();

			if(!list.contains(url)) {
				list.add(url);
				tasks.add(new WorkTask(url,userId));
			}
		}


		if (list.size() == 12) {
			//http://shop.zbj.com/18115303/works-p2.html
			Task t = WorkScanTask.generateTask("http://shop.zbj.com/" + this.getParamString("userId") + "/works", ++page);



			if (t != null) {
				t.setPrior();
				tasks.add(t);
			}
		}
		return tasks;
	}

	public static void main(String[] args) throws Exception {
		ChromeDriverAgent agent = new ChromeDriverAgent();
		Queue<Task> queue = new LinkedBlockingQueue<>();

		queue.add(WorkScanTask.generateTask("http://shop.zbj.com/16123923/", 1));

		while(!queue.isEmpty()) {

			Task t = queue.poll();

			agent.fetch(t);

			for (Task task : t.postProc(agent.getDriver())) {

				queue.add(task);
			}

		}
	}


}
