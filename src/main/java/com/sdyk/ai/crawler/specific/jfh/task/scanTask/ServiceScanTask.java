package com.sdyk.ai.crawler.specific.jfh.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.specific.jfh.task.modelTask.ServiceProviderTask;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import org.jsoup.nodes.Document;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceScanTask extends ScanTask {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				ServiceScanTask.class,
				"https://list.jfh.com/shops",
				ImmutableMap.of("page", String.class),
				ImmutableMap.of("page", "")
		);
	}

	public ServiceScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setBuildDom();

		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			String pageUrl = doc.select("span.jPag-current").text();

			int page = Integer.valueOf(pageUrl);

			int nextPage = 100+ ( page * 30 );
			List<Task> task = new ArrayList<>();
			for(int i = page ; i < nextPage ; i++ ){

				try {

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					ImmutableMap.of("servicer_id", String.valueOf(i));

					Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.jfh.task.modelTask.ServiceProviderTask");

					//生成holder
					ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

					//提交任务
					ChromeDriverDistributor.getInstance().submit(holder);

				} catch ( Exception e) {

					logger.error("error for submit ServiceProviderTask.class", e);
				}

			}

		});

	}

	public static void registerBuilder(Class<? extends ChromeTask> clazz, String url_template, Map<String, Class> init_map_class, Map<String, Object> init_map_defaults){
		ChromeTask.registerBuilder( clazz, url_template, init_map_class, init_map_defaults );
	}
}
