package com.sdyk.ai.crawler.specific.shichangbu.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.sdyk.ai.crawler.model.witkey.Work;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.shichangbu.task.Task;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class WorkTask extends Task {

	public static long MIN_INTERVAL = 24 * 60 * 60 * 1000L;

	static {
		registerBuilder(
				WorkTask.class,
				"http://www.shichangbu.com/portal.php{{work_id}}",
				ImmutableMap.of("work_id", String.class),
				ImmutableMap.of("work_id", "")
		);
	}

	public WorkTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setPriority(Priority.MEDIUM);

		// 检测异常
		this.setValidator((a,t) -> {

			String src = getResponse().getText();
			if( src.contains("账号登陆")
					&& src.contains("第三方登陆")){

				throw new AccountException.Failed(a.accounts.get("shichangbu.com"));
			}
		});

		this.setNoFetchImages();

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

		Work work = new Work(getUrl());

		String useUrl = "http://www.shichangbu.com/" +
				doc.select("body > div.se-show.se-module.container > div.se-sh-con > div.se-sh-con-title > div > a:nth-child(1)")
				.attr("href");

		//服务商ID
		work.user_id = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(useUrl));

		//案例名称
		String title_name = doc.getElementsByClass("se-sh-con-title").text();
		String name = doc.select("div.fr").text();
		work.title = title_name.replace(name, "");

		// 标签
		Elements elements = doc.select("div.mnl-right-tab > ul > li");
		StringBuffer tagr = new StringBuffer();
		for(Element element : elements){
			tagr.append(element.text());
			tagr.append(",");
		}
		if( tagr.length() > 1 ){
			work.tags = tagr.substring(0, tagr.length()-1);
		}

		//浏览次数
		String viewNum = doc.select("span.se-sh-star.iconimg-eye").text();
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
		String contentHtml = doc.select("div.se-editcon").html()
				.replaceAll(doc.select("div.mnl-right-tab").html(), "")
				.replaceAll("使用模块：", "");
		Set<String> extraUrls_img = new HashSet<>();

		// 时间
		String pub = doc.select("span.se-sh-time.iconimg-clock").text();

		try {
			work.pubdate = DateFormatUtil.parseTime(pub);
		} catch (ParseException e) {
			logger.error("error fro String to date", e);
		}


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
