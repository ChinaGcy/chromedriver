package com.sdyk.ai.crawler.specific.mihuashi.task.scanTask;

import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.mihuashi.action.TendererRatingActive;
import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ServiceRatingTask;
import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ServiceSupplierTask;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import org.jsoup.nodes.Document;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class ProjectScanTask extends com.sdyk.ai.crawler.specific.mihuashi.task.ScanTask {

    public static ProjectScanTask generateTask(String zone_id, int page){

        //生成LIST页
        StringBuffer url = new StringBuffer("https://www.mihuashi.com/projects?zone_id=");
        url.append(zone_id);
        url.append("&page=");
        url.append(page);

        //创建任务
        try {
            ProjectScanTask t = new ProjectScanTask(url.toString(), zone_id, page);
            return t;
        } catch (MalformedURLException | URISyntaxException e) {
            logger.error(e);
        }

        return null;
    }


    /**
     *
     * @param url
     * @param zone_id
     * @param page
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public ProjectScanTask(String url, String zone_id, int page) throws MalformedURLException, URISyntaxException {

        super(url);

        // 设定高优先级
        this.setPriority(Priority.HIGH);

        this.addDoneCallback(() -> {

            List<Task> task = new ArrayList<>();

            Document doc = getResponse().getDoc();

            String src = doc.select("#projects > div.grid-col-10 > div.projects__list").toString();
            // 设置页面路径
            String pagePath = "#projects-index > div.container-fluid > div.container > div > div > nav > span.last > a";

            // A 获取项目任务 TODO 注意去重
            Pattern pattern = Pattern.compile("(?<=/projects/)\\d+");
            Matcher matcher = pattern.matcher(src);
            Set<String> usernames = new HashSet<>();
            while (matcher.find()) {
                try {
                    usernames.add("https://www.mihuashi.com/projects/" + matcher.group());
                } catch (Exception e) {
                    logger.error(e);
                }
            }
            for(String un : usernames){
                try {
                    //添加抓取服务商信息任务
                    task.add(new ProjectTask(un));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            }

            //判断是否为最大页
            if( pageTurning(pagePath, page) )
            {
                int nextPage = page+1;
                Task t = generateTask(zone_id, nextPage);
                if (t != null) {
                    t.setBuildDom();
                    t.setPriority(one.rewind.io.requester.Task.Priority.HIGH);
                    task.add(t);
                }
            }

            for(Task t : task) {
                ChromeDriverRequester.getInstance().submit(t);
            }
        });
    }

    /**
     * 判断是否为最大页数
     *
     * @param path
     * @param page
     * @return
     */
    @Override
    public boolean pageTurning(String path, int page) {
        boolean pageTurningFlag = true;
        Document doc = getResponse().getDoc();
        try{
            String pagr = doc.select(path).text();
        }catch (Exception e){
            pageTurningFlag = false;
        }
        return pageTurningFlag;
    }

    @Override
    public TaskTrace getTaskTrace() {
        return null;
    }

    @Override
    public one.rewind.io.requester.Task validate() throws ProxyException.Failed, AccountException.Failed, AccountException.Frozen {
        return this;
    }
}
