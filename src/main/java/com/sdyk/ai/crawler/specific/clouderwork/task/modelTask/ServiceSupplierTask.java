package com.sdyk.ai.crawler.specific.clouderwork.task.modelTask;

import com.sdyk.ai.crawler.model.Resume;
import com.sdyk.ai.crawler.model.ServiceProvider;
import com.sdyk.ai.crawler.model.ServiceProviderRating;
import com.sdyk.ai.crawler.specific.clouderwork.task.Task;
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

    ServiceProvider serviceProvider;

    Resume resume;

    public ServiceSupplierTask(String url) throws MalformedURLException, URISyntaxException {
        super(url);
        this.setPriority(Priority.MEDIUM);
        this.setBuildDom();
        this.addDoneCallback(() -> {

            Document doc = getResponse().getDoc();
            serviceProvider = new ServiceProvider(getUrl());
            resume = new Resume(getUrl());

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
        serviceProvider.origin_id =urls[1];

        //名字
        String name = doc.getElementsByClass("name-box").text();
        if( name!=null && !"".equals(name) ){
            serviceProvider.name = name;
        }
        //类型
        String type = doc.select("#profile > div > div > section.left > div.team > p").text();
        if( type!=null && !"".equals(type) ){
            String team = doc.getElementsByClass("team-s-title").text();
            serviceProvider.type = team;
        }
        //没有团队时类型为个人
        else {
            serviceProvider.type = "个人";
        }

        String all =doc.select("#profile > div > div > section.basic > section > div:nth-child(8)").text().replace("擅长领域：","");
        if( all!=null && !"".equals(all) && all.contains("擅长技能") ){
            String[] all1 = all.split("擅长技能：");
            String expertise = all1[0];
            //擅长领域
            if( expertise!=null && !"".equals(expertise) ){
                serviceProvider.category = expertise;
            }
            String[] all2 = all1[1].split("所在城市：");
            //擅长技能
            String skills = all2[0];
            if( skills!=null && !"".equals(skills) ){
                serviceProvider.tags = skills;
            }
            String[] all3 = all2[1].split("工作经验：");
            //地点
            String location = all3[0];
            if( location!=null && !"".equals(location) ){
                serviceProvider.location = location;
            }
        }
        //当行数据出现收藏标志时
        else if( all!=null && !"".equals(all) && all.contains("收藏")){
            String allOther = doc.select("#profile > div > div > section.basic > section > div:nth-child(7)").text();
            String[] all1 = allOther.split("擅长技能：");
            String expertise = all1[0];
            //擅长领域
            if(expertise!=null&&!"".equals(expertise)){
                serviceProvider.category = expertise;
            }
            String[] all2 = all1[1].split("所在城市：");
            //擅长技能
            String skills;
            if(all2[0].contains("预估时薪")){
                String[] all2try = all2[0].split("预估时薪");
                skills = all2try[0];
            }
            //不含预估时薪
            else {
                skills = all2[0];
            }
            if( skills!=null && !"".equals(skills) ){
                serviceProvider.tags = skills;
            }
            String[] all3 = all2[1].split("工作经验：");
            //地点
            String location = all3[0];
            if(location!=null&&!"".equals(location)){
                serviceProvider.location = location;
            }
        }

        //工作经验
        String workExperience = doc.getElementsByClass("main-item").text();
        if( workExperience!=null && !"".equals(workExperience) ){
            //serviceProvider.work_experience =workExperience;
        }


        //成员数量
        String memberNum = doc.select("#profile > div > div > section.left > div.team > div > a > div").text();
        if( memberNum!=null && !"".equals(memberNum) ){
            String[] memberNums = memberNum.split("名");
            String Num = memberNums[0].substring(1,memberNums[0].length());
            if( Num!=null && !"".equals(Num) && pattern.matcher(Num).matches() ){
                serviceProvider.team_size = Integer.valueOf(Num);
            }
        }
        //当为非团队时，成员数量为1
        else {
            serviceProvider.team_size = Integer.valueOf(1);
        }
        //平台项目数
        String projectNum = doc.select("#profile > div > div > section.left > div.evaluation > p.eva-desc > span:nth-child(1)").text();
        if( projectNum!=null && !"".equals(projectNum) && pattern.matcher(projectNum).matches()){
            serviceProvider.project_num = Integer.valueOf(projectNum);
        }
        //平台成功率
        String successRatio = doc.select("#profile > div > div > section.left > div.evaluation > p.eva-desc > span:nth-child(2)").text().replace("%","");
        if( successRatio!=null && !"".equals(successRatio) ){
            serviceProvider.success_ratio = Float.valueOf(successRatio);
        }
        //顾客评分
        String rating = doc.select("#profile > div > div > section.left > div.evaluation > div > span").text().replace("分","");
        if( rating!=null && !"".equals(rating) ){
            serviceProvider.rating = Float.valueOf(rating);
        }
        //评价数
        String ratingNum = doc.select("#profile > div > div > section.left > div.evaluation > p.only-sys").text();
        String[] ratingNums = ratingNum.split("个");
        if( ratingNums[0]!=null && !"".equals(ratingNums[0]) ){
            serviceProvider.rating_num = Integer.valueOf(ratingNums[0]);
            //好评数
            if( ratingNums.length>1 && ratingNums[1].contains("好评") ){
                serviceProvider.praise_num = serviceProvider.rating_num;
            }
        }

        //教育经历
        String eduAll = doc.getElementsByClass("main-content edu").text();
        if( eduAll!=null && !"".equals(eduAll) ){
            resume.user_id = serviceProvider.origin_id.split("cers/")[1];
            String eduTime = doc.getElementsByClass("edu-time").text().replace("年","");
            if( eduTime!=null && !"".equals(eduTime) ){
                String[] times = eduTime.split("-");
                try {
                    SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy");
                    Date startdate=simpleDateFormat.parse(times[0]);
                    Date enddate=simpleDateFormat.parse(times[1]);
                    resume.sd =startdate;
                    resume.ed = enddate;
                } catch(ParseException px) {
                    px.printStackTrace();
                }
            }
            String eduSchool = doc.getElementsByClass("edu-school").text();
            if( eduSchool!=null && !"".equals(eduSchool) ){
                resume.org = eduSchool;
            }
            String eduCity = doc.getElementsByClass("edu-city").text();
            if( eduCity!=null && !"".equals(eduCity) ){
                resume.dep = eduCity;
                String[] edu_position = eduAll.split(eduCity);
                if(edu_position.length > 1){
                    resume.degree_occupation = edu_position[1];
                }
            }
            try {
                resume.insert();
                logger.info(resume.toJSON());
            } catch (Exception e) {
                logger.error("error on insert resume", e);
            }
        }

        //评价
        Elements ratingElements = doc.getElementsByClass("company");
        if( ratingElements != null && ratingElements.size() > 0 ){
            for(Element element : ratingElements){
                ServiceProviderRating serviceProviderRating = new ServiceProviderRating(getUrl());
                //服务商ID
                serviceProviderRating.service_provider_id = getUrl().split("freelancers/")[1];
                //雇主名称
              //  serviceProviderRatings.tenderer_name = doc.getElementsByClass("comp-name").text();
                //评价内容
                serviceProviderRating.content = doc.getElementsByClass("comp-desc").text();
                serviceProviderRating.insert();
            }
        }


        //描述
        String description1 = doc.getElementsByClass("main-content").html();
        String description2 = doc.getElementsByClass("file").html();
        Elements fileelements = doc.getElementsByClass("download");

        if( fileelements!=null && fileelements.size()>0 ){
            Set<String> fileUrl =new HashSet<>();
            List<String> fileName = new ArrayList<>();
            for(Element element : fileelements){
                fileUrl.add(element.attr("href"));
                fileName.add(element.attr("download"));
            }
            String description = BinaryDownloader.download(description2,fileUrl,url,fileName);
            serviceProvider.content = description1+description;
        }
        //不含附件
        else {
            serviceProvider.content = description1;
        }

        //抓取乙方项目
        Elements elements = doc.getElementsByClass("case-item");
        ArrayList<CaseTask> casesTaskList = new ArrayList<>();
        String casesUrl = "https://www.clouderwork.com";
        if( elements.size()>0 ){
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
            serviceProvider.insert();
        } catch (Exception e) {
            logger.error("error on insert serviceProvider", e);
        }
    }

}
