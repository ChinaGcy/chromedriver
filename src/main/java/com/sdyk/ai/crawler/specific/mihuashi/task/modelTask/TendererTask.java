package com.sdyk.ai.crawler.specific.mihuashi.task.modelTask;

import com.sdyk.ai.crawler.model.Tenderer;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TendererTask extends com.sdyk.ai.crawler.task.Task{

    public Tenderer tenderer;

    public TendererTask(String url) throws MalformedURLException, URISyntaxException {

        super(url);
        this.setPriority(Priority.MEDIUM);
        this.setBuildDom();
        this.addDoneCallback(()->{
            Document doc = getResponse().getDoc();
            //执行抓取任务
            crawlawJob(doc);
        });
    }

    @Override
    public one.rewind.io.requester.Task validate() throws ProxyException.Failed, AccountException.Failed, AccountException.Frozen {
        return null;
    }

    public void crawlawJob(Document doc){

        tenderer = new Tenderer(getUrl());
        List<Task> task = new ArrayList();
        Pattern pattern = Pattern.compile("/users/(?<username>.+?)\\?role=employer");
        Matcher matcher = pattern.matcher(getUrl());
        while(matcher.find()) {
            try {
                String web = URLDecoder.decode(matcher.group("username"), "UTF-8")+"?role=employer";
                tenderer.website_id = web;
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        //名称
        tenderer.name = doc.select("#users-show > div.container-fluid > div.profile__container > aside > section.profile__avatar-wrapper > h5 > span").text();
        //评价数
        String ratingNum = doc.select("#users-show > div.container-fluid > div.profile__container > main > header > ul > li.active > a > span").text();
        ratingNum = CrawlerAction.getNumbers(ratingNum);
        if(ratingNum!=null&&!"".equals(ratingNum)){
            tenderer.rating_num = Integer.valueOf(ratingNum);
        }
        //简介
        tenderer.description = doc.select("#users-show > div.container-fluid > div.profile__container > aside > section.profile__summary-wrapper").html();

        Elements elements =doc.getElementsByClass("project-cell__title-link");
        for(Element element : elements){
            String projecturl = "https://www.mihuashi.com"+element.attr("href");
            try {
                task.add(new ProjectTask(projecturl));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        for(Task t : task){
            t.setBuildDom();
            ChromeDriverRequester.getInstance().submit(t);
        }
        tenderer.insert();
    }


}
