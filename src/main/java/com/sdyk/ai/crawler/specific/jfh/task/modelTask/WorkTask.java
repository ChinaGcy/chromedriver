package com.sdyk.ai.crawler.specific.jfh.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.witkey.Work;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.jfh.task.Task;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.exception.AccountException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class WorkTask extends Task {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				WorkTask.class,
				"https://www.jfh.com/jfhrm/buinfo/showbucaseinfo?uuidSecret={{uuidSecret}}",
				ImmutableMap.of("uuidSecret", String.class, "uId", String.class),
				ImmutableMap.of("uuidSecret", "MTQxODM7NDQ", "uId", ""),
				true,
				Priority.MEDIUM
		);
	}

	public WorkTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setValidator((a,t) -> {

			String src = getResponse().getText();
			if( src.contains("Log in JointForce") && src.contains("Don't have an account?") ){

				throw new AccountException.Failed(a.accounts.get("jfh.com"));

			}

		});

		//this.setNoFetchImages();

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			String uid = t.getStringFromInitMap("uId");

			crawler(doc, uid);

		});

	}

	public void crawler(Document doc, String uId) {

 		Work work = new Work(getUrl());

		work.user_id = uId;

		// 标题
		work.title = doc.select("h1.txtellipsis").text();

		String[] tenderName = doc.select("span.txtellipsis").text().split("：");
		if (tenderName.length > 1) {
			work.tenderer_name = tenderName[1];
		}

		// 价格
		String price = doc.select("em.red.pdlr4").text();
		price = CrawlerAction.getNumbers(price);
		if (!"".equals(price)) {
			work.price = Double.valueOf(price);
		}

		work.external_url = doc.select("div.websiteLink > a").attr("href");

		// 简介
		work.content = "<p>" + doc.select("p.pcontent").text() + "</p>";

		// 行业
		work.category = doc.select(
				"#wrap > div.mine_package_left > div.mine_package_mainCon > div:nth-child(2) > ul > li > span").text();

		Map<String, String> map = new HashMap<>();
		Elements elements = doc.select("#small_pic > li > img");
		for( Element element : elements ){
			map.put(element.attr("src"), "");
		}

		// 下载附件
		work.attachment_ids = BinaryDownloader.download(getUrl(), map);

		try {
			work.insert();
		} catch (Exception e) {
			logger.error("erroe for work.insert", e);
		}

	}
}
