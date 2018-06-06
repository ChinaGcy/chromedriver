package com.sdyk.ai.crawler.specific.mihuashi.task.modelTask;

import com.sdyk.ai.crawler.model.TendererRating;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TendererRatingTask extends Task {

    public TendererRatingTask(String url) throws MalformedURLException, URISyntaxException {
        super(url);
        this.setPriority(Priority.MEDIUM);
        this.setBuildDom();
        this.addDoneCallback(()->{
            Document doc = getResponse().getDoc();
            //执行抓取任务
            crawlawJob(doc);
        });
    }

    public void crawlawJob(Document doc){
        //雇主url
        Pattern pattern = Pattern.compile("/users/(?<username>.+?)\\?role=employer");
        Matcher matcher = pattern.matcher(getUrl());
        String web=null;
        while(matcher.find()) {
            try {
                web = URLDecoder.decode("https://www.mihuashi.com/users/"+matcher.group("username"), "UTF-8")+"?role=employer";
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        //抓取评价
        Elements elements = doc.getElementsByClass("profile__comment-cell");
        int i =1;
        for(Element element : elements){
            TendererRating tendererRating = new TendererRating(getUrl()+"&num="+i);
            i++;
            //雇主URl
            tendererRating.user_id = web;
            //服务商名称
          //  tendererRating.facilitator_name = element.getElementsByClass("name").text();
            //服务商URL
         //   tendererRating.facilitator_url = element.getElementsByClass("name").attr("href");
            //评价
            tendererRating.content = element.getElementsByClass("content").text();
            //评价时间
            String time = element.getElementsByClass("commented-time").text();
            try {
                tendererRating.pubdate = DateFormatUtil.parseTime(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            tendererRating.insert();
        }
    }

    @Override
    public one.rewind.io.requester.Task validate() throws ProxyException.Failed, AccountException.Failed, AccountException.Frozen {
        return null;
    }
}
