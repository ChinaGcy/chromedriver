package com.sdyk.ai.crawler.specific.shichangbu;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.specific.shichangbu.action.ShichangbuLoginAction;
import com.sdyk.ai.crawler.specific.shichangbu.task.ShichangbuLoginTask;
import com.sdyk.ai.crawler.specific.shichangbu.task.scanTask.ServiceScanTask;
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
	@Override
	public void getLoginTask(ChromeDriverAgent agent, Account account) throws MalformedURLException, URISyntaxException, ChromeDriverException.IllegalStatusException, InterruptedException {

		agent.submit(new ChromeTask("http://www.shichangbu.com/member.php?mod=logging&action=login").addAction(new ShichangbuLoginAction(account)));

	}

	/**
	 * @param backtrace
	 * @return
	 */
	@Override
	public void getTask(boolean backtrace) {

		if( backtrace == true ){

			try {
				HttpTaskPoster.getInstance().submit(ServiceScanTask.class,
						ImmutableMap.of("page", "1"));
			} catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {

				logger.error("error fro HttpTaskPoster.submit ServiceScanTask.class", e);
			}

		}

	}

	/**
	 * 获取历史数据
	 */
	@Override
	public void getHistoricalData() {

		// 需求
		getTask(true);

	}

	/**
	 * 监控调度
	 */
	@Override
	public void monitoring() {

		getTask(false);

	}

	public static void main(String[] args) {

		int num = 0;

		if (args.length >= 1 && !args[0].equals("") && Integer.parseInt(args[0]) > 1) {
			num = Integer.parseInt(args[0]);
		}

		Scheduler scheduler = new Scheduler("shichangbu", num);

		scheduler.getHistoricalData();

		scheduler.monitoring();
	}
}
