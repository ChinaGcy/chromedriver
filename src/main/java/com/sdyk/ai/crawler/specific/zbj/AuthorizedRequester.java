package com.sdyk.ai.crawler.specific.zbj;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.typesafe.config.Config;
import one.rewind.io.requester.BasicRequester;
import one.rewind.io.requester.Task;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.util.Configs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;

public class AuthorizedRequester extends ChromeDriverRequester {

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
					requester_executor.submit(instance);
				}
			}
		}
		return instance;
	}

	public ChromeDriverAgent agent;

	ThreadPoolExecutor executor = new ThreadPoolExecutor(
			10,
			20,
			0, TimeUnit.MICROSECONDS,
			new SynchronousQueue<>()
	);

	ThreadPoolExecutor post_executor = new ThreadPoolExecutor(
			10,
			10,
			0, TimeUnit.MICROSECONDS,
			new LinkedBlockingQueue<>()
	);

	ThreadPoolExecutor restart_executor = new ThreadPoolExecutor(
			4,
			4,
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

		// 使用本地IP
		// 读取一个特定账户

		try {
			this.addAgent(agent);
		} catch (ChromeDriverException.IllegalStatusException e) {
			e.printStackTrace();
		}
	}
}
