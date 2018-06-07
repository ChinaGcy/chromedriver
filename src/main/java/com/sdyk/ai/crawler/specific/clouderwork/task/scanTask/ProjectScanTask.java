package com.sdyk.ai.crawler.specific.clouderwork.task.scanTask;

import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
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

public class ProjectScanTask extends com.sdyk.ai.crawler.specific.clouderwork.task.ScanTask {

    public static ProjectScanTask generateTask(int page){

        //生成LIST页
        StringBuffer url = new StringBuffer("https://www.clouderwork.com/api/v2/jobs/search?ts=pagesize=20&pagenum=");
        url.append(page);

        //创建任务
        try {
            ProjectScanTask t = new ProjectScanTask(url.toString(),page);
            t.setRequester_class(ChromeDriverRequester.class.getSimpleName());
            return t;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        return null;
    }

    //设置任务
    public ProjectScanTask(String url,int page) throws MalformedURLException, URISyntaxException {
        super(url);
        this.setPriority(Priority.HIGH);
        this.setBuildDom();
        String sign = "jobs";

        this.addDoneCallback(() -> {

            String src = getResponse().getDoc().text();

            Pattern pattern = Pattern.compile("\"id\":\"(?<username>.{16}?)\",\"user_id\"");
            Matcher matcher = pattern.matcher(src);

            Set<String> usernames = new HashSet<>();

            while(matcher.find()) {
                try {
                    usernames.add(URLDecoder.decode(matcher.group("username"), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            List<Task> task = new ArrayList<>();

            for(String user : usernames){
                String pUrl = "https://www.clouderwork.com/jobs/" + user;
                try {
                    task.add(new ProjectTask(pUrl));
                } catch (MalformedURLException e) {
                    logger.error("error on creat task", e);
                } catch (URISyntaxException e) {
                    logger.error("error on creat task", e);
                }
            }

            if( usernames.size()>0 ){
                Task t = generateTask(page + 1);
                if (t != null) {
                    t.setBuildDom();
                    t.setPriority(Priority.HIGH);
                    task.add(t);
                }
            }
            logger.info("Task driverCount: {}", task.size());

            for(Task t : task) {
                ChromeDriverRequester.getInstance().submit(t);
            }

        });

    }

    @Override
    public TaskTrace getTaskTrace() {
        return null;
    }

}