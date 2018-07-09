package com.sdyk.ai.crawler.specific.jfh.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.specific.jfh.task.modelTask.ServiceProviderTask;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.exception.ProxyException;
import org.jsoup.nodes.Document;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ServiceScanTask extends ScanTask {

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
					HttpTaskPoster.getInstance().submit(ServiceProviderTask.class,
							ImmutableMap.of("servicer_id", String.valueOf(i)));
				} catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {

					logger.error("error fro HttpTaskPoster.submit ServiceProviderTask.class", e);
				}

			}

		});

	}
}
