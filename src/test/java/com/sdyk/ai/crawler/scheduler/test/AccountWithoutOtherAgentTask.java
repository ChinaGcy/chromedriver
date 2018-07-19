package com.sdyk.ai.crawler.scheduler.test;

import com.google.common.collect.ImmutableMap;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.task.ChromeTask;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class AccountWithoutOtherAgentTask extends ChromeTask {

	public static long MIN_INTERVAL = 1L;

	static {
		registerBuilder(
				AccountWithoutOtherAgentTask.class,
				"https://www.clouderwork.com/{{jobs}}",
				ImmutableMap.of("jobs", String.class),
				ImmutableMap.of("jobs", "jobs")
		);
	}

	public AccountWithoutOtherAgentTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setBuildDom();

		this.setPriority(Priority.HIGH);

		this.setValidator((a, t) -> {

			throw new AccountException.Failed(a.accounts.get(t.getDomain()));

		});

	}
}
