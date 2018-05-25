package com.sdyk.ai.crawler.specific.mihuashi.task.modelTask;

import com.sdyk.ai.crawler.model.Project;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.mihuashi.action.TendererProjectActive;
import com.sdyk.ai.crawler.specific.mihuashi.action.TendererRatingActive;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.txt.DateFormatUtil;
import one.rewind.util.FileUtil;
import org.jsoup.nodes.Document;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 处理需求页面 解析 project
 * 示例URL:
 */
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
            //抓取页面
            crawlJob(doc);
        });
    }

    public void crawlJob(Document doc){

        try {
            project = new Project(getUrl());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        List<Task> tasks = new ArrayList();
        project.domain = "mihuashi.com";
        String authorUrl = null;

        //项目名
        String title = doc.select("#project-name").text();
        String renzheng = doc.select("#project-name > span").text();
        project.title = title.replace(renzheng,"");
        //发布时间
        String time = doc.select("#projects-show > div.container-fluid > div.project__main-section > div.project__info-section > section > section > div.pull-left > p")
                .text().replace("企划发布于","").replace("\"","");
        String pub = doc.select("#projects-show > div.container-fluid > div.project__main-section > div.project__info-section > section > section > div.pull-left > p > span").text();
        String pubTime = time.replace(pub,"");
        DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
        try {
            project.pubdate = format1.parse(pubTime);
        } catch (ParseException e) {
            logger.error("error on String to data");
        }
        //类型
        project.category = pub;
        //预算
        String budget = doc.select("#aside-rail > div > aside > p:nth-child(4)").text().replace("￥","");
        if(budget!=null&&!"".equals(budget)){
            if(budget.contains("~")){
                String[] budgets = budget.split("~");
                String budget_lb = CrawlerAction.getNumbers(budgets[0]);
                String budget_uo = CrawlerAction.getNumbers(budgets[1]);
                project.budget_lb = Integer.valueOf(budget_lb);
                project.budget_up = Integer.valueOf(budget_uo);
            }else{
                project.budget_up=project.budget_lb=Integer.valueOf(budget);
            }
        }
        //剩余时间
        String remainingTime = doc.select("#aside-rail > div > aside > span").text().replace("后关闭应征","");
        try {
            project.remaining_time = DateFormatUtil.parseTime(remainingTime);
        } catch (ParseException e) {
            logger.error("error on String to date",e);
        }
        //工期
        String endTime = doc.select("#aside-rail > div > aside > p:nth-child(2)").text();
        try {
            long eTime = DateFormatUtil.parseTime(endTime).getTime();
            long timeLimt = eTime - project.pubdate.getTime();
            project.time_limit = Integer.valueOf((int)(timeLimt / (1000 * 60 * 60 *24)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        //描述
        project.description = doc.select("#project-detail").html();
        //赏金分配
        project.reward_distribution = doc.select("#projects-show > div.container-fluid > div.project__main-section > div.project__info-section > section > div.deposit-phases__wrapper").text();
        //采集时刻以投标数
        String bidderNewNum = doc.select("#projects-show > div.container-fluid > div.project__main-section > div.project__application-section > section > h5 > div > span.applications-count")
                .text().replace("本企划共应征画师","").replace("名","");
        String num = CrawlerAction.getNumbers(bidderNewNum);
        if(num!=null&&!"".equals(num)){
            project.bids_num= Integer.valueOf(num);
        }
        //投标人姓名
        project.tenderer_name = doc.select("#projects-show > div.container-fluid > div.project__main-section > div.project__sidebar-container > aside > section > h5 > span").text();
        //投标人ID
        String tendererId = doc.select("#profile__avatar > a").attr("href").toString();
        Pattern pattern = Pattern.compile("/users/(?<username>.+?)\\?role=employer");
        Matcher matcher = pattern.matcher(tendererId);
        while(matcher.find()) {
            try {
                project.tenderer_id = URLDecoder.decode(matcher.group("username"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        //采集招标人信息
        if(project.tenderer_id!=null&&!"".equals(project.tenderer_id)){
            authorUrl = "https://www.mihuashi.com/users/"+project.tenderer_id+"?role=employer";     //招标人详情页url
            try {
                //添加甲方任务
                Task taskT = new TendererTask(authorUrl);
                taskT.addAction(new TendererProjectActive(authorUrl));
                tasks.add(taskT);
                //添加甲方评论任务
                Task taskTR = new TendererRatingTask(authorUrl+"&rating=true");
                taskTR.addAction(new TendererRatingActive(authorUrl+"&rating=true"));
                tasks.add(taskTR);
            } catch (MalformedURLException | URISyntaxException e) {
                logger.error("Error extract url: {}, ", authorUrl, e);
            }
        }
        for(Task t : tasks){
            t.setBuildDom();
            ChromeDriverRequester.getInstance().submit(t);
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
