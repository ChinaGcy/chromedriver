package com.sdyk.ai.crawler.specific.proLagou.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.witkey.ServiceProvider;
import com.sdyk.ai.crawler.model.witkey.Work;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import com.sdyk.ai.crawler.util.LocationParser;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.ChromeTaskScheduler;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import one.rewind.io.requester.task.ScheduledChromeTask;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;

public class ServiceProviderTask extends com.sdyk.ai.crawler.task.Task {

	public static long MIN_INTERVAL = 24 * 60 * 60 * 1000L;

	public static List<String> crons = Arrays.asList("* * */1 * *", "* * */2 * *", "* * */4 * *", "* * */8 * *");

	static {
		registerBuilder(
				ServiceProviderTask.class,
				"https://pro.lagou.com/user/{{user_id}}.html",
				ImmutableMap.of("user_id", String.class),
				ImmutableMap.of("user_id","")
		);
	}

	public ServiceProviderTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setBuildDom();

		this.setNoFetchImages();

		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			//页面正常
			crawlerJob(doc, (ChromeTask)t);

		});
	}

	public void crawlerJob(Document doc, ChromeTask t) throws Exception {

		ServiceProvider serviceProvider = new ServiceProvider(getUrl());

		//名字
		String name_position = doc.select("#profile > div > div.profile_content.table_cell > div.profile_base > div > div > h3").text();

		serviceProvider.domain_id = 3;

		String[] position = name_position.split(" ");
		serviceProvider.position = position[position.length - 1];

		serviceProvider.name = name_position.replace(serviceProvider.position, "");

		// 头像
		String imageUrl = doc.getElementsByClass("show_face").attr("src");
		Map<String, String> url_filename = new HashMap<>();
		if( imageUrl != null && imageUrl.length() > 0 ){
			url_filename.put(imageUrl, "head_portrait");
			serviceProvider.head_portrait = BinaryDownloader.download(getUrl(), url_filename);
		}


		//原网站ID
		serviceProvider.origin_id = getUrl().split("user/")[1].replace(".html","");

		//大标签
		Elements eTags = doc.select("ul.technic_label > li");
		StringBuffer tags = new StringBuffer();

		for( int i =3; i < eTags.size(); i++ ){
			tags.append(eTags.get(i).text());
			tags.append(",");
		}

		if( tags.length() > 1 ){
			serviceProvider.category = tags.substring(0, tags.length() - 1);

		}

		//介绍
		Set<String> set = new HashSet<>();
		String s = doc.select("pre.has_about_me.has_data").html();
		serviceProvider.content = StringUtil.cleanContent(doc.select("pre.has_about_me.has_data").html(), set);
		if( set.size() > 0 ){
			serviceProvider.content = BinaryDownloader.download(serviceProvider.content, set, getUrl());
		}

		// 平台认证
		serviceProvider.platform_certification = doc.select("span.check_status.active").text();

		//服务质量
		String serviceQuality = doc.getElementsByClass("center").text();
		serviceQuality = CrawlerAction.getNumbers(serviceQuality);
		if( serviceQuality.length() > 1 ){
			serviceProvider.service_quality = Integer.valueOf(serviceQuality);
		}

		//服务态度
		String serviceAttatude = doc.getElementsByClass("fr").text();
		serviceAttatude = CrawlerAction.getNumbers(serviceAttatude);
		serviceProvider.service_attitude = Integer.valueOf(serviceAttatude);

		//准时率
		String serviceSpeed = doc.getElementsByClass("fl").text();
		serviceSpeed = CrawlerAction.getNumbers(serviceSpeed);
		if( serviceSpeed.length() > 1 ){
			serviceProvider.service_speed = Integer.valueOf(serviceSpeed);
		}

		//价格/天
		String pricePerDay = doc.getElementsByClass("form_value").text();
		pricePerDay = CrawlerAction.getNumbers(pricePerDay);
		serviceProvider.price_per_day = Double.valueOf(pricePerDay);

		//顾客评分
		String rating = doc.select("#profile > div > div.profile_sidebar.table_cellr > div > div:nth-child(5) > span.form_value").text();
		rating = CrawlerAction.getNumbers(rating);
		if( rating!=null && !"".equals(rating) ){
			serviceProvider.rating = Integer.valueOf(rating);
		}

		//平台项目数
		String projectNum = doc.select("#profile > div > div.profile_sidebar.table_cellr > div > div:nth-child(6) > span.form_value").text();
		projectNum = CrawlerAction.getNumbers(projectNum);
		if( projectNum!=null && !"".equals(projectNum) ){
			serviceProvider.project_num = Integer.valueOf(projectNum);
		}

		//地点
		LocationParser parser = LocationParser.getInstance();
		serviceProvider.location = doc.select("#profile > div > div.profile_content.table_cell > div.profile_base > div > ul > li:nth-child(1)").text();
		serviceProvider.location = parser.matchLocation(serviceProvider.location).size() > 0 ? parser.matchLocation(serviceProvider.location).get(0).toString() : null;

		//类型
		String type = doc.select("#profile > div > div.profile_content.table_cell > div.profile_base > div > ul > li:nth-child(2)").text();
		if( type.contains("团队") ){
			serviceProvider.type = "团队";
		}
		//没有团队字样
		else {
			serviceProvider.type = "个人";
		}

		//工作年限
		String workExperience = doc.select("#profile > div > div.profile_content.table_cell > div.profile_base > div > ul > li:nth-child(3)").text();
		workExperience = CrawlerAction.getNumbers(workExperience);
		if( workExperience != null && !"".equals(workExperience) ){
			serviceProvider.work_experience = Integer.valueOf(workExperience);
		}

		// 标签
		Elements elements_t = doc.select("div.has_technic_data.has_data > ul > li");
		StringBuffer targ = new StringBuffer();
		for( Element element : elements_t ){
			targ.append(element.text());
			targ.append(",");
		}

		if(targ != null && targ.length() > 1){
			serviceProvider.tags = targ.substring(0, targ.length() - 1);
		}

		//评价数
		String ratingNum = doc.select("#profile > div > div.profile_sidebar.table_cellr > div > div.comments_base > div.title > strong").text();
		ratingNum = CrawlerAction.getNumbers(ratingNum);
		if( ratingNum!=null && !"".equals(ratingNum) ){
			serviceProvider.rating_num = Integer.valueOf(ratingNum);
		}

		//项目
		Elements elements = doc.getElementsByClass("product_item");
		int i = 0;
		for(Element element : elements){
			i++;
			Work work = new Work(getUrl() + "?work=" + i);

			//大类
			String workCategory = element.getElementsByClass("tags").text();
			work.category = workCategory;

			//标题
			String title = element.getElementsByClass("title").text().replace(workCategory, "");

			String[] arg = title.split("•");
			if( arg.length > 1 ){
				work.tags = arg[1].replace("、", "").replace("与", "").replace("及", "");
			}

			work.title = title.split("•")[0];

			//内容
			work.content = "</p>" + element.getElementsByClass("show_product_desc").text() + "</p>";

			String url = element.select("img").attr("src");
			Map<String, String> map = new HashMap<>();
			if( url != null && url.length() > 0 ){
				map.put(url, "workImg");
				work.attachment_ids = BinaryDownloader.download(getUrl(), map);
			}

			//外部链接
			work.external_url = element.getElementsByClass("quick_link").attr("href");

			//用户ID
 			work.user_id = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(getUrl()));

			work.insert();
		}

		//评价
		String more = doc.getElementsByClass("more").text();
		if( more != null && !"".equals(more) ){

			try {

				//设置参数
				Map<String, Object> init_map = new HashMap<>();
				ImmutableMap.of("user_id", serviceProvider.origin_id);

				Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.proLagou.task.modelTask.ServiceProviderRatingTask");

				//生成holder
				ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

				//提交任务
				ChromeDriverDistributor.getInstance().submit(holder);

			} catch ( Exception e) {

				logger.error("error for submit scanTaskServiceScanTask.class", e);
			}
		}

		boolean status = serviceProvider.insert();

		/*ScheduledChromeTask st = t.getScheduledChromeTask();

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
		}*/

	}

}
