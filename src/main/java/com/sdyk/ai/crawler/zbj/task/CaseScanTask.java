package com.sdyk.ai.crawler.zbj.task;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.tfelab.io.requester.account.AccountWrapper;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;
import org.tfelab.io.requester.chrome.ChromeDriverRequester;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 乙方项目列表
 * 1. 找到url
 * 2. 翻页
 */
public class CaseScanTask extends Task {

	public static List<String> list = new ArrayList<>();

	//   http://shop.zbj.com/7523816/
	public static CaseScanTask generateTask(String url_, int page) {

		String url = url_ + "servicelist-p" + page + ".html";

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

	public List<Task> postProc(WebDriver driver) throws Exception {

		String src = getResponse().getText();

		// http://shop.zbj.com/17788555/servicelist-p1.html
		String webId = this.getUrl().split("/")[3];

		List<Task> tasks = new ArrayList<>();

		int page = this.getParamInt("page");

		if (!src.contains("暂时还没有此类服务！")) {
			Task t = generateTask("http://shop.zbj.com/" + webId + "/", ++page);
			if (t != null) {
				t.setPrior();
				tasks.add(t);
			}
		}

		Pattern pattern = Pattern.compile("http://shop.zbj.com/\\d+/sid-\\d+.html");
		Matcher matcher = pattern.matcher(src);
		Pattern pattern1 = Pattern.compile("http://shop.tianpeng.com/\\d+/sid-\\d+.html");
		Matcher matcher1 = pattern1.matcher(src);

		while (matcher.find()) {

			String url = matcher.group();

			if(!list.contains(url)) {
				list.add(url);
				tasks.add(new CaseTask(url));
			}
		}

		while (matcher1.find()) {

			String url = matcher1.group();

			if(!list.contains(url)) {
				list.add(url);
				tasks.add(new CaseTask(url));
			}
		}

		return tasks;
	}

	public static void main(String[] args) throws Exception {

		ChromeDriverAgent agent = new ChromeDriverAgent();

		Task t = CaseScanTask.generateTask("http://shop.zbj.com/17029968/",1);

		Queue<Task> queue = new LinkedBlockingQueue<>();

		queue.add(t);

		while(!queue.isEmpty()) {

			Task tt = queue.poll();

			agent.fetch(tt);

			for (Task t1 : tt.postProc(agent.getDriver())) {
				queue.add(t1);
			}

		}



	}

}
