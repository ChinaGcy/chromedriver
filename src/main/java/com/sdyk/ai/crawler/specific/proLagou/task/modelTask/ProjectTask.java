package com.sdyk.ai.crawler.specific.proLagou.task.modelTask;

import com.sdyk.ai.crawler.model.Project;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.util.FileUtil;
import org.jsoup.nodes.Document;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ProjectTask extends Task {

    public Project project;

    public ProjectTask(String url) throws MalformedURLException, URISyntaxException {
        super(url);
        this.setBuildDom();
        // 设置优先级
        this.setPriority(Priority.HIGH);

        this.addDoneCallback(() -> {
            Document doc = getResponse().getDoc();
            String src = getResponse().getText();
            //下载页面
            FileUtil.writeBytesToFile(src.getBytes(), "project.html");
            if (src.contains("失败")||src.contains("错误")) {
                try {
                    ChromeDriverRequester.getInstance().submit(new ProjectTask(getUrl()));
                    return;
                } catch (MalformedURLException | URISyntaxException e) {
                    logger.error(e);
                }
            }
            //抓取页面
            crawlJob(doc);
        });
    }


    public void crawlJob(Document doc){

        try {
            project = new Project(getUrl());
        } catch (MalformedURLException e) {
            logger.info("error on creat projectMode",e);
        } catch (URISyntaxException e) {
            logger.info("error on creat projectMode",e);
        }
        project.domain = "pro.lagou.com";
        //项目名
        String title = doc.select("#project_detail > div.project_info.fl > div.title > div").text();
        //状态
        String currentStatus = doc.select("#project_detail > div.project_info.fl > div.title > div > span").text();
        if(currentStatus!=null&&!"".equals(currentStatus)){
            title = title.replace("currentStatus","");
        }
        project.title = title;
        project.current_status = currentStatus;
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
        //以投标人数
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
                project.budget_up = Integer.valueOf(budgets[1]);
            }else{
                project.budget_up = Integer.valueOf(budget);
                project.budget_lb = Integer.valueOf(budget);
            }
        }
        //描述
        project.description = doc.select("#project_detail > div.project_info.fl > div.project_content > div.project_txt > pre").html();
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

    @Override
    public one.rewind.io.requester.Task validate() throws ProxyException.Failed, AccountException.Failed, AccountException.Frozen {
        return null;
    }


}
