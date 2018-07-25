package com.sdyk.ai.crawler.binary.test;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import one.rewind.io.requester.task.ChromeTask;
import org.jsoup.nodes.Document;

import javax.lang.model.util.Elements;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BinaryTask extends ChromeTask {

	public static long MIN_INTERVAL = 1000L;

	static {
		registerBuilder(
				BinaryTask.class,
				"https://www.clouderwork.com/jobs/{{Id}}",
				ImmutableMap.of("Id", String.class),
				ImmutableMap.of("Id", "b0e6ace68bbfc891")
		);
	}


	public BinaryTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setBuildDom();

		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			String contain = doc
					.select("#project-detail > div > div.main-detail > section > div.files > p > span > a:nth-child(1)")
							.toString();

			System.err.println("原始附件代码 ：" + contain);

			Set<String> fileUrl =new HashSet<>();
			List<String> fileName = new ArrayList<>();

			fileUrl.add(doc.select("#project-detail > div > div.main-detail > section > div.files > p > span > a:nth-child(1)")
					.attr("href"));
			fileName.add(doc.select("#project-detail > div > div.main-detail > section > div.files > p > span > a:nth-child(1)")
					.attr("download"));

			String description = BinaryDownloader.download(contain,fileUrl,url,fileName);

			System.err.println("处理后附件代码" + description);

		});

	}
}
