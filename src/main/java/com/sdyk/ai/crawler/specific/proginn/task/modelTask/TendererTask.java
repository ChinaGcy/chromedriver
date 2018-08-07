package com.sdyk.ai.crawler.specific.proginn.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.witkey.Tenderer;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.proginn.task.Task;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import com.sdyk.ai.crawler.util.LocationParser;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ScheduledChromeTask;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TendererTask extends Task {

	public static List<String> crons = Arrays.asList("* * */1 * *", "* * */2 * *", "* * */4 * *", "* * */8 * *");

	public static long MIN_INTERVAL = 24 * 60 * 60 * 1000L;

	static {
		registerBuilder(
				TendererTask.class,
				"https://www.proginn.com/{{tenderer_id}}",
				ImmutableMap.of("tenderer_id", String.class),
				ImmutableMap.of("tenderer_id", "")
		);
	}

	public TendererTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setPriority(Priority.HIGH);

		this.setNoFetchImages();

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			crawler(doc, (ChromeTask)t);

		});
	}

	public void crawler( Document doc, ChromeTask t ) throws Exception {

		Tenderer tenderer = new Tenderer(getUrl());

		System.out.println("甲方url" + getUrl());

		if( getUrl().contains("wo") ){
			tenderer.origin_id = getUrl().split("wo/")[1];
		}
		else {
			tenderer.origin_id = getUrl().split("u/")[1];
		}


		tenderer.name = doc.select("a.header").text();
		if( tenderer.name == null || tenderer.name.length() < 1 ){
			tenderer.name = doc.select("a.hd").text();
		}

		tenderer.content = "<p>" + doc.select("p.desc").text() + "</p>";

		tenderer.company_name = doc.select("span.name-txt").text();

		tenderer.platform_certification = doc.select("span.tag").text();

		Elements elements = doc.select("div.rate > p");
		for(Element element : elements){
			if( element.text().contains("项目") ){
				String rat = CrawlerAction.getNumbers(element.text());
				if( rat.length() > 0 ){
					tenderer.success_ratio = Integer.valueOf(rat);
				}
			}
			else if( element.text().contains("雇佣") ){
				String rat = CrawlerAction.getNumbers(element.text());
				if( rat.length() > 0 ){
					tenderer.selection_ratio = Integer.valueOf(rat);
				}
			}
		}

		tenderer.domain_id = 9;

		//介绍
		String introduction = doc.select("div.introduction").text();

		String[] introductions = introduction.split(" ");
		if( introductions.length > 2 ){

			tenderer.location = LocationParser.getInstance().matchLocation(introductions[0]).size() > 0 ?
					LocationParser.getInstance().matchLocation(introductions[0]).get(0).toString() : null;

			if( !introductions[1].contains("远程") ){
				tenderer.company_name = introductions[1];
			}

		}
		//信息不全时
		else {

			Elements icon1 = doc.select("div.introduction > i.arrow.location.icon");

			if( icon1 != null && icon1.size() > 0 ){
				tenderer.location = LocationParser.getInstance().matchLocation(introductions[0]).size() > 0 ?
						LocationParser.getInstance().matchLocation(introductions[0]).get(0).toString() : null;
			}
			else{
				tenderer.company_name = introductions[0];
			}
		}
		if( tenderer.location == null || tenderer.location.length() < 1 ){
			tenderer.location = LocationParser.getInstance().matchLocation(tenderer.name).size() > 0 ?
					LocationParser.getInstance().matchLocation(tenderer.name).get(0).toString() : null;
		}

		// 头像
		String img = doc.select("a.ui.tiny.circular.image > img").attr("src");
		Map<String, String> map = new HashMap<>();
		map.put(img, "head_portrait");
		tenderer.head_portrait = BinaryDownloader.download(getUrl(), map);

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

	}

}
