package com.sdyk.ai.crawler.specific.mihuashi.task.scanTask;

import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ServiceRatingTask;
import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ServiceSupplierTask;
import com.sdyk.ai.crawler.specific.mihuashi.action.LoadMoreContentAction;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.chrome.action.ClickAction;
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

public class ServiceScanTask extends com.sdyk.ai.crawler.specific.mihuashi.task.ScanTask {

    public String workFilePath = "#users-show > div.container-fluid > div.profile__container > main > header > ul > li:nth-child(2) > a";

    public String morePath = "#vue-comments-app > div:nth-child(2) > a";

    public static ServiceScanTask generateTask(int page) {

        StringBuffer url = new StringBuffer("https://www.mihuashi.com/artists?page=");
        url.append(page);

        try {
            ServiceScanTask t = new ServiceScanTask(url.toString(),page);
            return t;
        } catch (MalformedURLException e) {
            logger.error("error for creat serviceScanTask",e);
        } catch (URISyntaxException e) {
            logger.error("error for creat serviceScanTask",e);
        }

        return null;
    }


    public ServiceScanTask(String url,int page) throws MalformedURLException, URISyntaxException {

        super(url);

        this.setPriority(Priority.HIGH);
        this.setBuildDom();
        this.addDoneCallback(() -> {

            String pagePath = "#artists-index > div.container-fluid > div.container > div > div > nav > span.last > a";

            List<Task> task = new ArrayList<>();
            Document doc = getResponse().getDoc();
            String src = getResponse().getText();

            Pattern pattern = Pattern.compile("/users/(?<username>.+?)\\?role=painter");
            Matcher matcher = pattern.matcher(src);

            Set<String> usernames = new HashSet<>();
            while(matcher.find()) {
                try {
                    usernames.add(URLDecoder.decode(matcher.group("username"), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            for(String un : usernames){
                try {

                    //添加抓取服务商信息任务
                    Task taskW = new ServiceSupplierTask("https://www.mihuashi.com/users/"+un+"?role=painter");
                    task.add(taskW);

                    //添加服务商评价任务
                    Task taskSR = new ServiceRatingTask("https://www.mihuashi.com/users/"+un+"?role=painter&rating=true");
                    taskSR.addAction(new ClickAction( workFilePath ));
                    taskSR.addAction(new LoadMoreContentAction(morePath));
                    task.add(taskSR);

                } catch (MalformedURLException e) {
                    logger.error("error for creat task", e);
                } catch (URISyntaxException e) {
                    logger.error("error for creat task", e);
                }
            }

            if( pageTurning(pagePath, page) ){
                int nextP = page+1;
                Task t = generateTask(nextP);
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
            String nextPage = doc.select(path).text();
            if ( nextPage == null || "".equals(nextPage) ) {}
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
        return null;
    }
}
