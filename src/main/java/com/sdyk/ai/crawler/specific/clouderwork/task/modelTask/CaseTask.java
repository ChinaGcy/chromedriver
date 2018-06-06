package com.sdyk.ai.crawler.specific.clouderwork.task.modelTask;

import com.sdyk.ai.crawler.model.Case;
import com.sdyk.ai.crawler.specific.clouderwork.task.Task;
import org.jsoup.nodes.Document;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

public class CaseTask extends Task {
    public CaseTask(String url,String useUrl) throws MalformedURLException, URISyntaxException {

        super(url);
        this.setBuildDom();
        this.addDoneCallback(() -> {

            String src = getResponse().getText();
            Document doc = getResponse().getDoc();
            crawlerJob(doc,useUrl);

        });

    }

    public void crawlerJob(Document doc, String useUrl){

        Pattern pattern = Pattern.compile("[0-9]*");
        String url = getUrl();
        Case caseinfor = new Case(url);
        String[] userId = useUrl.split("ers/");
        //乙方ID
        caseinfor.user_id = userId[1];
        String title = doc.getElementsByClass("p-title").text();
        //项目名称
        if(title!=null&&!"".equals(title)){
            caseinfor.title = title;
        }
        String all = doc.getElementsByClass("info").text().replace("类型：","");
        if(all!=null&&!"".equals(all)){
            //抓取类型
            String[] all1 = all.split("行业：");
            if(all1[0]!=null&&!"".equals(all1[0])){
                caseinfor.category = all1[0];
            }
            //抓取行业
            String[] all2 = all1[1].split("工期：");
            if(all2[0]!=null&&!"".equals(all2[0])){
                caseinfor.tags = all2[0];
            }
            //抓取工期
            String[] all3 = all2[1].split("报价：");
            if(all3[0]!=null&&!"".equals(all3[0])){
                caseinfor.cycle = all3[0];
            }
            //抓取报价
            if(all3[1]!=null&&!"".equals(all3[1])){
                String budge = all3[1].replace(",","").replace("¥","");
                if(budge!=null&!"".equals(budge)&&pattern.matcher(budge).matches()){
                    try {
                        caseinfor.budget_lb = Integer.valueOf(budge);
                        caseinfor.budget_ub = Integer.valueOf(budge);
                    } catch (Exception e) {
                        logger.error("error on String to Integer", e);
                    }
                }
            }
        }
        //抓取描述
        String descreption = doc.getElementsByClass("desc simditor-content").html();
        if(descreption!=null&&!"".equals(descreption)){
            caseinfor.description = descreption;
        }
        try {
            caseinfor.insert();
        } catch (Exception e) {
            logger.error("error on insert case", e);
        }
    }
}
