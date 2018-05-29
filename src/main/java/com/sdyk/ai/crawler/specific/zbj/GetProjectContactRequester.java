package com.sdyk.ai.crawler.specific.zbj;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.typesafe.config.Config;
import one.rewind.io.requester.BasicRequester;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.util.Configs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GetProjectContactRequester extends ChromeDriverRequester {

	public static GetProjectContactRequester instance;

	ChromeDriverAgent agent = new ChromeDriverAgent();

	public String domain;

	public Account account;

	public static final Logger logger = LogManager.getLogger(ChromeDriverRequester.class.getName());

	// 连接超时时间
	public static int CONNECT_TIMEOUT;

	// 读取超时时间
	public static int READ_TIMEOUT;

	public static int AGENT_NUM = 1;

	public static String REQUESTER_LOCAL_IP;

	public static ExecutorService requester_executor;

	// 配置设定
	static {

		Config ioConfig = Configs.getConfig(BasicRequester.class);
		CONNECT_TIMEOUT = ioConfig.getInt("connectTimeout");
		READ_TIMEOUT = 0;
		REQUESTER_LOCAL_IP = ioConfig.getString("requesterLocalIp");
		requester_executor = Executors.newSingleThreadExecutor(
				new ThreadFactoryBuilder().setNameFormat("GetProjectContactRequester-%d").build());
	}

	public static GetProjectContactRequester getInstance() {
		if (instance == null) {
			Class var0 = GetProjectContactRequester.class;
			synchronized(GetProjectContactRequester.class) {
				if (instance == null) {
					instance = new GetProjectContactRequester();
					requester_executor.submit(instance);
				}
			}
		}

		return instance;
	}

	public GetProjectContactRequester() {
		try {
			this.addAgent(agent);
		} catch (ChromeDriverException.IllegalStatusException e) {
			e.printStackTrace();
		}
	}

}
