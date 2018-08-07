package com.sdyk.ai.crawler.specific.oschina.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.witkey.ServiceProvider;
import com.sdyk.ai.crawler.model.witkey.ServiceProviderRating;
import com.sdyk.ai.crawler.model.witkey.Work;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.oschina.task.Task;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import com.sdyk.ai.crawler.util.LocationParser;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ScheduledChromeTask;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceProviderTask extends Task {

	public static long MIN_INTERVAL = 24 * 60 * 60 * 1000L;

	public static List<String> crons = Arrays.asList("* * */1 * *", "* * */2 * *", "* * */4 * *", "* * */8 * *");

	static {
		registerBuilder(
				ServiceProviderTask.class,
				"https://zb.oschina.net/profile/index.html?u={{user_id}}&t=d",
				ImmutableMap.of("user_id", String.class),
				ImmutableMap.of("user_id", "")
		);
	}

	public ServiceProviderTask(String url) throws MalformedURLException, URISyntaxException {
		super(url);

		this.setPriority(Priority.HIGH);

		this.setNoFetchImages();

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

		ServiceProvider serviceProvider = new ServiceProvider(getUrl());

		//原网站ID
		serviceProvider.origin_id = getUrl().split("html?")[1];

		//名字
		String name	= doc.select("#profile > div.show-for-medium.pc-profile > div.user-box.u-bg-1 > div > span.font-20.font-bold.mb-3").text();
		if( name == null || name.length() < 1  ){
			name = doc.select("#profile > div.show-for-medium.pc-profile > div.user-box.u-bg-2 > div > span.font-20.font-bold.mb-3").text();
		}
		serviceProvider.name = name;

		// 头像
		String imageUrl = doc.getElementsByClass("u-icon").attr("src");
		Map<String, String> url_filename = new HashMap<>();
		if( imageUrl != null ){
			url_filename.put(imageUrl, "head_portrait");
			serviceProvider.head_portrait = BinaryDownloader.download(getUrl(), url_filename);
		}

		serviceProvider.domain_id = 5;

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

		serviceProvider.platform_certification = platformCertification.substring(0, platformCertification.length()-2);

		//地理位置
		LocationParser parser = LocationParser.getInstance();
		serviceProvider.location = doc.select("#profile > div.show-for-medium.pc-profile > div.user-box.u-bg-1 > div > span:nth-child(3)").text();
		if( serviceProvider.location == null || serviceProvider.location.length() < 1){
			serviceProvider.location = doc.select("#profile > div.show-for-medium.pc-profile > div.user-box.u-bg-2 > div > span:nth-child(3)").text();
		}
		serviceProvider.location = parser.matchLocation(serviceProvider.location).get(0).toString();

		//描述
		serviceProvider.content = StringUtil.cleanContent(doc.select("#profile > div.show-for-medium.pc-profile > div.mtb-large > div > div > div.el-col.el-col-18 > div:nth-child(1) > div.content > div > div").text(), new HashSet<>());

		//岗位，大标签，小标签
		Elements elements = doc.getElementsByClass("skill-row");
		String tags = "";

		for(Element element : elements) {

			String skillLabel = element.getElementsByClass("skill-label").text();
			String skillItem = element.getElementsByClass("skill-item").text();

			//岗位
			if( skillLabel.contains("岗位") ) {
				serviceProvider.position = skillItem;
			}
			//大标签
			else if( skillLabel.contains("类型") ){
				serviceProvider.category = skillItem.replace("、", ",");
			}
			//小标签拼接
			else if( skillLabel.contains("语言") || skillLabel.contains("技能") || skillLabel.contains("中间件")){
				if( !tags.contains(skillItem) ){
					tags = tags+ skillItem + ",";
				}
			}

		}

		//小标签
		if( tags.length() > 0 ){
			serviceProvider.tags = tags.substring(0, tags.length()-1).replace("、", ",");
		}

		Elements ratingGrade = doc.getElementsByClass("comment-item");
		for( Element r : ratingGrade ){

			//工作质量
			if( r.text().contains("工作质量 ") ) {
				String serviceQuality = CrawlerAction.getNumbers(r.text());
				if( !"".equals(serviceQuality) ){
					serviceProvider.service_quality = Integer.valueOf(serviceQuality);
				}
			}
			//服务态度
			else if( r.text().contains("沟通能力 ") ){
				String serviceAttitude = CrawlerAction.getNumbers(r.text());
				if( !"".equals(serviceAttitude) ){
					serviceProvider.service_attitude = Integer.valueOf(serviceAttitude);
				}
			}
			//服务速度
			else if( r.text().contains("响应速度 ") ){
				String serviceSpeed = CrawlerAction.getNumbers(r.text());
				if( !"".equals(serviceSpeed) ){
					serviceProvider.service_speed = Integer.valueOf(serviceSpeed);
				}
			}
			//雇主推荐
			else if( r.text().contains("下次合作意愿 ") ){
				String rcmdNum = CrawlerAction.getNumbers(r.text());
				if( !"".equals(rcmdNum) ){
					serviceProvider.rcmd_num = Integer.valueOf(rcmdNum);
				}
			}

		}

		//评论
		Elements ratingElements = doc.getElementsByClass("comment-list");

		Set<String> RatingsSet = new HashSet<>();

		int i = 0;
		for( Element e : ratingElements ){

			String projectName = e.select("div:nth-child(2) > div:nth-child(1) > div:nth-child(1) > h3").text();

			if( RatingsSet.add(projectName) ) {

				i++;
				ServiceProviderRating serviceProviderRating = new ServiceProviderRating( getUrl() + "&rating" + i);

				//服务商ID
				serviceProviderRating.service_provider_id = serviceProvider.id;

				//项目名称
				serviceProviderRating.project_name = e.select("div:nth-child(2) > div:nth-child(1) > div:nth-child(1) > h3").text();

				//发布时间
				try {
					//类型转换
					serviceProviderRating.pubdate = DateFormatUtil.parseTime(
							e.getElementsByClass("comment-time").text());
				} catch (ParseException e1) {
					logger.error("error for string to date", e);
				}

				//内容
				serviceProviderRating.content = e.select("div.desc").text().replaceAll("评价描述：", "");

				/*//标签
				serviceProviderRating.tags = e.getElementsByClass("tags").text();*/

				//打分
				Elements star = e.getElementsByClass("el-rate__item");
				serviceProviderRating.rating = Double.valueOf(star.size());

				try {
					serviceProviderRating.insert();
				} catch (Exception e1) {
					logger.error("error for serviceProviderRating.insert()", e1);
				}

			}

		}

		//项目
		Elements workElements = doc.select("div.case-item");
		int j =0;
		for( int n = workElements.size()/2 ; n<workElements.size(); n++  ){

			Element e = workElements.get(n);

			j++;
			Work work = new Work( getUrl() + "&work" + j);

			//标题
			work.title = e.select("h3.mb-mini").text();

			//服务商ID
			work.user_id = serviceProvider.id;

			Elements workItems = e.select("div.case-item-row");

			for( Element e1 : workItems ){

				String count = e1.text();

				if( count.contains("我的角色") ){
					work.position = count.split("我的角色：")[1].replace("、", ",");
				}
				//小标签
				else if ( count.contains("应用技术：") ) {
					work.tags = count.replace("应用技术：","").replace("、", ",");
				}
				//简介
				else if ( count.contains("项目简介：") ) {
					work.content = "<p>" + count + "</p>";
				}
				//外网地址
				else if( count.contains("演示地址") ) {
					work.external_url = e1.select("a").attr("href");
				}
				// 截图
				else if( count.contains("截图") ){
					Elements imgs = e1.select("div.images > img");
					Map<String, String> map = new HashMap<>();
					for( Element img : imgs ){
						map.put(img.attr("src"), null);
					}
					work.attachment_ids = BinaryDownloader.download(getUrl(), map);

				}

			}

			Elements elements1 = workItems.get(0).select("span");

			// 周期
			work.time_limit = elements1.get(1).text();


			try {
				work.insert();
			} catch ( Exception e2 ) {
				logger.error("error for work.insert();", e2);
			}

		}

		serviceProvider.position = serviceProvider.position.replace("、", ",");
		if( serviceProvider.name.contains("公司") ){
			serviceProvider.company_name = serviceProvider.name;
		}
		try {
			boolean status = serviceProvider.insert();

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
		} catch ( Exception e ) {
			logger.error("error for serviceProvider.insert()", e);
		}

	}
}
