package com.sdyk.ai.crawler.zbj.task;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.tfelab.io.requester.account.AccountWrapper;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 服务商列表
 * 1. 找到url
 * 2. 翻页
 */
public class ServiceScanTask extends Task {

	private static final Logger logger = LogManager.getLogger(ServiceScanTask.class.getName());

	public static ServiceScanTask generateTask(String tag, int page, AccountWrapper aw) {

		String url = "http://www.zbj.com/" + tag + "/pk" + page + ".html";

		try {
			ServiceScanTask t = new ServiceScanTask(url, page);
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
	public ServiceScanTask(String url, int page) throws MalformedURLException, URISyntaxException {

		super(url);
		this.setParam("page", page);
		this.priority = Priority.low;
	}

	/**
	 *
	 * @return
	 */
	public List<Task> postProc(WebDriver driver) throws Exception {

		String src = getResponse().getText();

		List<Task> tasks = new ArrayList<>();

		int page = this.getParamInt("page");

		Pattern pattern = Pattern.compile("//shop.zbj.com/\\d+/");
		Matcher matcher = pattern.matcher(src);

		List<String> list = new ArrayList<>();

		while (matcher.find()) {

			String url = "http:" + matcher.group();

			if(!list.contains(url)) {
				list.add(url);
				tasks.add(new ServiceSupplierTask(url));
			}
		}

		// 当前页数
		int i = (page-1)/40+1;
		// 翻页
		if (pageTurning(driver, "#utopia_widget_18 > div.pagination > ul", i)) {
			Task t = ServiceScanTask.generateTask(getUrl().split("/")[3],page + 40, null);
			tasks.add(t);
		}
		logger.info("Task num: {}", tasks.size());

		return tasks;
	}
}
