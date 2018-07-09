package com.sdyk.ai.crawler.specific.jfh.task;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.specific.jfh.action.JfhLoginAction;
import one.rewind.io.requester.account.Account;

public class JfhLoginTask extends Task {

	static {
		registerBuilder(
				JfhLoginTask.class,
				"https://www.{{domain}}.com/security/index",
				ImmutableMap.of("domain", String.class),
				ImmutableMap.of("domain", "baidu")
		);
	}

	public JfhLoginTask(String url) throws Exception {

		super(url);

		String domain = url.replace("https://www.","")
				.replace(".com/security/index","");

		this.addDoneCallback((t) -> {

		});

		Account account = AccountManager.getAccountByDomain(domain);

		this.addAction(new JfhLoginAction(account));

	}
}
