package com.sdyk.ai.crawler.scheduler.test;

import com.google.common.collect.ImmutableMap;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class ProxyDomainBannedWithAccountTask extends ChromeTask {

	public static long MIN_INTERVAL = 1L;

	static {
		registerBuilder(
				ProxyDomainBannedWithAccountTask.class,
				"https://www.clouderwork.com/{{jobs}}",
				ImmutableMap.of("jobs", String.class),
				ImmutableMap.of("jobs", "jobs")
		);
	}

	public ProxyDomainBannedWithAccountTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setBuildDom();

		this.setPriority(Priority.HIGH);

		this.setValidator((a, t) -> {

			throw new ProxyException.Failed(a.proxy);
		});

	}

}
