package com.sdyk.ai.crawler.task;

import com.google.common.collect.ImmutableMap;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class KeepAliveTask extends Task {

	public static long MIN_INTERVAL = 12 * 60 * 60 * 1000L;

	static {
		registerBuilder(
				KeepAliveTask.class,
				"https://www.baidu.com/{{q}}",
				ImmutableMap.of("q", String.class),
				ImmutableMap.of("q", "")
		);
	}

	public KeepAliveTask(String url) throws MalformedURLException, URISyntaxException {
		super(url);

		this.setPriority(Priority.MEDIUM);

		//this.setNoFetchImages();
	}
}
