package com.sdyk.ai.crawler.zbj.requester;

import com.sdyk.ai.crawler.zbj.model.Account;
import com.sdyk.ai.crawler.zbj.model.Proxy;
import com.sdyk.ai.crawler.zbj.task.scanTask.ServiceScanTask;
import com.sdyk.ai.crawler.zbj.task.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * 调用ChromeDriver
 */
public class ChromeRequester {

	protected static ChromeRequester instance;

	private static final Logger logger = LogManager.getLogger(ChromeRequester.class.getName());

	// 开启线程数，开启几个ChromeDriver
	private int agentCount = 4;

	public String domain = "zbj.com";

	//
	private List<ChromeDriverLoginWrapper> agents = new LinkedList<>();

	/**
	 * 单例模式
	 *
	 * @return
	 */
	public static ChromeRequester getInstance() {

		if (instance == null) {

			synchronized (ChromeRequester.class) {
				if (instance == null) {
					instance = new ChromeRequester();
				}
			}
		}

		return instance;
	}

	/**
	 *
	 */
	private ChromeRequester() {


		for(int i=0; i<agentCount; i++) {

			ChromeDriverLoginWrapper agent = new ChromeDriverLoginWrapper(domain);

			try {

				// 登录
				agent.login(Account.getAccountByDomain(domain), Proxy.getValidProxy("aliyun"));

				// 执行任务
				agent.start();

				agents.add(agent);

			} catch (Exception e) {

				logger.error("Error while chrome login. ", e);
			}
		}
	}

	/**
	 *最短队列的chrome
	 * @return
	 */
	private ChromeDriverLoginWrapper getDriverWithShortestQueue() {
		int size = Integer.MAX_VALUE;
		ChromeDriverLoginWrapper agent_ = null;
		for(ChromeDriverLoginWrapper agent : agents) {
			if(agent.taskQueue.size() < size) {
				size = agent.taskQueue.size();
				agent_ = agent;
			}
		}

		return agent_;
	}

	/**
	 * 添加任务
	 * @param task
	 */
	public void distribute(Task task) {

		ChromeDriverLoginWrapper agent = this.getDriverWithShortestQueue();
		agent.taskQueue.add(task);
	}

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args) throws InterruptedException {

		/*
		// A. 获取所有地址
		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.getDriver().get("http://task.zbj.com/xuqiu/");
		List<String> list = new ArrayList<>();
		List<String> list1 = new ArrayList<>();

		// A1 需求
		// http://task.zbj.com/t-pxfw/
		List<WebElement> li = agent.getDriver().findElement(By.cssSelector("body > div.grid.list-category-nav > form > div.ui-dropdown.ui-dropdown-level1 > ul"))
				.findElements(By.tagName("li"));
		for (WebElement w : li) {
			for (WebElement ww : w.findElements(By.tagName("a"))) {
				list.add(ww.getAttribute("href").split("/")[3]);
			}
		}

		// A2 服务商
		//    http://task.zbj.com//jpps/p.html
		agent.getDriver().get("http://www.zbj.com/home/p.html");
		List<WebElement> li1 = agent.getDriver().findElement(By.cssSelector("#utopia_widget_5 > div.clearfix.category-list > ul"))
				.findElements(By.tagName("li"));
		for (WebElement w : li1) {
			list1.add(w.findElement(By.tagName("a")).getAttribute("href").split("/")[3]);
		}
		System.err.println(list.size()+"-----"+list1.size());

		agent.close();*/

		ChromeRequester requester = ChromeRequester.getInstance();


		/*requester.distribute(ProjectScanTask.generateTask("t-rcsc", 1, null));
		requester.distribute(ServiceScanTask.generateTask("rlzy", 1, null));
		requester.distribute(ProjectScanTask.generateTask("t-yxtg", 1, null));
		requester.distribute(ServiceScanTask.generateTask("yxtg", 1, null));
		requester.distribute(ProjectScanTask.generateTask("t-xswbzbj", 1, null));
		*/
		requester.distribute(ServiceScanTask.generateTask("dhmh", 1, null)); //
		/*requester.distribute(ProjectScanTask.generateTask("t-gongyesj", 1, null));
		requester.distribute(ServiceScanTask.generateTask("rcsc", 1, null));
*/
	/*	// C. 添加任务
		if(list.size() >= list1.size()) {
			for (int i = 0; i < list.size(); i++) {

				if (list1.size() > i) {
					requester.distribute(ServiceScanTask.generateTask(list1.get(i), 1, null));
				}
				requester.distribute(ProjectScanTask.generateTask(list.get(i), 1, null));
			}
		}
		else {
			for (int i = 0; i < list1.size(); i++) {

				if (list.size() > i) {
					requester.distribute(ProjectScanTask.generateTask(list.get(i), 1, null));
				}
				requester.distribute(ServiceScanTask.generateTask(list1.get(i), 1, null));
			}

		}*/
	}
}
