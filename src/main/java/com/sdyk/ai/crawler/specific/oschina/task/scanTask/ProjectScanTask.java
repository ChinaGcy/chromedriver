package com.sdyk.ai.crawler.specific.oschina.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.oschina.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.ChromeTaskScheduler;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.*;
import one.rewind.io.requester.task.TaskHolder;

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
				ImmutableMap.of("page", String.class, "max_page", String.class),
				ImmutableMap.of("page", "", "max_page", "2"),
				true,
				Priority.HIGH
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
					TaskHolder holder =  ChromeTaskFactory.getInstance().newHolder(clazz, init_map);

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
					TaskHolder holder = this.getHolder(clazz, init_map);

					//提交任务
					((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

				} catch ( Exception e) {

					logger.error("error for submit ProjectTask.class", e);
				}

			}

			String maxPageSrc =  t.getStringFromVars("max_page");

			// 不含 max_page 参数，则表示可以一直翻页
			if( maxPageSrc.length() < 1 ){

				if( !(projectId.size() < 10) ) {

					int nextPage = page+1;

					try {

						//设置参数
						Map<String, Object> init_map = new HashMap<>();
						init_map.put("page", String.valueOf(nextPage));
						init_map.put("max_page", "");

						Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.oschina.task.scanTask.ProjectScanTask");

						//生成holder
						TaskHolder holder =  ChromeTaskFactory.getInstance().newHolder(clazz, init_map);

						//提交任务
						((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

					} catch ( Exception e) {

						logger.error("error for submit ProjectTask.class", e);
					}

				}
			}
			// 含有 max_page 参数，若max_page小于当前页则不进行翻页
			else {

				int maxPage = Integer.valueOf(maxPageSrc);
				int current_page = Integer.valueOf(t.getStringFromVars("page"));

				for(int i = current_page + 1; i <= maxPage; i++) {

					Map<String, Object> init_map = new HashMap<>();
					init_map.put("page", String.valueOf(i));
					init_map.put("max_page", "0");

					TaskHolder holder =  ChromeTaskFactory.getInstance().newHolder(t.getClass(), init_map);

					ChromeDriverDistributor.getInstance().submit(holder);
				}
			}

		});

	}

	@Override
	public TaskTrace getTaskTrace() {

		return new TaskTrace(this.getClass(), "all", this.getParamString("page"));
	}
}
