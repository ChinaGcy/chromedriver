package com.sdyk.ai.crawler.zbj.task;

import com.sdyk.ai.crawler.zbj.ChromeDriverWithLogin;
import com.sdyk.ai.crawler.zbj.Crawler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.tfelab.db.Refacter;
import org.tfelab.io.requester.BasicRequester;
import org.tfelab.io.requester.account.AccountWrapper;
import org.tfelab.io.requester.account.AccountWrapperImpl;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;
import org.tfelab.io.requester.chrome.ChromeDriverRequester;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.PrivateKey;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取项目信息
 * 1. 登录 TODO 待实现
 * 2. 找到url
 * 3. 翻页
 */
public class ProjectScanTask extends Task {

	private static final Logger logger = LogManager.getLogger(ProjectScanTask.class.getName());

	private static String url_;

	public static ProjectScanTask generateTask(String url_, int page, AccountWrapper aw) {

		ProjectScanTask.url_ = url_;

		if(page >= 100) return null;

		String url = "http://task.zbj.com/" + url_ + "/p" + page + "s5.html";

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
	}

	/**
	 *
	 * @return
	 */
	public List<Task> postProc(WebDriver driver) throws Exception {

		String src = getResponse().getText();

		List<Task> tasks = new ArrayList<>();

		int page = this.getParamInt("page");

		Pattern pattern = Pattern.compile("http://task.zbj.com/\\d+/");
		Matcher matcher = pattern.matcher(src);

		List<String> list = new ArrayList<>();

		while (matcher.find()) {

			String url = matcher.group();

			if(!list.contains(url)) {

				list.add(url);
				tasks.add(new ProjectTask(url));
			}
		}

		if (list.size() == 40) {

			Task t = generateTask(url_, ++page, null);
			if (t != null) {
				t.setPrior();
				tasks.add(t);
			}
		}

		logger.info("Task num: {}", tasks.size());

		return tasks;
	}

	/**
	 * 测试方法
	 */
	public static void main(String[] args) throws Exception {

		ChromeDriverAgent agent = (new ChromeDriverWithLogin("zbj.com")).login();
		Queue<Task> taskQueue = new LinkedList<>();
		taskQueue.add(ProjectScanTask.generateTask("t-ydyykf",1,null));

		while(!taskQueue.isEmpty()) {
			Task t = taskQueue.poll();
			if(t != null) {
				try {
					agent.fetch(t);
					for (Task t_ : t.postProc(agent.getDriver())) {
						//taskQueue.add(t_);
						agent.fetch(t_);
					}

				} catch (Exception e) {
					logger.error("Exception while fetch task. ", e);
					taskQueue.add(t);
				}
			}
		}
	}
}
