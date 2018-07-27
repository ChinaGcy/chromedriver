package com.sdyk.ai.crawler.specific.oschina.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.witkey.Project;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.oschina.task.Task;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectTask extends Task {

	public static long MIN_INTERVAL = 24 * 60 * 60 * 1000L;

	static {
		registerBuilder(
				ProjectTask.class,
				"https://zb.oschina.net/{{project_id}}",
				ImmutableMap.of("project_id", String.class),
				ImmutableMap.of("project_id", "")
		);
	}

	public ProjectTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setBuildDom();

		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			String src = getResponse().getText();

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

		Project project = new Project(getUrl());

		project.origin_id = getUrl().split("id=")[1];

		project.domain_id = Integer.valueOf(5);

		String price = doc.getElementsByClass("zb-money").text();

		//标题
		project.title = doc.getElementsByClass("zb-workbench-h1").text().replace(price,"");

		project.category = doc.select("#container > div.main-content > div.show-for-medium > div > div > div > div.el-row > div > div > div > div.el-card.box-card.zb-box-card.is-always-shadow > div > div > div.zb-workbench-box > div:nth-child(2) > span:nth-child(3)").text();

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

		// 行业
		project.category = doc.select("span.zb-workbench-mark:nth-child(2)").text();

		// 付款方式
		project.trade_type = doc.select("span.zb-workbench-mark:nth-child(1)").text();

		//小标签
		Elements tagSrc = doc.getElementsByClass("zb-workbench-btn");
		StringBuffer tags = new StringBuffer();
		for( Element e : tagSrc ){
			tags.append(e.text());
			tags.append(",");
		}
		project.tags = tags.substring(0, tags.length()-1);

		//描述
		project.content = StringUtil.cleanContent(doc.select("div.minh-mini").html(), new HashSet<>());

		project.tenderer_name = doc.select("a.zb-header-box > img").attr("title");

		//服务商任务
		String services = doc.getElementsByClass("apply-list").toString();

		Pattern pattern = Pattern.compile("u=(?<uId>.+?)&amp");
		Matcher matcher = pattern.matcher(services);

		Set<String> uId = new HashSet<>();

		while( matcher.find() ) {
			uId.add(matcher.group("uId"));
		}

		for( String id : uId ) {

			try {

				//设置参数
				Map<String, Object> init_map = new HashMap<>();
				init_map.put("user_id", id);

				Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.oschina.task.modelTask.ServiceProviderTask");

				//生成holder
				ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

				//提交任务
				((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

			} catch ( Exception e) {

				logger.error("error for submit ServiceProviderTask.class", e);
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

				//设置参数
				Map<String, Object> init_map = new HashMap<>();
				init_map.put("tenderer_id", t);

				Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.oschina.task.modelTask.TendererTask");

				//生成holder
				ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

				//提交任务
				((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

			} catch ( Exception e) {

				logger.error("error for submit TendererTask.class", e);
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
