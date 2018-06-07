package com.sdyk.ai.crawler.specific.clouderwork.task.scanTask;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ServiceSupplierTask;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import java.io.IOException;
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

public class ServiceScanTask extends com.sdyk.ai.crawler.specific.clouderwork.task.ScanTask {
    public static ServiceScanTask generateTask(int page) {

        StringBuffer url = new StringBuffer("https://www.clouderwork.com/api/v2/freelancers/search?pagesize=10&pagenum=");
        url.append(page);
        try {
            ServiceScanTask ta = new ServiceScanTask(url.toString(),page);
            return ta;
        } catch (MalformedURLException e) {
            logger.info("error on creat ckouderWork serviceScanTask",e);
        } catch (URISyntaxException e) {
            logger.info("error on creat ckouderWork serviceScanTask",e);
        }
        return null;
    }

    /**
     *
     * @param url
     * @param page
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public ServiceScanTask (String url, int page) throws MalformedURLException, URISyntaxException {

        super(url);

        this.setPriority(Priority.HIGH);
        this.setBuildDom();

        this.addDoneCallback(() -> {

            String src = getResponse().getDoc().text();

            Pattern pattern = Pattern.compile("\"id\":\"(?<username>.{16}?)\",\"plus_type\"");
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
                String pUrl = "https://www.clouderwork.com/freelancers/" + user;
                try {
                    task.add(new ServiceSupplierTask(pUrl));
                } catch (MalformedURLException e) {
                    logger.error("error on creat task", e);
                } catch (URISyntaxException e) {
                    logger.error("error on creat task", e);
                }
            }

            if( usernames.size()>0 ){
                Task t = generateTask(page + 1);
                if (t != null) {
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