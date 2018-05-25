package com.sdyk.ai.crawler.specific.proLagou.task.scanTask;

import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.proLagou.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import org.jsoup.nodes.Document;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectScanTask extends com.sdyk.ai.crawler.task.ScanTask {

    public static ProjectScanTask generateTask(int page){
        //生成LIST页
        StringBuffer url = new StringBuffer("https://pro.lagou.com/project/");
        url.append(page);

        //创建任务
        try {
            ProjectScanTask task = new ProjectScanTask(url.toString(),page);
            task.setRequester_class(ChromeDriverRequester.class.getSimpleName());
            return task;
        } catch (MalformedURLException e) {
            logger.error(e);
        } catch (URISyntaxException e) {
            logger.error(e);
        }
        return null;
    }



    public ProjectScanTask(String url, int page) throws MalformedURLException, URISyntaxException {
        super(url);
        this.setPriority(one.rewind.io.requester.Task.Priority.HIGH);
        this.setBuildDom();
        this.addDoneCallback(() -> {
            List<Task> task = new ArrayList<>();
            Document doc = getResponse().getDoc();
            String s = doc.select("#project_list > ul").toString();
            Pattern pattern = Pattern.compile("https://pro.lagou.com/project/\\d+.html");
            Matcher matcher = pattern.matcher(s);
            while (matcher.find()) {
                try {
                    task.add(new ProjectTask(matcher.group()));
                } catch (Exception e) {
                    logger.error(e);
                }
            }
            String pagePath = "#pager > div > span:nth-child(9)";
            if(pageTurning(pagePath, page)){
                int nextPage = page+1;
                Task t = generateTask(nextPage);
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

    @Override
    public one.rewind.io.requester.Task validate() throws ProxyException.Failed, AccountException.Failed, AccountException.Frozen {
        return null;
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

        Document doc = getResponse().getDoc();
        int nextPage = 0;
        try{
            String mexPage = doc.select(path).text();
            nextPage = Integer.valueOf(mexPage);
        }catch (Exception e){
            return false;
        }

        return nextPage>page;
    }

    @Override
    public TaskTrace getTaskTrace() {
        return null;
    }
}
