package com.sdyk.ai.crawler.specific.clouderwork.task.modelTask;


import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.specific.clouderwork.task.Task;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.model.witkey.Project;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import one.rewind.util.FileUtil;
import org.jsoup.nodes.Document;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ProjectTask extends Task {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				ProjectTask.class,
				"https://www.clouderwork.com/jobs/{{project_id}}",
				ImmutableMap.of("project_id", String.class),
				ImmutableMap.of("project_id", "")
		);
	}

    public Project project;

    public ProjectTask(String url) throws MalformedURLException, URISyntaxException {

    	super(url);

        // 设置优先级
        this.setPriority(Priority.HIGH);

        this.addDoneCallback((t) -> {

        	//获取页面信息
            Document doc = getResponse().getDoc();
            String src = getResponse().getText();

            //下载页面
            FileUtil.writeBytesToFile(src.getBytes(), "project.html");

            //判断页面是否正确
            if (src.contains("失败")||src.contains("错误")) {
	            this.setRetry();
	            return;
            }

            //解析页面
            crawlJob(doc);

        });

    }

	/**
	 * 执行抓取任务
	 * @param doc
	 */
	public void crawlJob( Document doc ){

		String authorUrl = null;

		project = new Project(getUrl());

		project.domain_id = Integer.valueOf(2);

		project.origin_id = getUrl().split("jobs/")[1];

		//工作地点
		project.location = doc.select("#project-detail > div > div.main-top > div.job-main > div.project-info > p.loc").text();

		//项目描述
		project.content = doc.select("#project-detail > div > div.main-detail > section > div").html();

		//项目状态
		project.status = doc.select("#project-detail > div > div.main-top > div.job-main > h3 > span").text();

		//项目名称
		project.title = doc.select("#project-detail > div > div.main-top > div.job-main > h3").text()
				.replaceAll(" ","")
				.replaceAll(project.status,"");

		//项目类别
		project.category = doc.getElementsByClass("scope").text();

		//项目预算
		String budget = doc.select("#project-detail > div > div.main-top > div.op-main > div.detail-row > div:nth-child(1) > p.budget").text().replaceAll("￥","");
		Double budget_lb = Double.valueOf(0);
		Double budget_up = Double.valueOf(0);

		//当数据为  人/月
		if(budget.contains("/")){

			//以万元为单位时
			if(budget.contains("万")){
				budget = budget.substring(0,budget.length()-5);
				if(budget.contains(",")){
					budget = budget.replace(",","");
				}

				//捕获String to Double 异常
				try {
					budget_lb = Double.valueOf(budget)*10000;
				} catch (Exception e) {
					logger.error("error on String"+budget+"To Double", e);
				}

			}
			//不以万元为单位
			else {
				budget = budget.substring(0,budget.length()-4);
				if(budget.contains(",")){
					budget = budget.replace(",","");
				}
				try {
					budget_lb = Double.valueOf(budget);
				} catch (Exception e) {
					logger.error("error on String"+budget+"To Double", e);
				}
			}
			budget_up = budget_lb;
		}
		//当数据为预算区间时
		else if (budget.contains("～")){
			String[] budgetArray = budget.split("～");

			//最低价以万元为单位
			if(budgetArray[0].contains("万")){
				if(budgetArray[0].contains(",")){
					budgetArray[0] = budgetArray[0].replace(",","");
				}
				budgetArray[0] = budgetArray[0].substring(0,budgetArray[0].length()-1);
				try {
					budget_lb = Double.valueOf(budgetArray[0])*10000;
				} catch (Exception e) {
					logger.error("error on String"+budgetArray[0] +"To Double", e);
				}
			}
			//最低价不以万元为单位
			else {
				if(budgetArray[0].contains(",")){
					budgetArray[0] = budgetArray[0].replace(",","");
				}
				try {
					budget_lb = Double.valueOf(budgetArray[0]);
				} catch (Exception e) {
					logger.error("error on String"+budgetArray[0] +"To Double", e);
				}
			}

			//最高价以万元为单位
			if(budgetArray[1].contains("万")){
				if(budgetArray[1].contains(",")){
					budgetArray[1] = budgetArray[1].replace(",","");
				}
				budgetArray[1] = budgetArray[1].substring(0,budgetArray[1].length()-1);
				try {
					budget_up = Double.valueOf(budgetArray[1])*10000;
				} catch (Exception e) {
					logger.error("error on String"+budgetArray[1] +"To Double", e);
				}
			}
			//最高价不以万元为单位
			else {
				if(budgetArray[1].contains(",")){
					budgetArray[1] = budgetArray[1].replace(",","");
				}
				try {
					budget_up = Double.valueOf(budgetArray[1]);
				} catch (Exception e) {
					logger.error("error on String"+budgetArray[1] +"To Double", e);
				}
			}
		}
		//只有整体预算
		else {
			//以万元为单位
			if( budget.contains("万") ) {
				budget_lb = Double.valueOf(CrawlerAction.getNumbers(budget)) * 10000;
			}
			//不以万元为单位
			else {
				budget_lb = Double.valueOf(budget.replace(",",""));
			}
			budget_up = budget_lb;
		}
		project.budget_lb = budget_lb;
		project.budget_ub = budget_up;

		//项目工期
		String timeLimit = doc.select("#project-detail > div > div.main-top > div.op-main > div.detail-row > div.budgets.workload > p.budget > span").text();
		if( timeLimit!=null && !"".equals(timeLimit) ){
			try {
				project.time_limit = Integer.valueOf(timeLimit);
			} catch (Exception e) {
				logger.error("error on String"+timeLimit +"To Integer", e);
			}
		}

		//招标人名称
		project.tenderer_name = doc.select("#project-detail > div > div.main-top > div.job-main > div.client > div > a").text();

		String da = doc.getElementsByClass("time").text().replaceAll("发布于","");
		DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
		Date pubdate= null;
		try {
			pubdate = format1.parse(da);
		} catch (ParseException e) {
			logger.info("error on String"+ da +"to Data",e);
		}

		//项目发布时间
		project.pubdate = pubdate;

		String numberP = doc.select("#project-detail > div > div.main-top > div.job-main > div.project-info > p:nth-child(3) > span").text();

		//可投标人数
		if( numberP!=null && !"".equals(numberP) ){
			try {
				project.bidder_total_num = Integer.valueOf(numberP);
			} catch (Exception e) {
				logger.error("error on String"+numberP +"To Integer", e);
			}
		}

		// 查看人数
		String bidder = doc.select("#project-detail > div > div.main-top > div.op-main > div.row > span").text();
		String bidderNum = CrawlerAction.getNumbers(bidder);
		if( bidderNum!=null ){
			try {
				project.view_num = Integer.valueOf(bidderNum);
			} catch (Exception e) {
				logger.error("error on String"+bidderNum +"To Integer", e);
			}

		}

		//招商人ID
		String tendererId =doc.select("#project-detail > div > div.main-top > div.job-main > div.client > div > a").attr("href");
		if( tendererId!=null && !"".equals(tendererId) ){
			project.tenderer_id = one.rewind.txt.StringUtil.byteArrayToHex(
					one.rewind.txt.StringUtil.uuid("https://www.clouderwork.com" + tendererId));
		}

		//采集招标人信息
		if( tendererId!=null && !"".equals(tendererId) ){

			try {

				//设置参数
				Map<String, Object> init_map = new HashMap<>();
				init_map.put("tenderer_id", tendererId);

				Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.TendererTask");

				//生成holder
				ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

				//提交任务
				((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

			} catch ( Exception e) {

				logger.error("error for submit TendererTask.class", e);
			}

		}

		try {
			project.insert();
		} catch (Exception e) {
			logger.error("error on insert project", e);
		}

	}

	public static void registerBuilder(Class<? extends ChromeTask> clazz, String url_template, Map<String, Class> init_map_class, Map<String, Object> init_map_defaults){
		ChromeTask.registerBuilder( clazz, url_template, init_map_class, init_map_defaults );
	}
}