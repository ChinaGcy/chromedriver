package com.sdyk.ai.crawler.scheduler.test;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.Domain;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Map;

public class ProxyAllDomainBannedTask extends ChromeTask {

	public static long MIN_INTERVAL = 1L;

	static {
		registerBuilder(
				ProxyAllDomainBannedTask.class,
				"https://www.clouderwork.com/{{jobs}}",
				ImmutableMap.of("jobs", String.class),
				ImmutableMap.of("jobs", "jobs")
		);
	}

	public ProxyAllDomainBannedTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setBuildDom();

		this.setPriority(Priority.HIGH);

		this.setValidator((a, t) -> {

			Domain.getAll().stream().forEach(d ->{
				ProxyManager.getInstance().addProxyBannedRecord(a.proxy, d.domain);
			});

			throw new ProxyException.Failed(a.proxy);
		});

	}

	public static void registerBuilder(Class<? extends ChromeTask> clazz, String url_template, Map<String, Class> init_map_class, Map<String, Object> init_map_defaults){
		ChromeTask.registerBuilder( clazz, url_template, init_map_class, init_map_defaults );
	}

}
