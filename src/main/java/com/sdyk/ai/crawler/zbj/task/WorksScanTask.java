package com.sdyk.ai.crawler.zbj.task;

import org.openqa.selenium.WebDriver;
import org.tfelab.io.requester.account.AccountWrapper;
import org.tfelab.io.requester.chrome.ChromeDriverRequester;

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
public class WorksScanTask extends Task {

	public static WorksScanTask generateTask(String url_, int page, String userId, AccountWrapper aw) {

		String url = url_ + "-p" + page + ".html";

		try {
			WorksScanTask t = new WorksScanTask(url, page, userId);
			t.setRequester_class(ChromeDriverRequester.class.getSimpleName());
			t.setAccount(aw);
			return t;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return null;
	}

	//http://shop.zbj.com/works/detail-wid-131609.html
	public WorksScanTask(String url, int page, String userId) throws MalformedURLException, URISyntaxException {
		super(url);
		this.setParam("page", page);
		this.setParam("userId", userId);

	}

	public List<Task> postProc(WebDriver driver) throws Exception {

		String src = getResponse().getText();

		List<Task> tasks = new ArrayList<>();

		int page = this.getParamInt("page");
		String userId = this.getParamString("userId");

		Pattern pattern = Pattern.compile("http://shop.zbj.com/works/detail-wid-\\d.html");
		Matcher matcher = pattern.matcher(src);

		List<String> list = new ArrayList<>();

		while (matcher.find()) {

			String url = matcher.group();

			if(!list.contains(url)) {

				list.add(url);
				tasks.add(new WorkTask(url,userId));
			}
		}


		Task t = generateTask(this.getUrl(), ++ page, userId,null);
		if(t != null) {
			t.setPrior();
			tasks.add(t);
		}


		return tasks;
	}


}
