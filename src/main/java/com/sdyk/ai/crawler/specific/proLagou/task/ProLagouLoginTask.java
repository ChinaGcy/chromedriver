package com.sdyk.ai.crawler.specific.proLagou.task;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.specific.clouderwork.task.Task;
import com.sdyk.ai.crawler.specific.proginn.action.ProginnLoginAction;
import one.rewind.io.requester.account.Account;

public class ProLagouLoginTask extends Task {

	static {
		// init_map_class
		init_map_class = ImmutableMap.of("domain", String.class);
		// init_map_defaults
		init_map_defaults = ImmutableMap.of("domain", "baidu");
		// url_template
		url_template = "https://passport.{{domain}}.com/pro/login.html";
	}

	public ProLagouLoginTask(String url) throws Exception {

		super(url);

		String domain = url.replace("https://passport.","")
				.replace(".com/pro/login.html","");

		this.addDoneCallback((t) -> {

		});

		Account account = AccountManager.getAccountByDomain(domain);

		this.addAction(new ProginnLoginAction(account));

	}
}
