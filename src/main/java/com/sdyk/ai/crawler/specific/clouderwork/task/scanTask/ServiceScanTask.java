package com.sdyk.ai.crawler.specific.clouderwork.task.scanTask;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ServiceSupplierTask;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ServiceScanTask extends ScanTask {

    public static ServiceScanTask generateTask(int page) {

        StringBuffer url = new StringBuffer("https://www.clouderwork.com/api/v2/freelancers/search?pagesize=10&pagenum=");
        url.append(page);
        try {
            ServiceScanTask t = new ServiceScanTask(url.toString(),page);
            return t;
        } catch (MalformedURLException e) {
            logger.info("error on creat serviceScanTask",e);
        } catch (URISyntaxException e) {
            logger.info("error on creat serviceScanTask",e);
        }
        return null;
    }


    public ServiceScanTask (String url,int page) throws MalformedURLException, URISyntaxException {
        super(url);
        this.setPriority(Priority.HIGH);
        this.setBuildDom();
        String sign = "users";

        this.addDoneCallback(() -> {
            logger.info("执行搜索任务ServiceScanTask");
            String src = getResponse().getDoc().text().replace("/U",",U ");
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
            List<Task> tasks = new ArrayList<>();
            try {
                JsonNode node = mapper.readTree(src).get("users");
                for(int i = 0;i<node.size();i++){
                    tasks.add(new ServiceSupplierTask("https://www.clouderwork.com/freelancers/"+node.get(i).get("id").toString().replace("\"","")));
                }
                logger.info("ServiceSupplierTaskSize",tasks.size());
            } catch (IOException e) {
                logger.info("error on String to Json",e);
            } catch (URISyntaxException e) {
                logger.info("error on add task",e);
            }

            try {
                if(!judjeMaxPage(page,sign,getUrl())){
                    Task t = ServiceScanTask.generateTask(page+1);
                    tasks.add(t);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            logger.info("Task driverCount: {}", tasks.size());

            for(Task t : tasks) {
                ChromeDriverRequester.getInstance().submit(t);
            }

        });
    }

}
