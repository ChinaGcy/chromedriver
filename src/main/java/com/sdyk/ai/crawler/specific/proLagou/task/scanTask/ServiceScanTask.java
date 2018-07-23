package com.sdyk.ai.crawler.specific.proLagou.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import org.jsoup.nodes.Document;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceScanTask extends com.sdyk.ai.crawler.task.ScanTask {

	static {
		registerBuilder(
				ProjectScanTask.class,
				"https://pro.lagou.com/project/myJobs.html",
				ImmutableMap.of("q", String.class),
				ImmutableMap.of("q","")
		);
	}

	public static String domain(){
		return "proLagou";
	}

	public ServiceScanTask(String url, int page) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setPriority(Priority.HIGH);

		this.setParam("page", init_map.get("page"));

		this.addDoneCallback((t) -> {


			Document doc = getResponse().getDoc();
			String s = doc.getElementsByClass("project_list table").toString();

			Pattern pattern = Pattern.compile("/project/myJobDetail.html\\?jobId\\=(?<Id>.+?)\"");
			Matcher matcher = pattern.matcher(s);

			Set<String> projectIdSet = new HashSet<>();

			while (matcher.find()) {
				projectIdSet.add(matcher.group("Id"));
			}
			for( String projectId : projectIdSet ){

				String suggestUrl ="{\"projectId\":" +
						projectId +
						",\"type\":3,\"pageId\":1,\"total\":10}";

				String applyUrl = "{\"projectId\":"
						+ projectId
						+ ",\"type\":5,\"pageId\":1,\"total\":10}";

				try {

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					ImmutableMap.of("scan_id", suggestUrl);

					Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.proLagou.task.scanTask.ServiceProviderScanTask");

					//生成holder
					ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

					//提交任务
					ChromeDriverDistributor.getInstance().submit(holder);

				} catch ( Exception e) {

					logger.error("error for submit ServiceProviderScanTask.class", e);
				}

				try {

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					ImmutableMap.of("scan_id", applyUrl);

					Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.proLagou.task.scanTask.ServiceProviderScanTask");

					//生成holder
					ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

					//提交任务
					ChromeDriverDistributor.getInstance().submit(holder);

				} catch ( Exception e) {

					logger.error("error for submit ServiceProviderScanTask.class", e);
				}

			}

		});

	}


	/**
	 * 判断是否为最大页数
	 *
	 * @param path
	 * @param page
	 * @return
	 */
	@Override
	public boolean pageTurning(String path, int page) {
		return false;
	}

	/**
	 * 获取ScanTask 标识
	 *
	 * @return
	 */
	@Override
	public TaskTrace getTaskTrace() {

		return new TaskTrace(this.getClass(), "all", this.getParamString("page"));
	}
}
