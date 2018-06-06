package com.sdyk.ai.crawler.specific.clouderwork.task.modelTask;

import com.sdyk.ai.crawler.model.TendererRating;
import com.sdyk.ai.crawler.specific.clouderwork.task.Task;
import com.sdyk.ai.crawler.specific.clouderwork.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.model.Tenderer;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class TendererTask extends Task {

    public TendererTask(String url) throws MalformedURLException, URISyntaxException {
        super(url);
        this.setPriority(Priority.MEDIUM);
        this.setBuildDom();

        this.addDoneCallback(()->{
            Document doc = getResponse().getDoc();
            String src = getResponse().getText();
            //执行抓取任务
            try {
                crawlawJob(doc);
            } catch (MalformedURLException e) {
                logger.info("error on crawlawJob");
            } catch (URISyntaxException e) {
                logger.info("error on crawlawJob");
            }

        });

    }

    /**
     * 抓取甲方数据
     * @param doc
     * @return
     * @throws ChromeDriverException.IllegalStatusException
     * @throws IOException
     * @throws URISyntaxException
     */
    public void crawlawJob (Document doc) throws MalformedURLException, URISyntaxException {

        List<com.sdyk.ai.crawler.task.Task> tasks = new ArrayList<>();

        Tenderer tenderer = new Tenderer(getUrl());
        Pattern pattern = Pattern.compile("[0-9]*");
        //原网站ID
        String[] urlarray = getUrl().split("com");
        tenderer.origin_id = urlarray[1];
        //招标人名字
        String name = doc.select("#profile > div > div > section > section > div.prjectBox > p.name-p").text();
        if( name!=null && !"".equals(name) ){
            tenderer.name = name;
        }
        //招标人描述
        tenderer.content = doc.select("#profile > div > div > section > section > div.prjectBox > p.overview-p").html();
        //线上消费金额
        String totalSpendingt =doc.select("#profile > div > div > section > section > div.prjectBox > section > span:nth-child(1)").text().replaceAll("￥","");
        if( totalSpendingt!= null &&!"".equals(totalSpendingt) ){
            if( totalSpendingt.contains("万") ){
                totalSpendingt = totalSpendingt.replace("万","").replace(",","");
                tenderer.total_spending = Double.valueOf(totalSpendingt)*10000;
            }
            //单位为元
            else {
                tenderer.total_spending = Double.valueOf(totalSpendingt);
            }
        }
        //雇佣人数
        String totalHires = doc.select("#profile > div > div > section > section > div.prjectBox > section > span:nth-child(3)").text();
        totalHires = CrawlerAction.getNumbers(totalHires);
        if( totalHires!=null && !"".equals(totalHires) && pattern.matcher(totalHires).matches() ){
            tenderer.total_employees = Integer.valueOf(totalHires);
        }
        //评论
        org.jsoup.select.Elements elements = doc.getElementsByClass("company");
        if( elements!=null && elements.size()>0 ){
            for(Element element : elements){

                TendererRating tendererRating = new TendererRating(getUrl());
                //雇主URL
                tendererRating.user_id = getUrl();
                //服务商名称
                //tendererRating.facilitator_name = element.getElementsByClass("comp-name").text();
                //评价内容
                tendererRating.tags = element.getElementsByClass("comp-desc").text();
                //合作愉快度
                String happy_num = element.getElementsByClass("score-num").text()
                        .replace(".","").replace("0","");
                String happy = CrawlerAction.getNumbers(happy_num);
                tendererRating.coop_rating = Integer.valueOf(happy);
                tendererRating.insert();

            }
        }
        //评价数
        String ratingNumAndGood = doc.select("#profile > div > div > div.evaluation > p.only-sys").text();
        String ratingNum = CrawlerAction.getNumbers(ratingNumAndGood);
        if(ratingNum!=null&&!"".equals(ratingNum)){
            tenderer.rating_num = Integer.valueOf(ratingNum);
        }
        //好评数
        if(ratingNumAndGood.contains("好评")){
            String googNum = CrawlerAction.getNumbers(ratingNumAndGood);
            if(googNum!=null&&!"".equals(googNum)){
                tenderer.praise_time = Integer.valueOf(googNum);
            }
        }
        //获取项目连接
        String[] userIds = getUrl().split("clients/");
        String userId = userIds[1];
        if(userId!=null&&!"".equals(userId)){
            String psUrl = "https://www.clouderwork.com/api/v2/jobs/client?pagesize=20&user_id=" + userId+"&pagenum=1";
            tasks.add(new ProjectScanTask(psUrl,1));
        }
        for(com.sdyk.ai.crawler.task.Task t : tasks){
            t.setBuildDom();
            ChromeDriverRequester.getInstance().submit(t);
        }
        tenderer.insert();
    }

}
