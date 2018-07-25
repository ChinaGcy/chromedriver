package com.sdyk.ai.crawler.specific.oschina.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.witkey.Tenderer;
import com.sdyk.ai.crawler.specific.oschina.task.Task;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import org.jsoup.nodes.Document;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TendererTask extends Task {

	static {
		registerBuilder(
				TendererTask.class,
				"https://zb.oschina.net/profile/index.html?u={{tenderer_id}}&t=p",
				ImmutableMap.of("tenderer_id", String.class),
				ImmutableMap.of("tenderer_id", "")
		);
	}

	Tenderer tenderer;

	public TendererTask(String url) throws MalformedURLException, URISyntaxException {
		super(url);

		this.setBuildDom();

		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			String src = getResponse().getText();

			tenderer = new Tenderer(getUrl());

			if( src.contains("对不起") || src.contains("错误了") || src.contains("错误码") ){
				return;
			}
			//页面正常
			else {
				crawlerJob(doc, src);
			}

		});

	}

	public void crawlerJob(Document doc, String src){

		//甲方名称
		tenderer.name = doc.select("#profile > div.show-for-medium.pc-profile > div.user-box.u-bg-1 > div > span.font-20.font-bold.mb-3").text();

		tenderer.origin_id = getUrl().split("html?")[1];

		//甲方地址
		tenderer.location = doc.select("#profile > div.show-for-medium.pc-profile > div.user-box.u-bg-1 > div > span:nth-child(3)").text();

		//认证情况
		String certification = doc.getElementsByClass("user-icons").toString();

		Pattern pattern = Pattern.compile("title=\"(?<count>.+?)\">");
		Matcher matcher = pattern.matcher(certification);

		StringBuffer platformCertification = new StringBuffer();

		Set<String> certificationSet = new HashSet<>();

		while( matcher.find() ){
			if( !matcher.group("count").contains("未") ){
				certificationSet.add(matcher.group("count"));
			}
		}

		for( String s : certificationSet ){
			platformCertification.append(s);
			platformCertification.append(",");
		}

		tenderer.platform_certification = platformCertification.substring(0, platformCertification.length()-1);

		// 头像
		String imageUrl = doc.getElementsByClass("u-icon").attr("src");
		Map<String, String> url_filename = new HashMap<>();
		url_filename.put(imageUrl, "head_portrait");
		tenderer.head_portrait  = BinaryDownloader.download(getUrl(), url_filename);

		try{
			tenderer.insert();
		} catch (Exception e){
			logger.error("error for tenderer.insert();", e);
		}
	}

}
