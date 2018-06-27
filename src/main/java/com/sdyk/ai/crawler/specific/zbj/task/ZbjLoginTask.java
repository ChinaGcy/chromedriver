package com.sdyk.ai.crawler.specific.zbj.task;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.account.model.AccountImpl;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

public class ZbjLoginTask extends Task {

	static {
		// init_map_class
		init_map_class = ImmutableMap.of("domain", String.class);
		// init_map_defaults
		init_map_defaults = ImmutableMap.of("domain", "baidu");
		// url_template
		url_template = "https://{{domain}}.com";
	}
	public ZbjLoginTask(String url) throws Exception {
		super(url);

		String domain = url.replace("https://","");

		this.addDoneCallback((t) -> {

		});

		Account account = AccountManager.getAccountByDomain(domain);

		this.addAction(new LoginWithGeetestAction(account));
	}
}
