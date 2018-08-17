package com.sdyk.ai.crawler.specific.oschina.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.witkey.Tenderer;
import com.sdyk.ai.crawler.specific.oschina.task.Task;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import com.sdyk.ai.crawler.util.LocationParser;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ScheduledChromeTask;
import org.jsoup.nodes.Document;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TendererTask extends Task {

	public static long MIN_INTERVAL = 24 * 60 * 60 * 1000L;

	public static List<String> crons = Arrays.asList("* * */1 * *", "* * */2 * *", "* * */4 * *", "* * */8 * *");

	static {
		registerBuilder(
				TendererTask.class,
				"https://zb.oschina.net/profile/index.html?u={{tenderer_id}}&t=p",
				ImmutableMap.of("tenderer_id", String.class),
				ImmutableMap.of("tenderer_id", ""),
				true,
				Priority.HIGH
		);
	}



	public TendererTask(String url) throws MalformedURLException, URISyntaxException {
		super(url);

		// 检测异常
		this.setValidator((a,t) -> {

			String src = getResponse().getText();
			if( src.contains("登陆") && src.contains("忘记密码") ){

				throw new AccountException.Failed(a.accounts.get(t.getDomain()));
			}
		});

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			String src = getResponse().getText();

			if( src.contains("对不起") || src.contains("错误了") || src.contains("错误码") ){
				return;
			}
			//页面正常
			else {
				crawlerJob(doc, (ChromeTask)t);
			}

		});

	}

	public void crawlerJob(Document doc, ChromeTask t) throws Exception {

		Tenderer tenderer = new Tenderer(getUrl());

		//甲方名称
		tenderer.name = doc.select(
				"#profile > div.show-for-medium.pc-profile > div.user-box.u-bg-1 > div > span.font-20.font-bold.mb-3").text();
		if( tenderer.name == null || tenderer.name.length() < 1 ){
			tenderer.name = doc.select(
					"#profile > div.show-for-medium.pc-profile > div.user-box.u-bg-2 > div > span.font-20.font-bold.mb-3").text();
		}

		tenderer.origin_id = getUrl().split("html?")[1];

		//甲方地址
		tenderer.location = doc.select("#profile > div.show-for-medium.pc-profile > div.user-box.u-bg-1 > div > span:nth-child(3)").text();
		if( tenderer.location == null || tenderer.location.length() < 1 ){
			tenderer.location = doc.select("#profile > div.show-for-medium.pc-profile > div.user-box.u-bg-2 > div > span:nth-child(3)").text();
		}
		LocationParser parser = LocationParser.getInstance();
		tenderer.location = parser.matchLocation(tenderer.location).size() > 0 ? parser.matchLocation(tenderer.location).get(0).toString() : null;

		tenderer.domain_id = 5;

		//认证情况
		String certification = doc.getElementsByClass("user-icons").toString();

		Pattern pattern = Pattern.compile("title=\"(?<count>.+?)\">");
		Matcher matcher = pattern.matcher(certification);

		List<String> platformCertification = new ArrayList<>();

		Set<String> certificationSet = new HashSet<>();

		while( matcher.find() ){
			if( !matcher.group("count").contains("未") ){
				certificationSet.add(matcher.group("count"));
			}
		}

		for( String s : certificationSet ){
			platformCertification.add(s);
		}

		tenderer.platform_certification = platformCertification;

		// 头像
		String imageUrl = doc.getElementsByClass("u-icon").attr("src");
		Map<String, String> url_filename = new HashMap<>();
		url_filename.put(imageUrl, "head_portrait");
		List<String> headList = BinaryDownloader.download(getUrl(), url_filename);
		if( headList != null ){
			tenderer.head_portrait  = headList.get(0);
		}

		try{

			tenderer.category.replace(" ", "");
			boolean status = tenderer.insert();

			ScheduledChromeTask st = t.getScheduledChromeTask();

			// 第一次抓取生成定时任务
			if(st == null) {

				try {
					st = new ScheduledChromeTask(t.getHolder(this.init_map), crons);
					st.start();
				} catch (Exception e) {
					logger.error("error for creat ScheduledChromeTask", e);
				}

			}
			else {
				if( !status ){
					st.degenerate();
				}
			}

		} catch (Exception e){
			logger.error("error for tenderer.insert();", e);
		}


	}

}
