package com.sdyk.ai.crawler.specific.zbj.task;

import com.sdyk.ai.crawler.account.AccountManager;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;

public class ZbjLoginTask extends Task {

	/*static {
		// init_map_class
		init_map_class = ImmutableMap.of("domain", String.class);
		// init_map_defaults
		init_map_defaults = ImmutableMap.of("domain", "baidu");
		// url_template
		url_template = "https://www.{{domain}}.com";
	}*/
	public ZbjLoginTask(String url) throws Exception {
		super(url);

		String domain = url.replace("https://","");

		this.addDoneCallback((t) -> { });

		Account account = AccountManager.getInstance().getAccountsByDomain(domain,null);

		this.addAction(new LoginWithGeetestAction(account));
	}
}
