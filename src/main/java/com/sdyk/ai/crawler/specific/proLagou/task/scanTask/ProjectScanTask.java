package com.sdyk.ai.crawler.specific.proLagou.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
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

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				ProjectScanTask.class,
				"https://pro.lagou.com/project/{{page}}",
				ImmutableMap.of("page", String.class, "max_page", String.class),
				ImmutableMap.of("page","", "max_page", "2")
		);
	}

    public ProjectScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

        this.setPriority(Priority.HIGH);

	    this.setNoFetchImages();

        this.setParam("page", url.split("project/")[1]);

        this.setValidator((a,t) -> {

        	String text = getResponse().getText();
        	if( text.contains("被封禁") ){
        		throw new ProxyException.Failed(a.proxy);
	        }

        });

        this.addDoneCallback((t) -> {

	        int page = Integer.valueOf(url.split("project/")[1]);

            Document doc = getResponse().getDoc();

            String s = doc.select("#project_list > ul").toString();

            Pattern pattern = Pattern.compile("https://pro.lagou.com/project/(?<ProjectId>.+?).html");
            Matcher matcher = pattern.matcher(s);

            while (matcher.find()) {

            	String project_id = matcher.group("ProjectId");

	            try {

		            //设置参数
		            Map<String, Object> init_map = new HashMap<>();
		            init_map.put("project_id", project_id);

		            Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.proLagou.task.modelTask.ProjectTask");

		            //生成holder
		            ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

		            //提交任务
		            ((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

	            } catch ( Exception e) {

		            logger.error("error for submit scanTaskServiceScanTask.class", e);
	            }

            }

            String maxPageSrc =  String.valueOf(((ChromeTask) t).init_map.get("max_page"));

            // 不含 max_page 参数，则表示可以一直翻页
	        if( maxPageSrc.length() < 1 ){
		        String pagePath = "#pager > div > span:nth-child(9)";
		        if(pageTurning(pagePath, page)){
			        int nextPage = page+1;

			        try {

				        //设置参数
				        Map<String, Object> init_map = new HashMap<>();
				        init_map.put("page", String.valueOf(nextPage));
				        init_map.put("max_page", "");

				        Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.proLagou.task.scanTask.ProjectScanTask");

				        //生成holder
				        ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

				        //提交任务
				        ((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

			        } catch ( Exception e) {

				        logger.error("error for submit ProjectScanTask.class", e);
			        }

		        }
	        }
	        // 含有 max_page 参数，若max_page小于当前页则不进行翻页
	        else {

		        int maxPage = Integer.valueOf(maxPageSrc);
		        int current_page = Integer.valueOf(String.valueOf(((ChromeTask) t).init_map.get("page")));

		        for(int i = current_page + 1; i <= maxPage; i++) {

			        Map<String, Object> init_map = new HashMap<>();
			        init_map.put("page", String.valueOf(i));
			        init_map.put("max_page", "0");

			        ChromeTaskHolder holder = ((ChromeTask) t).getHolder(((ChromeTask) t).getClass(), init_map);

			        ChromeDriverDistributor.getInstance().submit(holder);
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

		return new TaskTrace(this.getClass(), "all", this.getParamString("page"));
	}
}
