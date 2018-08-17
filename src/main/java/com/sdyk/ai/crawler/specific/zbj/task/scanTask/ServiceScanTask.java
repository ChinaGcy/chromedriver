package com.sdyk.ai.crawler.specific.zbj.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.TaskHolder;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 服务商列表
 * 1. 找到url
 * 2. 翻页
 */
public class ServiceScanTask extends ScanTask {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				ServiceScanTask.class,
				"https://www.zbj.com/home/pk{{page}}.html",
				ImmutableMap.of("page", String.class),
				ImmutableMap.of("page", "1"),
				false,
				Priority.MEDIUM
		);
	}

	String page_;

	/**
	 *
	 * @param url
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public ServiceScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			int page = 0;
			Pattern pattern_url = Pattern.compile("https://www.zbj.com/home/pk(?<page>\\d+)");
			Matcher matcher_url = pattern_url.matcher(url);
			if (matcher_url.find()) {
				page = Integer.parseInt(matcher_url.group("page"));
			}
			page_ = page + "";
			try {

				String src = getResponse().getText();


				Pattern pattern = Pattern.compile("//shop.zbj.com/(?<userid>\\d+)/");
				Matcher matcher = pattern.matcher(src);

				while (matcher.find()) {

					String user_id = matcher.group("userid");
					try {

						try {

							//设置参数
							Map<String, Object> init_map = ImmutableMap.of("user_id", user_id);

							Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.zbj.task.modelTask.ServiceProviderTask");

							//生成holder
							TaskHolder holder = this.getHolder(clazz, init_map);

							//提交任务
							ChromeDriverDistributor.getInstance().submit(holder);

						} catch ( Exception e) {

							logger.error("error for submit ProjectTask.class", e);
						}


					} catch (Exception e) {
						logger.error(e);
					}
				}

				// 当前页数
				int i_ = ((int) (page - 1) / 40) + 1;


				// #utopia_widget_18 > div.pagination > ul > li:nth-child(1)
				// 翻页
				if (pageTurning("#utopia_widget_18 > div.pagination > ul > li", i_)) {

					try {

						//设置参数
						Map<String, Object> init_map = ImmutableMap.of("page", String.valueOf(page+40));


						Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.zbj.task.scanTask.ServiceScanTask");

						//生成holder
						TaskHolder holder = this.getHolder(clazz, init_map);

						//提交任务
						ChromeDriverDistributor.getInstance().submit(holder);

					} catch ( Exception e) {

						logger.error("error for submit ProjectTask.class", e);
					}

				}
				//logger.info("Task driverCount: {}", tasks.size());

			} catch (Exception e) {
				logger.error(e);
			}
		});
	}
	@Override
	public TaskTrace getTaskTrace() {
		return new TaskTrace(this.getClass(), "service", page_);
	}

}
