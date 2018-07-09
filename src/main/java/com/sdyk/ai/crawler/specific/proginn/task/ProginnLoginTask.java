package com.sdyk.ai.crawler.specific.proginn.task;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.specific.proginn.action.ProginnLoginAction;
import one.rewind.io.requester.account.Account;

public class ProginnLoginTask extends Task {

	static {
		registerBuilder(
				ProginnLoginTask.class,
				"https://www.{{domain}}.com/?loginbox=show",
				ImmutableMap.of("domain", String.class),
				ImmutableMap.of("domain", "")
		);
	}

	public ProginnLoginTask(String url) throws Exception {

		super(url);

		String domain = url.replace("https://www.","")
				.replace(".com/?loginbox=show","");

		this.addDoneCallback((t) -> {

		});

		Account account = AccountManager.getAccountByDomain(domain);

		this.addAction(new ProginnLoginAction(account));

	}

}
