package com.sdyk.ai.crawler.specific.clouderwork.task.modelTask;


import com.sdyk.ai.crawler.specific.clouderwork.task.Task;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.model.Project;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.util.FileUtil;
import org.jsoup.nodes.Document;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
            List<Task> tasks = new ArrayList();
            if (src.contains("失败")||src.contains("错误")) {
                try {
                    ChromeDriverRequester.getInstance().submit(new ProjectTask(getUrl()));
                    return;
                } catch (MalformedURLException | URISyntaxException e) {
                    logger.error(e);
                }
            }
            //抓取页面
            crawlJob(doc,tasks);
            for(Task t : tasks) {
                ChromeDriverRequester.getInstance().submit(t);
            }
        });

    }

    /**
     * 执行抓取任务
     * @param doc
     */
    public void crawlJob(Document doc,List<Task> tasks ){
        String authorUrl = null;

        project = new Project(getUrl());

        //项目名称
        project.title = doc.select("#project-detail > div > div.main-top > div.job-main > h3").text()
                .replaceAll(" ","")
                .replaceAll("招募中","").replaceAll("开发中","");
        //工作地点
        project.location = doc.select("#project-detail > div > div.main-top > div.job-main > div.project-info > p.loc").text();
        //项目描述
        project.content = doc.select("#project-detail > div > div.main-detail > section > div").html();
        //项目状态
        project.status = doc.select("#project-detail > div > div.main-top > div.job-main > h3 > span").text();
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
        String da = doc.select("#project-detail > div > div.main-top > div.job-main > div.client > div > span").text().replaceAll("发布于","");
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
        //已投标人数
        String bidder = doc.select("#project-detail > div > div.main-top > div.op-main > div.row > span").text();
        String bidderNum = CrawlerAction.getNumbers(bidder);
        if( bidderNum!=null && !"".equals(bidderNum) ){
            try {
                project.bids_num = Integer.valueOf(bidderNum);
            } catch (Exception e) {
                logger.error("error on String"+bidderNum +"To Integer", e);
            }

        }
        //招商人ID
        String tendererId =doc.select("#project-detail > div > div.main-top > div.job-main > div.client > div > a").attr("href");
        if( tendererId!=null && !"".equals(tendererId) ){
            project.tenderer_id = tendererId;
        }

        //采集招标人信息
        if( tendererId!=null && !"".equals(tendererId) ){
            //招标人详情页url
            authorUrl = "https://www.clouderwork.com"+tendererId;

            try {
                tasks.add(new TendererTask(authorUrl));
            } catch (MalformedURLException | URISyntaxException e) {
                logger.error("Error extract url: {}, ", authorUrl, e);
            }

        }
        try {
            project.insert();
        } catch (Exception e) {
            logger.error("error on insert project", e);
        }

    }
}
