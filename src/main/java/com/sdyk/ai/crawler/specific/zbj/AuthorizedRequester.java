package com.sdyk.ai.crawler.specific.zbj;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.typesafe.config.Config;
import one.rewind.io.requester.BasicRequester;

import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;

import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.util.Configs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;

/**
 *
 */
public class AuthorizedRequester extends ChromeDriverDistributor {

	public static AuthorizedRequester instance;

	public String domain;

	public Account account;

	public static int AGENT_NUM = 1;

	public static ExecutorService requester_executor;

	// 配置设定
	static {

		Config ioConfig = Configs.getConfig(BasicRequester.class);
		requester_executor = Executors.newSingleThreadExecutor(
				new ThreadFactoryBuilder().setNameFormat("AuthorizedRequester-%d").build());
	}

	/**
	 *
	 * @return
	 */
	public static AuthorizedRequester getInstance() {

		if (instance == null) {
			synchronized (AuthorizedRequester.class) {
				if (instance == null) {
					instance = new AuthorizedRequester();
					requester_executor.submit((Runnable) instance);
				}
			}
		}
		return instance;
	}

	ThreadPoolExecutor executor = new ThreadPoolExecutor(
			1,
			1,
			0, TimeUnit.MICROSECONDS,
			new SynchronousQueue<>()
	);

	ThreadPoolExecutor post_executor = new ThreadPoolExecutor(
			2,
			2,
			0, TimeUnit.MICROSECONDS,
			new LinkedBlockingQueue<>()
	);

	ThreadPoolExecutor restart_executor = new ThreadPoolExecutor(
			1,
			1,
			0, TimeUnit.MICROSECONDS,
			new LinkedBlockingQueue<>()
	);

	private volatile boolean done = false;

	public AuthorizedRequester() {

		executor.setThreadFactory(new ThreadFactoryBuilder()
				.setNameFormat("ChromeDriverRequester-Worker-%d").build());

		executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

		post_executor.setThreadFactory(new ThreadFactoryBuilder()
				.setNameFormat("ChromeDriverRequester-PostWorker-%d").build());

		restart_executor.setThreadFactory(new ThreadFactoryBuilder()
				.setNameFormat("ChromeDriverRequester-RestartWorker-%d").build());

	}

	// 增加每天执行次数统计，超过次数拒绝任务
	public boolean submit_(ChromeTask task) {
		if(true) {
			/*this.queues.offer(task);*/
			return true;
		} else {
			return false;
		}
	}
}
