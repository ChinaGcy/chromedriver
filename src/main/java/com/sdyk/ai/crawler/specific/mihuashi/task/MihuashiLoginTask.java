package com.sdyk.ai.crawler.specific.mihuashi.task;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.specific.clouderwork.task.Task;
import com.sdyk.ai.crawler.specific.mihuashi.action.MihuashiLoginAction;
import one.rewind.io.requester.account.Account;

public class MihuashiLoginTask extends Task {

	/*static {
		// init_map_class
		init_map_class = ImmutableMap.of("domain", String.class);
		// init_map_defaults
		init_map_defaults = ImmutableMap.of("domain", "baidu");
		// url_template
		url_template = "https://www.{{domain}}.com/login";
	}*/

	public MihuashiLoginTask(String url) throws Exception {

		super(url);

		String domain = url.replace("https://www.","")
				.replace(".com/login","");

		this.addDoneCallback((t) -> {

		});

		Account account = AccountManager.getAccountByDomain(domain);

		this.addAction(new MihuashiLoginAction(account));

	}

}
