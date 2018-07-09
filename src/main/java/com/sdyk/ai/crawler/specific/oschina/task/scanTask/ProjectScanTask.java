package com.sdyk.ai.crawler.specific.oschina.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.specific.oschina.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.exception.ProxyException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectScanTask extends ScanTask {

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

		this.setBuildDom();

		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			int page = 0;
			Pattern pattern_url = Pattern.compile("currentPage=(?<page>.+?)");
			Matcher matcher_url = pattern_url.matcher(url);
			if (matcher_url.find()) {
				page = Integer.parseInt(matcher_url.group("page"));
			}

			String src = getResponse().getDoc().html();

			Pattern pattern = Pattern.compile("\"id\":(?<Id>.+?),\"createdAt\"");
			Matcher matcher = pattern.matcher(src);

			Set<String> projectId = new HashSet<>();

			while( matcher.find() ) {
				projectId.add(matcher.group("Id"));
			}

			List<Task> task = new ArrayList<>();

			for( String id : projectId ) {

				try {
					HttpTaskPoster.getInstance().submit(ProjectTask.class,
							ImmutableMap.of("project_id", "project/detail.html?id="+id));
				} catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {

					logger.error("error fro HttpTaskPoster.submit ProjectTask.class", e);
				}

				try {
					HttpTaskPoster.getInstance().submit(ProjectTask.class,
							ImmutableMap.of("project_id", "reward/detail.html?id="+id));
				} catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {

					logger.error("error fro HttpTaskPoster.submit ProjectTask.class", e);
				}

			}

			if( !(projectId.size() < 10) ) {

				int nextPage = page+1;

				try {
					HttpTaskPoster.getInstance().submit(ProjectScanTask.class,
							ImmutableMap.of("page", String.valueOf(nextPage)));
				} catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {

					logger.error("error fro HttpTaskPoster.submit ProjectScanTask.class", e);
				}

			}

		});

	}
}
