package com.sdyk.ai.crawler.specific.proginn;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.specific.proginn.action.ProginnLoginAction;
import com.sdyk.ai.crawler.specific.proginn.task.ProginnLoginTask;
import com.sdyk.ai.crawler.specific.proginn.task.scanTask.ServiceScanTask;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.task.ChromeTask;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class Scheduler extends com.sdyk.ai.crawler.Scheduler {

	public String cron = "*/30 * * * *";

	public Scheduler() {
		super();
	}

	public Scheduler(String domain, int driverCount) {
		super(domain, driverCount);
	}

	/**
	 * @return
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public void getLoginTask(ChromeDriverAgent agent, Account account) throws MalformedURLException, URISyntaxException, ChromeDriverException.IllegalStatusException, InterruptedException {

		try {
			agent.submit(new ChromeTask("https://www.proginn.com/?loginbox=show").addAction(new ProginnLoginAction(account)));
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * @param backtrace
	 * @return
	 */
	public void getTask(boolean backtrace) {

		if( backtrace == true ){

			try {
				HttpTaskPoster.getInstance().submit(ServiceScanTask.class,
						ImmutableMap.of("page", "1"));

			} catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {
				logger.error("error for HttpTaskPoster.submit ServiceScanTask", e);
			}

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

		getTask(false);

	}

	public static void main(String[] args) {

		int num = 0;

		if (args.length >= 1 && !args[0].equals("") && Integer.parseInt(args[0]) > 1) {
			num = Integer.parseInt(args[0]);
		}

		Scheduler scheduler = new Scheduler("proginn", num);

		scheduler.getHistoricalData();

		scheduler.monitoring();
	}
}
