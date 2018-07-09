package com.sdyk.ai.crawler.specific.shichangbu.task;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.specific.shichangbu.action.ShichangbuLoginAction;
import one.rewind.io.requester.account.Account;

public class ShichangbuLoginTask extends Task {

	static {
		registerBuilder(
				ShichangbuLoginTask.class,
				"http://www.{{domain}}.com/member.php?mod=logging&action=login",
				ImmutableMap.of("domain", String.class),
				ImmutableMap.of("domain", "")
		);
	}

	public ShichangbuLoginTask(String url) throws Exception {

		super(url);

		String domain = url.replace("http://www.","")
				.replace(".com/member.php?mod=logging&action=login","");

		this.addDoneCallback((t) -> {

		});

		Account account = AccountManager.getAccountByDomain(domain);

		this.addAction(new ShichangbuLoginAction(account));
	}
}
