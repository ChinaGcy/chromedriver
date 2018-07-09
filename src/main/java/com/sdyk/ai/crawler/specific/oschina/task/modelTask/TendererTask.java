package com.sdyk.ai.crawler.specific.oschina.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.witkey.Tenderer;
import com.sdyk.ai.crawler.specific.oschina.task.Task;
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

		//甲方头像
		Set<String> fileUrl =new HashSet<>();
		List<String> fileName = new ArrayList<>();

		String headPortraitHtml = doc.getElementsByClass("u-icon").toString();
		String headPortraitUrl = doc.getElementsByClass("u-icon").attr("src");

		fileUrl.add(headPortraitUrl);
		fileName.add("head_portrait");
		String oUrl = "https://zb.oschina.net/";

		tenderer.head_portrait = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(headPortraitUrl));

		BinaryDownloader.download(headPortraitHtml,fileUrl,oUrl,fileName);

		try{
			tenderer.insert();
		} catch (Exception e){
			logger.error("error for tenderer.insert();", e);
		}
	}

}
