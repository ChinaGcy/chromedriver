package com.sdyk.ai.crawler.scheduler.test;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class CommonTask extends ChromeTask {

	public static long MIN_INTERVAL = 1L;

	static {
		registerBuilder(
				CommonTask.class,
				"http://dilidili.92demo.com/{{youyaoqidm}}/",
				ImmutableMap.of("youyaoqidm", String.class),
				ImmutableMap.of("youyaoqidm", "youyaoqidm")
		);
	}

	public CommonTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setBuildDom();

		this.setValidator((a, t) ->{

		//	System.err.println("登陆队列任务数 : " + ((Distributor)ChromeDriverDistributor.getInstance()).loginTaskQueues.get(a).size());

		});

		this.setPriority(Priority.LOW);

		this.addDoneCallback((t) -> {

			Thread.sleep(4000);

			// 设置普通任务
			Map<String, Object> init_map_ = new HashMap<>();

			init_map_.put("youyaoqidm", "youyaoqidm");

			Class clazz_ = Class.forName("com.sdyk.ai.crawler.scheduler.test.CommonTask");

			ChromeTaskHolder holder_ = ChromeTask.buildHolder(clazz_, init_map_ );

			((Distributor)ChromeDriverDistributor.getInstance()).submit(holder_);



		});

	}
}
