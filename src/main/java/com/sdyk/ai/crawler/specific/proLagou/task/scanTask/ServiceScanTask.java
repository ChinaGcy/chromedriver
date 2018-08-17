package com.sdyk.ai.crawler.specific.proLagou.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.vividsolutions.jts.index.strtree.SIRtree;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import org.jsoup.nodes.Document;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceScanTask extends com.sdyk.ai.crawler.task.ScanTask {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				ServiceScanTask.class,
				"https://pro.lagou.com/project/myJobs.html",
				ImmutableMap.of("q", String.class),
				ImmutableMap.of("q","q")
		);
	}

	public ServiceScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setPriority(Priority.HIGH);

		this.setNoFetchImages();

		this.addDoneCallback((t) -> {

			List<String> urlList = new ArrayList<>();
			urlList.add("1063864230");
			urlList.add("1444339558");
			urlList.add("2035789400");
			urlList.add("1499377079");
			urlList.add("114678383");
			urlList.add("1298289106");
			urlList.add("194245315");
			urlList.add("2026889144");
			urlList.add("330444376");
			urlList.add("848849983");
			urlList.add("1436324527");
			urlList.add("343856295");
			urlList.add("1811233949");
			urlList.add("1051353576");
			urlList.add("1992408762");
			urlList.add("1473440792");
			urlList.add("501875341");
			urlList.add("550996503");
			urlList.add("1599520736");
			urlList.add("487583800");
			urlList.add("775052242");
			urlList.add("1685620892");
			urlList.add("2127261279");
			urlList.add("1831155084");
			urlList.add("1518734512");
			urlList.add("845893165");
			urlList.add("1025264614");
			urlList.add("1008073612");
			urlList.add("1258309904");

			for(String s : urlList){

				try {

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					init_map.put("user_id", s);

					Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.proLagou.task.modelTask.ServiceProviderTask");

					//生成holder
					//ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

					//提交任务
					ChromeDriverDistributor.getInstance().submit(holder);

				} catch ( Exception e) {

					logger.error("error for submit ServiceProviderTask.class", e);
				}

			}


			/*Document doc = getResponse().getDoc();
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

			}*/

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

		return new TaskTrace(this.getClass(), "all", "1");
	}
}
