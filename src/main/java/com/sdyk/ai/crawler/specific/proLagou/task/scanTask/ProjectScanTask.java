package com.sdyk.ai.crawler.specific.proLagou.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.proLagou.task.modelTask.ProjectTask;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import org.jsoup.nodes.Document;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectScanTask extends com.sdyk.ai.crawler.task.ScanTask {

	static {
		registerBuilder(
				ProjectScanTask.class,
				"https://pro.lagou.com/project/{{page}}",
				ImmutableMap.of("page", String.class),
				ImmutableMap.of("page","")
		);
	}

    public ProjectScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

        this.setPriority(Priority.HIGH);

        this.addDoneCallback((t) -> {

	        int page = 0;
	        Pattern pattern_url = Pattern.compile("project/(?<page>.+?)");
	        Matcher matcher_url = pattern_url.matcher(url);
	        if (matcher_url.find()) {
		        page = Integer.parseInt(matcher_url.group("page"));
	        }

            Document doc = getResponse().getDoc();

            String s = doc.select("#project_list > ul").toString();

            Pattern pattern = Pattern.compile("https://pro.lagou.com/project/(?<ProjectId>.+?).html");
            Matcher matcher = pattern.matcher(s);

            while (matcher.find()) {

            	String project_id = matcher.group("ProjectId");

	            try {

		            //设置参数
		            Map<String, Object> init_map = new HashMap<>();
		            ImmutableMap.of("project_id", project_id);

		            Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.proLagou.task.modelTask.ProjectTask");

		            //生成holder
		            ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

		            //提交任务
		            ChromeDriverDistributor.getInstance().submit(holder);

	            } catch ( Exception e) {

		            logger.error("error for submit scanTaskServiceScanTask.class", e);
	            }

            }

            String pagePath = "#pager > div > span:nth-child(9)";
            if(pageTurning(pagePath, page)){
                int nextPage = page+1;

	            try {

		            //设置参数
		            Map<String, Object> init_map = new HashMap<>();
		            ImmutableMap.of("page", String.valueOf(nextPage));

		            Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.proLagou.task.scanTask.ProjectScanTask");

		            //生成holder
		            ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

		            //提交任务
		            ChromeDriverDistributor.getInstance().submit(holder);

	            } catch ( Exception e) {

		            logger.error("error for submit ProjectScanTask.class", e);
	            }

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
