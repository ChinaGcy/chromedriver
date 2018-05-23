package com.sdyk.ai.crawler.specific.clouderwork.task.modelTask;

import com.sdyk.ai.crawler.model.Resume;
import com.sdyk.ai.crawler.model.ServiceSupplier;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class ServiceSupplierTask extends Task {

    ServiceSupplier serviceSupplier;

    public ServiceSupplierTask(String url) throws MalformedURLException, URISyntaxException {
        super(url);
        this.setPriority(Priority.MEDIUM);
        this.setBuildDom();
        this.addDoneCallback(() -> {

            String src = getResponse().getText();
            Document doc = getResponse().getDoc();
            serviceSupplier = new ServiceSupplier(getUrl());

            try {
                crawlerJob(doc);
            } catch (ChromeDriverException.IllegalStatusException e) {
                logger.info("error on crawlerJob",e);
            }

        });

    }

    /**
     * 抓取服务商信息
     * @param doc
     * @throws ChromeDriverException.IllegalStatusException
     */
    public void crawlerJob(Document doc) throws ChromeDriverException.IllegalStatusException {

        List<Task> tasks = new ArrayList<Task>();
        String url = getUrl();
        Pattern pattern = Pattern.compile("[0-9]*");
        String[] urls = url.split("com");
        serviceSupplier.website_id =urls[1];

        //名字
        String name = doc.getElementsByClass("name-box").text();
        if(name!=null&&!"".equals(name)){
            serviceSupplier.name = name;
        }
        //类型
        String type = doc.select("#profile > div > div > section.left > div.team > p").text();
        if(type!=null&&!"".equals(type)){
            String team = doc.getElementsByClass("team-s-title").text();
            serviceSupplier.type = team;
        }else{
            serviceSupplier.type = "个人";
        }

        String all =doc.select("#profile > div > div > section.basic > section > div:nth-child(8)").text().replace("擅长领域：","");
        if(all!=null&&!"".equals(all)&&all.contains("擅长技能")){
            String[] all1 = all.split("擅长技能：");
            String expertise = all1[0];
            //擅长领域
            if(expertise!=null&&!"".equals(expertise)){
                serviceSupplier.expertise = expertise;
            }
            String[] all2 = all1[1].split("所在城市：");
            //擅长技能
            String skills = all2[0];
            if(skills!=null&&!"".equals(skills)){
                serviceSupplier.skills = skills;
            }
            String[] all3 = all2[1].split("工作经验：");
            //地点
            String location = all3[0];
            if(location!=null&&!"".equals(location)){
                serviceSupplier.location = location;
            }
        }else if(all!=null&&!"".equals(all)&&all.contains("收藏")){
            String allOther = doc.select("#profile > div > div > section.basic > section > div:nth-child(7)").text();
            String[] all1 = allOther.split("擅长技能：");
            String expertise = all1[0];
            //擅长领域
            if(expertise!=null&&!"".equals(expertise)){
                serviceSupplier.expertise = expertise;
            }
            String[] all2 = all1[1].split("所在城市：");
            //擅长技能
            String skills;
            if(all2[0].contains("预估时薪")){
                String[] all2try = all2[0].split("预估时薪");
                skills = all2try[0];
            }else{
                skills = all2[0];
            }
            if(skills!=null&&!"".equals(skills)){
                serviceSupplier.skills = skills;
            }
            String[] all3 = all2[1].split("工作经验：");
            //地点
            String location = all3[0];
            if(location!=null&&!"".equals(location)){
                serviceSupplier.location = location;
            }
        }

        //工作经验
        String workExperience = doc.getElementsByClass("main-item").text();
        if(workExperience!=null&&!"".equals(workExperience)){
            serviceSupplier.work_experience =workExperience;
        }


        //成员数量
        String memberNum = doc.select("#profile > div > div > section.left > div.team > div > a > div").text();
        if(memberNum!=null&&!"".equals(memberNum)){
            String[] memberNums = memberNum.split("名");
            String Num = memberNums[0].substring(1,memberNums[0].length());
            if(Num!=null&&!"".equals(Num)&&pattern.matcher(Num).matches()){
                serviceSupplier.member_num = Integer.valueOf(Num);
            }
        }else {
            serviceSupplier.member_num = Integer.valueOf(1);
        }
        //平台项目数
        String projectNum = doc.select("#profile > div > div > section.left > div.evaluation > p.eva-desc > span:nth-child(1)").text();
        if(projectNum!=null&&!"".equals(projectNum)&&pattern.matcher(projectNum).matches()){
            serviceSupplier.project_num = Integer.valueOf(projectNum);
        }
        //平台成功率
        String successRatio = doc.select("#profile > div > div > section.left > div.evaluation > p.eva-desc > span:nth-child(2)").text().replace("%","");
        if(successRatio!=null&!"".equals(successRatio)){
            serviceSupplier.success_ratio = Float.valueOf(successRatio);
        }
        //顾客评分
        String rating = doc.select("#profile > div > div > section.left > div.evaluation > div > span").text().replace("分","");
        if(rating!=null&&!"".equals(rating)){
            serviceSupplier.rating = Float.valueOf(rating);
        }
        //评价数
        String ratingNum = doc.select("#profile > div > div > section.left > div.evaluation > p.only-sys").text();
        String[] ratingNums = ratingNum.split("个");
        if(ratingNums[0]!=null&&!"".equals(ratingNums[0])){
            serviceSupplier.rating_num = Integer.valueOf(ratingNums[0]);
            //好评数
            if(ratingNums.length>1&&ratingNums[1].contains("好评")){
                serviceSupplier.good_rating_num = serviceSupplier.rating_num;
            }
        }

        //教育经历
        String eduAll = doc.getElementsByClass("main-content edu").text();
        if(eduAll!=null&&!"".equals(eduAll)){
            Resume resume = new Resume(url);
            resume.user_id = serviceSupplier.website_id;
            String eduTime = doc.getElementsByClass("edu-time").text().replace("年","");
            if(eduTime!=null&&!"".equals(eduTime)){
                String[] times = eduTime.split("-");
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy");
                try {
                    Date startdate=simpleDateFormat.parse(times[0]);
                    Date enddate=simpleDateFormat.parse(times[1]);
                    resume.sd =startdate;
                    resume.ed = enddate;
                } catch(ParseException px) {
                    px.printStackTrace();
                }
            }
            String eduSchool = doc.getElementsByClass("edu-school").text();
            if(eduSchool!=null&&!"".equals(eduSchool)){
                resume.unit = eduSchool;
            }
            String eduCity = doc.getElementsByClass("edu-city").text();
            if(eduCity!=null&&!"".equals(eduCity)){
                resume.dep = eduCity;

                String[] eduposition = eduAll.split(eduCity);
                if(eduposition.length>1){
                    resume.degree_position = eduposition[1];
                }
            }
            try {
                resume.insert();
            } catch (Exception e) {
                logger.error("error on insert resume", e);
            }
        }

        //描述
        String description1 = doc.getElementsByClass("main-content").html();
        String description2 = doc.getElementsByClass("file").html();
        Elements fileelements = doc.getElementsByClass("download");
        if(fileelements!=null&&fileelements.size()>0){
            Set<String> fileUrl =new HashSet<>();
            List<String> fileName = new ArrayList<>();
            for(Element element : fileelements){
                fileUrl.add(element.attr("href"));
                fileName.add(element.attr("download"));
            }
            String description = BinaryDownloader.download(description2,fileUrl,url,fileName);
            serviceSupplier.description = description1+description;
        }else{
            serviceSupplier.description = description1;
        }

        //抓取乙方项目
        Elements elements = doc.getElementsByClass("case-item");
        ArrayList<CaseTask> casesTaskList = new ArrayList<>();
        String casesUrl = "https://www.clouderwork.com";
        if(elements.size()>0){
            for(Element element : elements){
                try {
                    tasks.add(new CaseTask(casesUrl+element.attr("href"),getUrl()));
                } catch (MalformedURLException e) {
                    logger.info("error on add taska",e);
                } catch (URISyntaxException e) {
                    logger.info("error on add tasks",e);
                }
            }
            for(Task t : tasks) {
                ChromeDriverRequester.getInstance().submit(t);
            }
        }
        try {
            serviceSupplier.insert();
        } catch (Exception e) {
            logger.error("error on insert serviceSupplier", e);
        }
    }

}
