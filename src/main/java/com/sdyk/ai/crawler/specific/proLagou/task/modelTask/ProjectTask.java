package com.sdyk.ai.crawler.specific.proLagou.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.model.witkey.Project;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.ChromeTaskScheduler;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ScheduledChromeTask;
import one.rewind.util.FileUtil;
import org.jsoup.nodes.Document;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProjectTask extends Task {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	public static List<String> crons = Arrays.asList("* * */1 * *");

	static {
		registerBuilder(
				ProjectTask.class,
				"https://pro.lagou.com/project/{{project_id}}.html",
				ImmutableMap.of("project_id", String.class),
				ImmutableMap.of("project_id","")
		);
	}

	public Project project;

    public ProjectTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

    	super(url);

    	// 设置优先级
        this.setPriority(Priority.HIGH);

	    this.setNoFetchImages();

	    // 检测异常
	    this.setValidator((a,t) -> {

		    String src = getResponse().getText();
		    if( src.contains("登陆") && src.contains("没有账号") ){

			    throw new AccountException.Failed(a.accounts.get(t.getDomain()));
		    }
	    });

        this.addDoneCallback((t) -> {

            Document doc = getResponse().getDoc();
            String src = getResponse().getText();

            // 下载页面
            FileUtil.writeBytesToFile(src.getBytes(), "project.html");

            if ( src.contains("失败") || src.contains("错误") ) {
	            this.setRetry();
	            return;
            }

            //抓取页面
	        project = new Project(getUrl());

	        project.domain_id = 3;

	        //项目名
	        String title = doc.select("#project_detail > div.project_info.fl > div.title > div").text();

	        project.origin_id = getUrl().split("project/")[1].replace(".html","");

	        //状态
	        String currentStatus = doc.select("#project_detail > div.project_info.fl > div.title > div > span").text();
	        if(currentStatus!=null&&!"".equals(currentStatus)){
		        title = title.replace(currentStatus, "");
	        }
	        project.title = title;

	        //类型
	        project.status = currentStatus;

	        //招标人
	        project.tenderer_name = doc.select("#project_detail > div.project_info.fl > ul:nth-child(2) > li:nth-child(2) > span:nth-child(3)").text();

	        //发布时间
	        String pubdata = doc.select("#project_detail > div.project_info.fl > ul:nth-child(3) > li:nth-child(1) > span:nth-child(3)").text();
	        if(pubdata!=null&&!"".equals(pubdata)){
		        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
		        try {
			        project.pubdate = format1.parse(pubdata);
		        } catch (ParseException e) {
			        logger.error("error on String"+ pubdata +"to Data",e);
		        }
	        }

	        //小标签
	       // project.tags = doc.select("#project_detail > div.project_info.fl > div.category_list")
			//        .text().replace(" ", ",");

	        //已投标人数
	        String num = doc.select("#project_detail > div.project_panel.fr > div.bottom_data > div:nth-child(2) > div.txt > strong").text();
	        if(num!=null&&!"".equals(num)){
		        project.bids_num = Integer.valueOf(num);
	        }

	        //工期
	        String timeLimt = doc.select("#project_detail > div.project_panel.fr > div:nth-child(2) > span.short_val").text();
	        int unit = 1;
	        if( timeLimt.contains("-") ){
		        timeLimt = timeLimt.split("-")[1];
	        }
	        if( timeLimt.contains("月") ){
		        unit = 30;
	        }
	        else if(timeLimt.contains("周")){
		        unit = 7;
	        }
	        timeLimt = CrawlerAction.getNumbers(timeLimt);
	        if(timeLimt.length() > 1){
		        project.time_limit = Integer.valueOf(timeLimt) * unit;
	        }

	        //预算
	        String budget = doc.select("#project_detail > div.project_panel.fr > div:nth-child(1) > span.short_val").text().replace("元","").replace("以下","");
	        if(budget!=null&&!"".equals(budget)){
		        if(budget.contains("-")){
			        String[] budgets = budget.split("-");
			        project.budget_lb = Integer.valueOf(budgets[0]);
			        project.budget_ub = Integer.valueOf(budgets[1]);
		        }else{
			        project.budget_ub = Integer.valueOf(budget);
			        project.budget_lb = Integer.valueOf(budget);
		        }
	        }

	        //描述
	        project.content = "<p>" +
			        doc.select("#project_detail > div.project_info.fl > div.project_content > div.project_txt > pre")
					        .html() + "</p>";

	        //浏览人数
	        String browse = doc.select("#project_detail > div.project_panel.fr > div.bottom_data > div:nth-child(3) > div.txt > strong")
			        .text();
	        if(browse!=null&&!"".equals(browse)){
		        try {
			        project.view_num = Integer.valueOf(browse);
		        } catch (Exception e) {
			        logger.error("error on String"+browse +"To Integer", e);
		        }
	        }

	        // 推荐人数
	        String rcmd_num = CrawlerAction.getNumbers(
			        doc.select("#project_detail > div.project_panel.fr > div.bottom_data > div:nth-child(1) > div.txt > strong").text());
	        if( rcmd_num != null ){
		        project.rcmd_num = Integer.valueOf(rcmd_num);
	        }

	        if( project.tenderer_name.contains("公司") ){

		        // 生成公司信息补全任务
		        try {

			        //设置参数
			        Map<String, Object> init_map = new HashMap<>();
			        init_map.put("company_name", project.tenderer_name);

			        Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.company.CompanyInformationTask");

			        //生成holder
			       // ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

			        //提交任务
			        ((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

		        } catch (Exception e){
			        logger.error("error for create CompanyInformationTask", e);
		        }
	        }

	        project.category = doc.select(
			        "#project_detail > div.project_info.fl > ul:nth-child(2) > li:nth-child(1) > span:nth-child(3)"
	        ).text().replace("/", ",").replace(" ", "");

	        try {
		        project.insert();
	        } catch (Exception e) {
		        logger.error("error on insert project", e);
	        }

        });
    }


}
