package com.sdyk.ai.crawler.specific.proLagou.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.ServiceProvider;
import com.sdyk.ai.crawler.model.Work;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import one.rewind.io.requester.exception.ProxyException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServiceProviderTask extends com.sdyk.ai.crawler.task.Task {

	static {
		registerBuilder(
				ServiceProviderTask.class,
				"https://pro.lagou.com/user/{{user_id}}.html",
				ImmutableMap.of("user_id", String.class),
				ImmutableMap.of("user_id","")
		);
	}

	public static String domain(){
		return "proLagou";
	}

	ServiceProvider serviceProvider;

	public ServiceProviderTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setBuildDom();

		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			//页面正常
			crawlerJob(doc);

		});
	}

	public void crawlerJob(Document doc) {

		List<com.sdyk.ai.crawler.task.Task> task = new ArrayList<>();

		serviceProvider = new ServiceProvider(getUrl());

		//名字
		serviceProvider.name = doc.select("#profile > div > div.profile_content.table_cell > div.profile_base > div > div > h3").text();

		//头像
		Set<String> fileUrl =new HashSet<>();
		List<String> fileName = new ArrayList<>();
		String image = doc.getElementsByClass("show_face").toString();
		String imageUrl = doc.getElementsByClass("show_face").attr("src");
		fileUrl.add(imageUrl);

		serviceProvider.head_portrait = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(imageUrl));

		BinaryDownloader.download(image,fileUrl,getUrl(),fileName);


		//原网站ID
		serviceProvider.origin_id = getUrl().split("user/")[1].replace(".html","");

		//小标签
		serviceProvider.tags = doc.select("#profile_additional > div.profile_technic > div.has_technic_data.has_data > ul").text();

		//介绍
		serviceProvider.content = doc.getElementsByClass("content vertical_border").html()
				+ doc.getElementsByClass("has_about_me has_data").html();

		//服务质量
		String serviceQuality = doc.getElementsByClass("center").text();
		serviceQuality = CrawlerAction.getNumbers(serviceQuality);
		serviceProvider.service_quality = Integer.valueOf(serviceQuality);

		//服务态度
		String serviceAttatude = doc.getElementsByClass("fr").text();
		serviceAttatude = CrawlerAction.getNumbers(serviceAttatude);
		serviceProvider.service_attitude = Integer.valueOf(serviceAttatude);

		//准时率
		String serviceSpeed = doc.getElementsByClass("fl").text();
		serviceSpeed = CrawlerAction.getNumbers(serviceSpeed);
		serviceProvider.service_speed = Integer.valueOf(serviceSpeed);

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
		serviceProvider.location = doc.select("#profile > div > div.profile_content.table_cell > div.profile_base > div > ul > li:nth-child(1)").text();

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

		//大标签
		String category = doc.getElementsByClass("technic_label").text()
				.replace(workExperience,"")
				.replace(type,"")
				.replace(serviceProvider.location,"");
		serviceProvider.category = category;

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
			work.title = title;

			//内容
			work.content = element.getElementsByClass("show_product_desc").html();

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
				HttpTaskPoster.getInstance().submit(ServiceProviderRatingTask.class,
						ImmutableMap.of("user_id", serviceProvider.origin_id));

			} catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {
				logger.error("error for HttpTaskPoster.submit ServiceProviderRatingTask", e);
			}

		}

		serviceProvider.insert();

	}

}
