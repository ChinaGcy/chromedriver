package com.sdyk.ai.crawler.specific.shichangbu.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.witkey.Case;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.shichangbu.task.Task;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import org.jsoup.nodes.Document;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CaseTask extends Task {

	static {
		registerBuilder(
				CaseTask.class,
				"http://www.shichangbu.com/portal.php{{case_id}}",
				ImmutableMap.of("case_id", String.class),
				ImmutableMap.of("case_id", "")
		);
	}

	public Case casemode;

	public CaseTask(String url) throws MalformedURLException, URISyntaxException {
		super(url);

		this.setBuildDom();

		this.setPriority(Priority.MEDIUM);
		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();
			crawlerJob(doc);

		});

	}

	public void crawlerJob(Document doc) {

		casemode = new Case(getUrl());

		String useUrl = "http://www.shichangbu.com/"
				+ doc.select("body > div.se-show.se-module.container > div.se-sh-con > div.se-sh-con-title > div > a:nth-child(2)")
				.attr("href");

		//服务商ID
		casemode.user_id = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(useUrl));

		//名字
		casemode.title = doc.select("div.se-sh-con-title").text();

		//价格
		String price = doc.select("span.se-sh-price ").text();
		if( price.contains("-") ) {
			casemode.budget_lb = Integer.valueOf(CrawlerAction.getNumbers(
					price.split("-")[0]
			));
			casemode.budget_ub = Integer.valueOf(CrawlerAction.getNumbers(
					price.split("-")[1]
			));
		}
		//不是区间
		else {
			casemode.budget_ub = casemode.budget_lb = Integer.valueOf(CrawlerAction.getNumbers(price));
		}

		//内容
		String contentHtml = doc.select("div.se-editcon").html();
		String context_url = "http://www.shichangbu.com/";
		Set<String> fileUrl =new HashSet<>();
		List<String> fileName = new ArrayList<>();

		//解析图片url
		Pattern p = Pattern.compile("<img src=\"(?<tUrl>.+?).jpg\"");
		Matcher m = p.matcher(contentHtml);

		//获取图片链接
		while(m.find()) {
			fileUrl.add(m.group("tUrl"));
			String[] urls = m.group("tUrl").split("/");
			fileName.add(urls[urls.length-1]);
		}
		String content = BinaryDownloader.download(contentHtml,fileUrl,context_url,fileName);
		casemode.content = content;

		try {
			casemode.insert();
		} catch (Exception e) {
			logger.error("error for casemode.insert()", casemode.toJSON(), e);
		}


	}

}
