package com.sdyk.ai.crawler.specific.oschina.task;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.specific.oschina.action.OschinaLoginAction;
import one.rewind.io.requester.account.Account;

public class OschinaLoginTask extends Task {

	static {
		registerBuilder(
				OschinaLoginTask.class,
				"https://www.{{domain}}.net/home/login",
				ImmutableMap.of("domain", String.class),
				ImmutableMap.of("domain", "")
		);
	}

	public OschinaLoginTask(String url) throws Exception {

		super(url);

		String domain = url.replace("https://www.","")
				.replace(".net/home/login","");

		this.addDoneCallback((t) -> {

		});

		Account account = AccountManager.getInstance().getAccountsByDomain(domain,null);

		this.addAction(new OschinaLoginAction(account));

	}
}
