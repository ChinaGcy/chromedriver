package com.sdyk.ai.crawler.specific.shichangbu.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.witkey.Work;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.shichangbu.task.Task;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import com.sdyk.ai.crawler.util.StringUtil;
import org.jsoup.nodes.Document;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorkTask extends Task {

	static {
		registerBuilder(
				WorkTask.class,
				"http://www.shichangbu.com/portal.php{{work_id}}",
				ImmutableMap.of("work_id", String.class),
				ImmutableMap.of("work_id", "")
		);
	}

	public Work work;

	public WorkTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setBuildDom();

		this.setPriority(Priority.MEDIUM);

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();
			crawlerJob(doc);

		});
	}

	/**
	 * 抓取页面
	 * @param doc
	 */
	public void crawlerJob(Document doc) {

		work = new Work(getUrl());

		String useUrl = "http://www.shichangbu.com/" +
				doc.select("body > div.se-show.se-module.container > div.se-sh-con > div.se-sh-con-title > div > a:nth-child(1)")
				.attr("href");

		//服务商ID
		work.user_id = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(useUrl));

		//案例名称
		work.title = doc.getElementsByClass("se-sh-con-title").text();

		//浏览次数
		String viewNum = doc.select("span.se-sh-star iconimg-eye").text();
		if( viewNum != null && !"".equals(viewNum) ) {
			work.view_num = Integer.valueOf(CrawlerAction.getNumbers(viewNum));
		}

		//点赞数
		String likeNum = doc.select("span.likenum").text();
		likeNum = CrawlerAction.getNumbers(likeNum);
		if( likeNum != null && !"".equals(likeNum) ){
			work.like_num = Integer.valueOf(likeNum);
		}

		// 描述
		String contentHtml = doc.select("div.se-editcon").html();
		Set<String> extraUrls_img = new HashSet<>();

		contentHtml = StringUtil.cleanContent(contentHtml, extraUrls_img);

		// 图片下载
		work.content = BinaryDownloader.download(contentHtml, extraUrls_img, getUrl());

		try {
			work.insert();
		} catch (Exception e) {
			logger.error("error for casemode.insert()", work.toJSON(), e);
		}


	}

}
