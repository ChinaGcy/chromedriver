package com.sdyk.ai.crawler.specific.shichangbu.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.specific.shichangbu.task.modelTask.ServiceProviderTask;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceScanTask extends ScanTask {

	static {
		registerBuilder(
				ServiceScanTask.class,
				"http://www.shichangbu.com/portal.php?mod=provider&op=search&sort=2&page={{page}}",
				ImmutableMap.of("page", String.class),
				ImmutableMap.of("page", "")
		);
	}

	public ServiceScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			//获取当前页数
			int page = 0;
			Pattern pattern_url = Pattern.compile("page=(?<page>.+?)");
			Matcher matcher_url = pattern_url.matcher(url);
			if (matcher_url.find()) {
				page = Integer.parseInt(matcher_url.group("page"));
			}

			String src = getResponse().getDoc().html();

			Pattern pattern = Pattern.compile("<a href=\"agency-(?<username>.+?).html\" title");
			Matcher matcher = pattern.matcher(src);

			Set<String> usernames = new HashSet<>();

			while( matcher.find() ) {
				usernames.add(matcher.group("username"));
			}

			for(String user : usernames){

				if( backtrace == true ){

					try {

						//设置参数
						Map<String, Object> init_map = new HashMap<>();
						ImmutableMap.of("servicer_id", user);

						Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.shichangbu.task.modelTask.ServiceProviderTask");

						//生成holder
						ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

						//提交任务
						ChromeDriverDistributor.getInstance().submit(holder);

					} catch ( Exception e) {

						logger.error("error for submit ServiceProviderTask.class", e);
					}

				}

			}

			String next = getResponse().getDoc().getElementsByClass("nxt").text();
			if( next != null && !"".equals(next) ) {

				int nextPag = page + 1;

				try {

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					ImmutableMap.of("page", String.valueOf(nextPag));

					Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.shichangbu.task.scanTask.ServiceScanTask");

					//生成holder
					ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

					//提交任务
					ChromeDriverDistributor.getInstance().submit(holder);

				} catch ( Exception e) {

					logger.error("error for submit ServiceScanTask.class", e);
				}

			}

		});
	}

}
