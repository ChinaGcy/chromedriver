package com.sdyk.ai.crawler.specific.oschina.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.witkey.Project;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.oschina.task.Task;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectTask extends Task {

	static {
		registerBuilder(
				ProjectTask.class,
				"https://zb.oschina.net/{{project_id}}",
				ImmutableMap.of("project_id", String.class),
				ImmutableMap.of("project_id", "")
		);
	}

	Project project;

	public ProjectTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setBuildDom();

		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			String src = getResponse().getText();

			project = new Project(getUrl());

			if( src.contains("对不起") ){
				return ;
			}
			//页面正常
			else {
				crawlerJob(doc);
			}

		});

	}

	public void crawlerJob(Document doc){

		project.origin_id = getUrl().split("id=")[1];

		project.domain_id = Integer.valueOf(5);

		String price = doc.getElementsByClass("zb-money").text();

		//标题
		project.title = doc.getElementsByClass("zb-workbench-h1").text().replace(price,"");

		//价格
		if( price.contains("-") ){

			String priceub = CrawlerAction.getNumbers(price.split("-")[1]);
			String pricelb = CrawlerAction.getNumbers(price.split("-")[0]);

			if( priceub!=null && !"".equals(price) ){
				project.budget_ub = Double.valueOf(priceub);
			}

			if( pricelb!=null && !"".equals(pricelb) ){
				project.budget_lb = Double.valueOf(pricelb);
			}
		}
		//价格不为区间时
		else {
			price = CrawlerAction.getNumbers(price);
			if( !"".equals(price) ){
				project.budget_lb = Double.valueOf(price);
				project.budget_ub = Double.valueOf(price);
			}
		}

		//发布时间，周期，参与人数，地点
		Elements elements = doc.getElementsByClass("zb-workbench-normal-text");
		for(Element element : elements){
			String detail = element.text();

			//发布时间
			if(detail.contains("发布")){

				try {
					project.pubdate = DateFormatUtil.parseTime(detail);
				} catch (ParseException e) {
					logger.error("error for string ro date", e);
				}
			}
			//周期
			else if( detail.contains("周期") || detail.contains("期望") ){
				if( detail.contains("月") ){
					detail = CrawlerAction.getNumbers(detail);
					if( !"".equals(detail) ){
						project.time_limit = Integer.valueOf(detail) * 30 ;
					}
				}
				else {
					detail = CrawlerAction.getNumbers(detail);
					if( !"".equals(detail) ){
						project.time_limit = Integer.valueOf(detail);
					}
				}
			}
			//地点
			else if( detail.contains("地域") ){

				String[] areas = detail.split(":");
				if( areas.length>1 ){
					project.location = areas[1];
				}
			}
			//参与人数
			else if( detail.contains("参与") ){

				detail = CrawlerAction.getNumbers(detail);
				if( !"".equals(detail) ){
					project.bids_num = Integer.valueOf(detail);
				}
			}
		}

		//状态
		project.status = doc.getElementsByClass("zb-workbench-state").text();

		//小标签
		Elements tagSrc = doc.getElementsByClass("zb-workbench-btn");
		StringBuffer tags = new StringBuffer();
		for( Element e : tagSrc ){
			tags.append(e.text());
			tags.append(",");
		}
		project.tags = tags.substring(0, tags.length()-1);

		//描述
		project.content = doc.select("div.minh-mini").html();

		//服务商任务
		String services = doc.getElementsByClass("apply-list").toString();

		Pattern pattern = Pattern.compile("u=(?<uId>.+?)&amp");
		Matcher matcher = pattern.matcher(services);

		Set<String> uId = new HashSet<>();

		while( matcher.find() ) {
			uId.add(matcher.group("uId"));
		}

		List<com.sdyk.ai.crawler.task.Task> task = new ArrayList<>();

		for( String id : uId ) {

			try {
				HttpTaskPoster.getInstance().submit(ServiceProviderTask.class,
						ImmutableMap.of("user_id", id));
			} catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {

				logger.error("error fro HttpTaskPoster.submit ServiceProviderTask.class", e);
			}

		}

		//甲方任务
		String tenderer = doc.select("div.zb-workbench-user-box").toString();

		Pattern pattern1 = Pattern.compile("u=(?<tId>.+?)&amp");
		Matcher matcher1 = pattern1.matcher(tenderer);

		Set<String> tId = new HashSet<>();

		while( matcher1.find() ) {
			tId.add(matcher1.group("tId"));
		}

		for( String t : tId ){

			try {
				HttpTaskPoster.getInstance().submit(TendererTask.class,
						ImmutableMap.of("tenderer_id", t));
			} catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {

				logger.error("error fro HttpTaskPoster.submit TendererTask.class", e);
			}

			project.tenderer_id = one.rewind.txt.StringUtil.byteArrayToHex(
					one.rewind.txt.StringUtil.uuid(
							"https://zb.oschina.net/profile/index.html?u=" + t + "&t=p"));

		}

		//插入数据
		try{
			project.insert();
		} catch (Exception e) {
			logger.error("error for project.insert()", e);
		}

	}


}
