package com.sdyk.ai.crawler.specific.itijuzi;

import com.sdyk.ai.crawler.specific.itijuzi.action.ItijuziLoginAction;
import com.sdyk.ai.crawler.specific.itijuzi.task.CompanyListScanTask;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class Scheduler extends com.sdyk.ai.crawler.Scheduler {

	public String cron = "*/30 * * * *";

	public Scheduler() {
		super();
	}

	public Scheduler(String domain, int driverCount) {
		super(domain, driverCount);
	}

	/**
	 * @param agent
	 * @param account
	 * @return
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public void getLoginTask(ChromeDriverAgent agent, Account account) throws MalformedURLException, URISyntaxException, ChromeDriverException.IllegalStatusException, InterruptedException {

		try {
			agent.submit(new ChromeTask("https://www.itjuzi.com/user/login").addAction(new ItijuziLoginAction(account)));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param backtrace
	 * @return
	 */
	public void getTask(boolean backtrace) {

		try {

			//设置参数
			Map<String, Object> init_map = new HashMap<>();
			init_map.put("page", "1");

			CompanyListScanTask companyListScanTask = new CompanyListScanTask("http://radar.itjuzi.com/company");

			//生成holder
			ChromeTaskHolder holder = ChromeTask.buildHolder(CompanyListScanTask.class, init_map);

			//提交任务
			ChromeDriverDistributor.getInstance().submit(holder);

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 获取历史数据
	 */
	public void getHistoricalData() {

		// 需求
		getTask(true);

	}

	/**
	 * 监控调度
	 */
	public void monitoring() {

	}

	public static void main(String[] args) {

		int num = 1;

		Scheduler scheduler = new Scheduler("itijuzi", num);

		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		scheduler.getHistoricalData();

	}

}
