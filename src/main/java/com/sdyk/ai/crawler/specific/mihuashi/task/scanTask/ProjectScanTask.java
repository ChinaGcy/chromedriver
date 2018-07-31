package com.sdyk.ai.crawler.specific.mihuashi.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ProjectTask;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.ChromeTaskScheduler;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import one.rewind.io.requester.task.ScheduledChromeTask;
import org.jsoup.nodes.Document;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class ProjectScanTask extends ScanTask {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				ProjectScanTask.class,
				"https://www.mihuashi.com/projects?zone_id={{zone_id}}&page={{page}}",
				ImmutableMap.of("zone_id", String.class, "page", String.class, "max_page", String.class),
				ImmutableMap.of("zone_id", "","page","", "max_page", "3")
		);
	}


    /**
     *
     * @param url
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public ProjectScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

        super(url);

        // 设定高优先级
        this.setPriority(Priority.HIGH);

	    this.setNoFetchImages();

        this.setParam("page", url.split("page=")[1]);

        this.addDoneCallback((t) -> {

	        int page = Integer.valueOf(getUrl().split("page=")[1]);

	        String zoneId = "1";
	        Pattern pattern_zoneId = Pattern.compile("zone_id=(?<zoneId>.+?)&page");
	        Matcher matcher_zoneId = pattern_zoneId.matcher(url);
	        if (matcher_zoneId.find()) {
		        zoneId = matcher_zoneId.group("zoneId");
	        }


            Document doc = getResponse().getDoc();

            String src = doc.select("#projects > div.grid-col-10 > div.projects__list").toString();
            // 设置页面路径
            String pagePath = "last";

            // A 获取项目任务 TODO 注意去重
            Pattern pattern = Pattern.compile("(?<=/projects/)\\d+");
            Matcher matcher = pattern.matcher(src);
            Set<String> usernames = new HashSet<>();
            while (matcher.find()) {
                try {
                    usernames.add(matcher.group());
                } catch (Exception e) {
                    logger.error(e);
                }
            }
            for(String un : usernames){

	            try {

		            //设置参数
		            Map<String, Object> init_map = new HashMap<>();
		            init_map.put("project_id", un);

		            Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ProjectTask");

		            //生成holder
		            ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

		            //提交任务
		            ((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);


	            } catch (Exception e) {
		            logger.error("error for submit ProjectTask", e);
	            }

            }

            String maxPageSrc =  String.valueOf(((ChromeTask) t).init_map.get("max_page"));

            // 不含 max_page 参数，则表示可以一直翻页
	        if( maxPageSrc.length() < 1 ){

		        //判断是否为最大页
		        if( pageTurning(pagePath, page) )
		        {

			        try {

				        //设置参数
				        Map<String, Object> init_map = new HashMap<>();
				        init_map.put("zone_id", zoneId);
				        init_map.put("page", String.valueOf(++page));
				        init_map.put("max_page", "");

				        //生成holder
				        ChromeTaskHolder holder = ChromeTask.buildHolder(ProjectScanTask.class, init_map);

				        //提交任务
				        ((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);


			        } catch (Exception e) {
				        logger.error("error for submit ProjectScanTask", e);
			        }

		        }
	        }
	        else {
		        int maxPage = Integer.valueOf(maxPageSrc);
		        int current_page = Integer.valueOf(String.valueOf(((ChromeTask) t).init_map.get("page")));

		        for(int i = current_page + 1; i <= maxPage; i++) {

			        Map<String, Object> init_map = new HashMap<>();
			        init_map.put("page", String.valueOf(i));
			        init_map.put("zone_id", zoneId);
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
        boolean pageTurningFlag = true;
        Document doc = getResponse().getDoc();
        try{
            String pagr = doc.getElementsByClass("last").text();
            if( pagr == null || "".equals(pagr) ) {
                pageTurningFlag = false;
            }
        }catch (Exception e){
            pageTurningFlag = false;
        }
        return pageTurningFlag;
    }

    @Override
    public TaskTrace getTaskTrace() {

	    return new TaskTrace(this.getClass(), "all", this.getParamString("page"));
    }

}
