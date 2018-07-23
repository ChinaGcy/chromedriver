package com.sdyk.ai.crawler.specific.jfh.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.witkey.ServiceProvider;
import com.sdyk.ai.crawler.model.witkey.Work;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.jfh.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class ServiceProviderTask extends Task {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				ServiceProviderTask.class,
				"http://shop.jfh.com/{{user_id}}/bu",
				ImmutableMap.of("user_id", String.class),
				ImmutableMap.of("user_id", "")
		);
	}

	ServiceProvider serviceProvider;

	public ServiceProviderTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			String src = getResponse().getText();

			//页面错误
			if( src.contains("关闭中") || src.contains("稍后再试") || src.contains("404") ){
				return;
			}
			//页面正确
			else {
				crawler(doc);
			}

		});
	}

	public void crawler(Document doc){

		serviceProvider = new ServiceProvider(getUrl());

		serviceProvider.origin_id = getUrl().split("com/")[1]
				.replace("/bu","").replace("/","");

		//名字
		serviceProvider.name = doc.select("#archivesInfo > div.archivesInfo_in.clearfix > div.archivesInfo_in_right > p > span")
				.text();

		//类型
		if( serviceProvider.name.contains("公司") ){
			serviceProvider.company_name = serviceProvider.name;
			serviceProvider.type = "团队-公司";
		}

		//描述
		serviceProvider.content = doc.select("div.companyProfile").toString();

		Elements elements = doc.select("span.reset-style");
		for(Element element : elements){
			String detail = element.text();

			//公司规模
			if( detail.contains("员工") ){
				detail = CrawlerAction.getNumbers(detail);
				if( !"".equals(detail) ){
					serviceProvider.team_size = Integer.valueOf(detail);
				}
			}
			//公司地址
			else if ( detail.contains("省") || detail.contains("市")){
				serviceProvider.company_address = detail;
			}
		}

		//小标签
		Elements tagsE = doc.select("#beGoodAtDiv > div > div > span");
		StringBuffer tags = new StringBuffer();
		for( Element tagE : tagsE ){
			tags.append(tagE.text());
			tags.append(",");
		}
		serviceProvider.tags = tags.substring(0, tags.length()-1);

		Elements jiaMess = doc.select("div.shopPingjiaMess > ul > li");
		for(Element element : jiaMess){
			String detail = element.text();

			//服务质量
			if( detail.contains("质量") ){
				detail = CrawlerAction.getNumbers(detail);
				if( !"".equals(detail) ){
					serviceProvider.service_quality = Double.valueOf(detail);
				}
			}
			//服务速度
			else if( detail.contains("速度") ){
				detail = CrawlerAction.getNumbers(detail);
				if( !"".equals(detail) ){
					serviceProvider.service_speed = Double.valueOf(detail);
				}
			}
			//服务态度
			else if( detail.contains("沟通") ){
				detail = CrawlerAction.getNumbers(detail);
				if( !"".equals(detail) ){
					serviceProvider.service_attitude = Double.valueOf(detail);
				}
			}
		}

		//成交量
		String projectNum = doc.select("#header_tradeNum").text();
		projectNum = CrawlerAction.getNumbers(projectNum);
		if( !"".equals(projectNum) ){
			serviceProvider.project_num = Integer.valueOf(projectNum);
		}

		//交易额
		String incom = doc.select("#header_totalAmount").text();
		incom = CrawlerAction.getNumbers(incom);
		if( !"".equals(incom) ){
			serviceProvider.income = Double.valueOf(incom);
		}

		//收藏数
		String favNum = doc.select("#shopFlowData_collectionAmount").text();
		favNum = CrawlerAction.getNumbers(favNum);
		if( !"".equals(favNum) ){
			serviceProvider.fan_num = Integer.valueOf(favNum);
		}

		//浏览数
		String viewNum = doc.select("#shopFlowData_pageViewInWeek").text();
		viewNum = CrawlerAction.getNumbers(viewNum);
		if( !"".equals(viewNum) ){
			serviceProvider.view_num = Integer.valueOf(viewNum);
		}

		//案例
		Elements works = doc.select("div.successfulCaseDiv_right");
		int i = 0;
		for( Element element : works ){
			i++;

			Work work = new Work(getUrl() +"?work=" + i);

			work.user_id = getId();

			work.title = element.select("div.successfulCaseDiv_right_top > span:nth-child(1)").text();

			work.tenderer_name = element.select("div.successfulCaseDiv_right_top > span:nth-child(2) > i").text();

			String price = doc.select("div.successfulCaseDiv_right_top > span:nth-child(2) > i").text();
			price = CrawlerAction.getNumbers(price);
			if( !"".equals(price) ){
				work.price = Double.valueOf(price);
			}

			work.content = doc.select("div.successfulCaseDiv_right_bottom").toString();

			try{
				work.insert();
			} catch (Exception e) {
				logger.error("erroe for work.insert", e);
			}

		}

		String servicer_id = getUrl().replace("/bu","")
				.replace("http://shop.jfh.com/","");

		try {

			//设置参数
			Map<String, Object> init_map = new HashMap<>();
			ImmutableMap.of("servicer_id", servicer_id);

			Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.jfh.task.modelTask.ServiceProviderRatingTask");

			//生成holder
			ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

			//提交任务
			ChromeDriverDistributor.getInstance().submit(holder);

		} catch ( Exception e) {

			logger.error("error for submit ServiceProviderRatingTask.class", e);
		}

		try{
			serviceProvider.insert();
		} catch (Exception e) {
			logger.error("error for serviceProvider.insert", e);
		}

	}
}
