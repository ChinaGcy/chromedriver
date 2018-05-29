package com.sdyk.ai.crawler.specific.clouderwork.task.scanTask;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

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

            String src = getResponse().getDoc().text().replace("/UE",",UE");
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
            List<Task> task = new ArrayList<>();
            try {
                JsonNode node = mapper.readTree(src).get("jobs");
                for(int i = 0;i<node.size();i++){
                    String pUrl = "https://www.clouderwork.com/jobs/"+node.get(i).get("id").toString().replace("\"","");
                    task.add(new ProjectTask(pUrl));
                }
                //添加下一页任务
                if(node.size()>0){
                    Task t = generateTask(page + 1);
                    if (t != null) {
                        t.setBuildDom();
                        t.setPriority(Priority.HIGH);
                        task.add(t);
                    }
                }

                logger.info("projectTaskSize",task.size());
            } catch (IOException e) {
                logger.info("error on String to Json",e);
            } catch (URISyntaxException e) {
                logger.info("error on add task",e);
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
