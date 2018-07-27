package com.sdyk.ai.crawler.specific.oschina.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.oschina.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectScanTask extends ScanTask {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				ProjectScanTask.class,
				"https://zb.oschina.net/project/contractor-browse-project-and-reward?" +
						"applicationAreas=&sortBy=30&pageSize=10&currentPage={{page}}",
				ImmutableMap.of("page", String.class),
				ImmutableMap.of("page", "")
		);
	}

	public ProjectScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setPriority(Priority.HIGH);

		this.setParam("page", url.split("currentPage=")[1]);

		this.addDoneCallback((t) -> {

			int page = Integer.valueOf(url.split("currentPage=")[1]);

			String src = getResponse().getDoc().html();

			Pattern pattern = Pattern.compile("\"id\":(?<Id>.+?),\"createdAt\"");
			Matcher matcher = pattern.matcher(src);

			Set<String> projectId = new HashSet<>();

			while( matcher.find() ) {
				projectId.add(matcher.group("Id"));
			}

			for( String id : projectId ) {

				try {

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					init_map.put("project_id", "project/detail.html?id="+id);

					Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.oschina.task.modelTask.ProjectTask");

					//生成holder
					ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

					//提交任务
					((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

				} catch ( Exception e) {

					logger.error("error for submit ProjectTask.class", e);
				}


				try {

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					init_map.put("project_id", "reward/detail.html?id="+id);

					Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.oschina.task.modelTask.ProjectTask");

					//生成holder
					ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

					//提交任务
					((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

				} catch ( Exception e) {

					logger.error("error for submit ProjectTask.class", e);
				}

			}

			if( !(projectId.size() < 10) ) {

				int nextPage = page+1;

				try {

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					init_map.put("page", String.valueOf(nextPage));

					Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.oschina.task.scanTask.ProjectScanTask");

					//生成holder
					ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

					//提交任务
					((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

				} catch ( Exception e) {

					logger.error("error for submit ProjectTask.class", e);
				}

			}

		});

	}

	@Override
	public TaskTrace getTaskTrace() {

		return new TaskTrace(this.getClass(), "all", this.getParamString("page"));
	}
}
