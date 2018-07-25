package com.sdyk.ai.crawler.specific.shichangbu.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.witkey.Case;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.shichangbu.task.Task;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
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

		// 服务商ID
		casemode.user_id = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(useUrl));

		// 名字
		casemode.title = doc.select("div.se-sh-con-title").text();

		// 价格
		String price = doc.select("span.se-sh-price").text();
		if( price.contains("-") ) {
			casemode.budget_lb = Integer.valueOf(CrawlerAction.getNumbers(
					price.split("-")[0]
			));
			casemode.budget_ub = Integer.valueOf(CrawlerAction.getNumbers(
					price.split("-")[1]
			));
		}
		// 不是区间
		else {
			price = CrawlerAction.getNumbers(price);
			if( price != null && price.length() > 0 ){
				casemode.budget_ub = casemode.budget_lb = Integer.valueOf(CrawlerAction.getNumbers(price));
			}
		}

		// 分类
		casemode.category = doc.select("span.se-sh-label").text();

		// 描述
		String contentHtml = doc.select("div.se-editcon").html();
		Set<String> extraUrls_img = new HashSet<>();

		contentHtml = StringUtil.cleanContent(contentHtml, extraUrls_img);

		// 图片下载
		String content = BinaryDownloader.download(contentHtml, extraUrls_img, getUrl());
		casemode.content = content;

		try {
			casemode.insert();
		} catch (Exception e) {
			logger.error("error for casemode.insert()", casemode.toJSON(), e);
		}


	}

}
