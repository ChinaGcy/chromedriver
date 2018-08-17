package com.sdyk.ai.crawler.specific.jfh.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.witkey.ServiceProvider;
import com.sdyk.ai.crawler.model.witkey.Work;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.jfh.task.Task;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import com.sdyk.ai.crawler.util.LocationParser;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.ChromeTaskScheduler;
import one.rewind.io.requester.exception.AccountException;
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

public class ServiceProviderTask extends Task {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	public static List<String> crons = Arrays.asList("* * */1 * *", "* * */2 * *", "* * */4 * *", "* * */8 * *");

	static {
		registerBuilder(
				ServiceProviderTask.class,
				"http://shop.jfh.com/{{user_id}}/bu",
				ImmutableMap.of("user_id", String.class),
				ImmutableMap.of("user_id", ""),
				true,
				Priority.HIGH
		);
	}

	public ServiceProviderTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setValidator((a,t) -> {

			String src = getResponse().getText();
			if( src.contains("Log in JointForce") && src.contains("Don't have an account?") ){

				throw new AccountException.Failed(a.accounts.get("jfh.com"));

			}

		});

		//this.setNoFetchImages();

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			String src = getResponse().getText();

			//页面错误
			if( src.contains("关闭中") || src.contains("稍后再试") || src.contains("404") ){
				return;
			}
			//页面正确
			else {
				crawler(doc, (ChromeTask)t);
			}

		});
	}

	public void crawler(Document doc, ChromeTask t) throws Exception {

		ServiceProvider serviceProvider = new ServiceProvider(getUrl());

		serviceProvider.origin_id = getUrl().split("com/")[1]
				.replace("/bu","").replace("/","");

		//名字
		serviceProvider.name = doc.select("#archivesInfo > div.archivesInfo_in.clearfix > div.archivesInfo_in_right > p > span").text();

		//类型
		if( serviceProvider.name.contains("公司") ){
			serviceProvider.company_name = serviceProvider.name;
		}
		serviceProvider.type = "团队-公司";

		// 头像
		String headImg = doc.select("#entLogo").attr("src");
		Map<String, String> map = new HashMap<>();
		map.put(headImg, "head_portrait");
		List<String> headList = BinaryDownloader.download(getUrl(), map);
		if( headList != null ){
			serviceProvider.head_portrait = headList.get(0);
		}

		//描述
		serviceProvider.content = StringUtil.cleanContent(doc.select("div.companyProfile").toString().replace("展开", ""), new HashSet<>());

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
			// 地址
			else if ( detail.contains("省") || detail.contains("市")){
				serviceProvider.location = LocationParser.getInstance().matchLocation(detail).get(0).toString();
			}
		}

		serviceProvider.domain_id = 6;

		//小标签
		Elements tagsE = doc.select("#beGoodAtDiv > div > div > span");
		StringBuffer tags = new StringBuffer();
		for( Element tagE : tagsE ){
			tags.append(tagE.text());
			tags.append(",");
		}
		if( tags.length() > 0 ){
			serviceProvider.tags = Arrays.asList(tags.substring(0, tags.length()-1));
		}

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

		// 认证情况
		Elements platform_certification = doc.select("i.certificationQIye");
		if( platform_certification.size() > 0 ){
			serviceProvider.platform_certification = new ArrayList<>();
			serviceProvider.platform_certification.add("企业认证");
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

		String servicer_id = getUrl().replace("/bu","")
				.replace("http://shop.jfh.com/","");

		// workTask
		Elements elements1 = doc.select("a.seeInfoBtn");
		for( Element element : elements1 ){

			String uuidSecret = element.attr("onclick")
					.replace("showCaseInfo('", "")
					.replace("');", "");

			try {

				//设置参数
				Map<String, Object> init_map = new HashMap<>();
				init_map.put("uuidSecret", uuidSecret);
				init_map.put("uId", serviceProvider.id);

				Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName(
						"com.sdyk.ai.crawler.specific.jfh.task.modelTask.WorkTask");

				//生成holder
				ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

				//提交任务
				((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

			} catch ( Exception e) {

				e.printStackTrace();
			}
		}

		// RatingTask
		try {

			//设置参数
			Map<String, Object> init_map = new HashMap<>();
			init_map.put("user_id", servicer_id);

			Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.jfh.task.modelTask.ServiceProviderRatingTask");

			//生成holder
			ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

			//提交任务
			((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

		} catch ( Exception e) {

			logger.error("error for submit ServiceProviderRatingTask.class", e);
		}

		try{

			boolean status = false;

			if( serviceProvider.name != null && serviceProvider.name.length() > 1 ){

				// 生成公司信息补全任务
				try {

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					init_map.put("company_name", serviceProvider.name);

					Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.company.CompanyInformationTask");

					//生成holder
					ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

					//提交任务
					((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

				} catch (Exception e){
					logger.error("error for create CompanyInformationTask", e);
				}

				serviceProvider.category.replace(" ", "");

				serviceProvider.insert();
			}

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


		} catch (Exception e) {
			logger.error("error for serviceProvider.insert", e);
		}

	}
}
