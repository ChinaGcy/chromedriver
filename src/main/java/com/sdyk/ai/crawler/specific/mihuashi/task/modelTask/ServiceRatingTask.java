package com.sdyk.ai.crawler.specific.mihuashi.task.modelTask;

import com.sdyk.ai.crawler.model.SupplierRating;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
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
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceRatingTask extends Task {

    public ServiceRatingTask(String url) throws MalformedURLException, URISyntaxException {
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

        List<Task> tasks = new ArrayList();
        Pattern pattern = Pattern.compile("/users/(?<username>.+?)\\?role=painter");
        Matcher matcher = pattern.matcher(getUrl());
        String web=null;
        while(matcher.find()) {
            try {
                web = URLDecoder.decode(matcher.group("username"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        Elements elements = doc.getElementsByClass("profile__comment-cell");
        Pattern pattern1 = Pattern.compile("/users/(?<username>.+?)\\?role=employer");
        int i = 0;
        for(Element element : elements){
            SupplierRating supplierRating = new SupplierRating(getUrl()+"&num="+i);
            i++;
            //服务商ID
            supplierRating.service_supplier_id = web;
            //甲方名字\URL\ID
            Elements name = element.getElementsByClass("name");
            supplierRating.tenderer_name = name.get(1).text();
            try {
                String url = URLDecoder.decode(name.get(1).attr("href"), "UTF-8");
                supplierRating.tenderer_url = url;
                Matcher matcher1 = pattern1.matcher(url);
                while(matcher1.find()) {
                    String id = URLDecoder.decode(matcher1.group("username"), "UTF-8");
                    supplierRating.tenderer_id = id;
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            //描述
            supplierRating.description = element.getElementsByClass("content").text();
            //发布时间
            String time = element.getElementsByClass("commented-time").text();
            try {
                supplierRating.rating_time = DateFormatUtil.parseTime(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            //项目花费
            String spand = element.getElementsByClass("budget-interval").text();
            if(spand.contains("~")){
                String[] spands = spand.split("~");
                supplierRating.spend_lb = Integer.valueOf(CrawlerAction.getNumbers(spands[0]));
                supplierRating.spend_up = Integer.valueOf(CrawlerAction.getNumbers(spands[1]));
            }

            //项目URL
            String proUrl = element.toString();
            Pattern pattern2 = Pattern.compile("/projects/\\d+");
            Matcher matcher2 = pattern2.matcher(proUrl);
            while (matcher2.find()) {
                try {
                    String projectUrl = "https://www.mihuashi.com"+matcher2.group();
                    supplierRating.project_url = projectUrl;
                    Task t = new ProjectTask(projectUrl);
                    tasks.add(t);
                } catch (Exception e) {
                    logger.error(e);
                }
            }

            supplierRating.insert();

        }

        /*for(Task t : tasks){
            t.setBuildDom();
            ChromeDriverRequester.getInstance().submit(t);
        }*/
    }

    @Override
    public one.rewind.io.requester.Task validate() throws ProxyException.Failed, AccountException.Failed, AccountException.Frozen {
        return null;
    }

}
