package com.sdyk.ai.crawler.specific.proginn.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.witkey.Work;
import com.sdyk.ai.crawler.specific.proginn.task.Task;
import org.jsoup.nodes.Document;

public class WorkTask extends Task {

	static {
		registerBuilder(
				ServiceProviderTask.class,
				"https://www.proginn.com{{work_id}}",
				ImmutableMap.of("work_id", String.class),
				ImmutableMap.of("work_id", "")
		);
	}

	Work work;

	public WorkTask(String url) throws Exception {

		super(url);

		this.setBuildDom();

		this.setPriority(Priority.MEDIUM);

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			String uId = doc.select("div.nickname > a").attr("href");

			crawler(doc, uId);

		});

	}

	public void crawler( Document doc, String uId ){

		work = new Work(getUrl());

		work.user_id = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(uId));

		//外部链接
		work.external_url = doc.select("a.link").attr("href");

		//大类
		work.category = doc.select("p.sub-title").text();

		//标签
		work.tags = doc.select("p.functions").text();

		//标题
		work.title = doc.select("p.title").text().replace("查看更多作品 >","");

		//内容
		work.content = doc.select("div.desc").text();

		try {
			work.insert();
		} catch (Exception e) {
			logger.error("error for work.insert", e);
		}
	}

}
