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
 * 乙方项目列表
 * 1. 找到url
 * 2. 翻页
 */
public class CaseScanTask extends Task {


	public static ProjectScanTask generateTask(String url_, int page, AccountWrapper aw) {

		if(page >= 100) return null;

		String url = url_ + "/servicelist-" + page + "p/.html";

		try {
			ProjectScanTask t = new ProjectScanTask(url, page);
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


	public CaseScanTask(String url, String page) throws MalformedURLException, URISyntaxException {
		super(url);
	}

	public List<Task> postProc(WebDriver driver) throws Exception {
		String src = getResponse().getText();

		String webUrl = getUrl();

		List<Task> tasks = new ArrayList<>();

		int page = this.getParamInt("page");

		Pattern pattern = Pattern.compile("http://shop.zbj.com/\\d+/sid-\\d+.html");
		Matcher matcher = pattern.matcher(src);

		List<String> list = new ArrayList<>();

		while (matcher.find()) {

			String url = matcher.group();

			if(!list.contains(url)) {

				list.add(url);
				tasks.add(new CaseTask(url));
			}
		}

		Task t = generateTask(webUrl, ++ page, null);
		if(t != null) {
			t.setPrior();
			tasks.add(t);
		}

		return tasks;
	}
}
