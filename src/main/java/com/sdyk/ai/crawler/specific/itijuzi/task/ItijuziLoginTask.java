package com.sdyk.ai.crawler.specific.itijuzi.task;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.LoginInAction;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.specific.mihuashi.task.MihuashiLoginTask;
import com.sdyk.ai.crawler.task.LogTask;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Map;

public class ItijuziLoginTask extends ChromeTask {

	public static final Logger logger = LogManager.getLogger(com.sdyk.ai.crawler.specific.zbj.task.Task.class.getName());

	static {
		registerBuilder(
				MihuashiLoginTask.class,
				"https://www.itjuzi.com/user/login?accountId={{accountId}}",
				ImmutableMap.of("accountId", String.class),
				ImmutableMap.of("accountId", "")
		);
	}

	public static String domain() {
		return "itijuzi.com";
	}

	public ItijuziLoginTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setBuildDom();

		this.setPriority(Task.Priority.HIGH);

		String domain = "itijuzi.com";

		String accountId = url.split("accountId=")[1];

		try {
			Account account = AccountManager.getInstance().getAccountById(accountId);
			LoginInAction loginInAction = LogTask.getLoginActionByDomain(domain, account);
			this.addAction( loginInAction );
		} catch (Exception e) {
			logger.error("error for get account by Id", e);
		}


	}

	public static void registerBuilder(Class<? extends ChromeTask> clazz, String url_template, Map<String, Class> init_map_class, Map<String, Object> init_map_defaults){
		ChromeTask.registerBuilder( clazz, url_template, init_map_class, init_map_defaults );
	}
}
