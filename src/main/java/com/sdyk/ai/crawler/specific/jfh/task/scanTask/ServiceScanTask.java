package com.sdyk.ai.crawler.specific.jfh.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.jfh.task.modelTask.ServiceProviderTask;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.ChromeTaskScheduler;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import one.rewind.io.requester.task.ScheduledChromeTask;
import org.jsoup.nodes.Document;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;

public class ServiceScanTask extends ScanTask {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	public static List<String> crons = Arrays.asList("0 0 0 1/1 * ? *");

	static {
		registerBuilder(
				ServiceScanTask.class,
				"https://list.jfh.com/shops/?{{page}}",
				ImmutableMap.of("page", String.class, "max_page", String.class),
				ImmutableMap.of("page", "", "max_page", "1")
		);
	}

	public ServiceScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setPriority(Priority.HIGH);

		this.setNoFetchImages();

		this.setParam("page", url.split("shops/?")[1]);

		this.addDoneCallback((t) -> {

			String pageUrl = url.split("shops/?")[1];

			int page = Integer.valueOf(pageUrl);

			int nextPage = 100+ ( page * 30 );

			for(int i = page ; i < nextPage ; i++ ){

				try {

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					init_map.put("servicer_id", String.valueOf(i));

					Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.jfh.task.modelTask.ServiceProviderTask");

					//生成holder
					ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

					//提交任务
					((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

				} catch ( Exception e) {

					logger.error("error for submit ServiceProviderTask.class", e);
				}

			}

			String maxPageSrc =  String.valueOf(((ChromeTask) t).init_map.get("max_page"));
			if( maxPageSrc.length() < 1 ){
				if( nextPage < 3000 ){

					try {

						//设置参数
						Map<String, Object> init_map = new HashMap<>();
						init_map.put("page", String.valueOf(nextPage));
						init_map.put("max_page", "");

						Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.jfh.task.scanTask.ServiceScanTask");

						//生成holder
						ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

						//提交任务
						((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

					} catch ( Exception e) {

						logger.error("error for submit ServiceScanTask.class", e);
					}

				}
			}
			else {
				int maxPage = Integer.valueOf(maxPageSrc);
				int current_page = Integer.valueOf(String.valueOf(((ChromeTask) t).init_map.get("page")));

				for(int i = current_page + 1; i <= maxPage; i++) {

					Map<String, Object> init_map = new HashMap<>();
					init_map.put("page", String.valueOf(i));
					init_map.put("max_page", "0");

					ChromeTaskHolder holder = ((ChromeTask) t).getHolder(((ChromeTask) t).getClass(), init_map);

					ChromeDriverDistributor.getInstance().submit(holder);
				}
			}

			ChromeTask t_ = (ChromeTask)t;

			// 注册定时任务, 只注册一次
			if( !ChromeTaskScheduler.getInstance().registered(t_._scheduledTaskId) ){
				try {

					Map<String, Object> init_map = new HashMap<>();
					init_map.put("page", "1");
					init_map.put("max_page","2");

					ScheduledChromeTask scheduledTask = new ScheduledChromeTask(
							t_.getHolder(this.getClass(), init_map),
							crons
					);
					ChromeTaskScheduler.getInstance().schedule(scheduledTask);
				} catch (Exception e) {
					logger.error("eror for creat ScheduledChromeTask", e);
				}
			}


		});

	}

	@Override
	public TaskTrace getTaskTrace() {

		return new TaskTrace(this.getClass(), "all", this.getParamString("page"));
	}

}
