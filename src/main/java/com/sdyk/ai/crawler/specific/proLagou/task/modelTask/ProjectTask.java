package com.sdyk.ai.crawler.specific.proLagou.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.Project;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.util.FileUtil;
import org.jsoup.nodes.Document;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ProjectTask extends Task {

	static {
		// init_map_class
		init_map_class = ImmutableMap.of("project_id", String.class);
		// init_map_defaults
		init_map_defaults = ImmutableMap.of("q", "ip");
		// url_template
		url_template = "https://pro.lagou.com/project/{{project_id}}.html";
	}


	public Project project;

    public ProjectTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

    	super(url);

    	// 设置优先级
        this.setPriority(Priority.HIGH);

        this.addDoneCallback((t) -> {

            Document doc = getResponse().getDoc();
            String src = getResponse().getText();

            //下载页面
            FileUtil.writeBytesToFile(src.getBytes(), "project.html");

            if ( src.contains("失败") || src.contains("错误") ) {
	            this.setRetry();
	            return;
            }
            //抓取页面
            crawlJob(doc);
        });
    }


	public void crawlJob(Document doc){

		project = new Project(getUrl());

		project.domain_id = 3;

		//项目名
		String title = doc.select("#project_detail > div.project_info.fl > div.title > div").text();

		project.origin_id = getUrl().split("project/")[1].replace(".html","");

		//状态
		String currentStatus = doc.select("#project_detail > div.project_info.fl > div.title > div > span").text();
		if(currentStatus!=null&&!"".equals(currentStatus)){
			title = title.replace("currentStatus","");
		}
		project.title = title;

		//类型
		project.category = doc.select("#project_detail > div.project_info.fl > ul:nth-child(2) > li:nth-child(1) > span:nth-child(3)").text();

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
		project.tags = doc.select("#project_detail > div.project_info.fl > div.category_list").text();

		//已投标人数
		String num = doc.select("#project_detail > div.project_panel.fr > div.bottom_data > div:nth-child(2) > div.txt > strong").text();
		if(num!=null&&!"".equals(num)){
			project.bids_num = Integer.valueOf(num);
		}

		//工期
		String tineLimt = doc.select("#project_detail > div.project_panel.fr > div:nth-child(2) > span.short_val").text();

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
		project.content = "<pre>" +
				doc.select("#project_detail > div.project_info.fl > div.project_content > div.project_txt > pre")
						.html() + "</pre>";

		//浏览人数
		String browse = doc.select("#project_detail > div.project_panel.fr > div.bottom_data > div:nth-child(3) > div.txt > strong").text();
		if(browse!=null&&!"".equals(browse)){
			try {
				project.view_num = Integer.valueOf(browse);
			} catch (Exception e) {
				logger.error("error on String"+browse +"To Integer", e);
			}
		}
		try {
			project.insert();
		} catch (Exception e) {
			logger.error("error on insert project", e);
		}
	}

}
