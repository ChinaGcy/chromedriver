package com.sdyk.ai.crawler.zbj.task;

import com.sdyk.ai.crawler.zbj.ChromeDriverWithLogin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.tfelab.io.requester.account.AccountWrapper;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;
import org.tfelab.io.requester.chrome.ChromeDriverRequester;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 服务商列表
 * 1. 找到url
 * 2. 翻页
 */
public class ServiceScanTask extends Task {
	private static final Logger logger = LogManager.getLogger(ServiceScanTask.class.getName());


	public static ServiceScanTask generateTask(int page, AccountWrapper aw) {

		if(page >= 100) return null;

		String url = "http://www.zbj.com/home/pk" + page + ".html";

		try {
			ServiceScanTask t = new ServiceScanTask(url, page);
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
	public ServiceScanTask(String url, int page) throws MalformedURLException, URISyntaxException {

		super(url);
		this.setParam("page", page);
	}

	/**
	 *
	 * @return
	 */
	public List<Task> postProc(WebDriver driver) throws Exception {
		//System.err.println("1111111111111");
		String src = getResponse().getText();

		//System.err.println(src);

		List<Task> tasks = new ArrayList<>();

		int page = this.getParamInt("page");

		Pattern pattern = Pattern.compile("//shop.zbj.com/\\d+/");
		Matcher matcher = pattern.matcher(src);

		List<String> list = new ArrayList<>();

		while (matcher.find()) {

			String url = "http:" + matcher.group();

			if(!list.contains(url)) {
				System.err.println(url);

				list.add(url);
				tasks.add(new ServiceSupplierTask(url));
			}
		}


		//判断一页是否有40个数据
		if (list.size() == 40) {

			Task t = generateTask(++page, null);
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

		Queue<Task> taskQueue = new LinkedBlockingQueue<>();

		taskQueue.add(ServiceScanTask.generateTask(1,null));

		while(!taskQueue.isEmpty()) {
			Task t = taskQueue.poll();
			if(t != null) {
				try {
					agent.fetch(t);
					for (Task t_ : t.postProc(agent.getDriver())) {
						taskQueue.add(t_);
					}

				} catch (Exception e) {
					logger.error("Exception while fetch task. ", e);
					taskQueue.add(t);
				}
			}
		}
	}
}
