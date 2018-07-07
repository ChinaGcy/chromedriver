package com.sdyk.ai.crawler.specific.proLagou.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import one.rewind.io.requester.exception.ProxyException;
import org.jsoup.nodes.Document;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceScanTask extends com.sdyk.ai.crawler.task.ScanTask {

	static {
		// init_map_class
		init_map_class = ImmutableMap.of("page", String.class);
		// init_map_defaults
		init_map_defaults = ImmutableMap.of("q", "ip");
		// url_template
		url_template = "https://pro.lagou.com/project/myJobs.html";
	}

	public static String domain(){
		return "proLagou";
	}

	public ServiceScanTask(String url, int page) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setBuildDom();

		this.setPriority(Priority.HIGH);

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
					HttpTaskPoster.getInstance().submit(ServiceProviderScanTask.class,
							ImmutableMap.of("scan_id", suggestUrl));

				} catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {
					logger.error("error for HttpTaskPoster.submit ServiceProviderScanTask", e);
				}

				try {
					HttpTaskPoster.getInstance().submit(ServiceProviderScanTask.class,
							ImmutableMap.of("scan_id", applyUrl));

				} catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {
					logger.error("error for HttpTaskPoster.submit ServiceProviderScanTask", e);
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
		return null;
	}
}
