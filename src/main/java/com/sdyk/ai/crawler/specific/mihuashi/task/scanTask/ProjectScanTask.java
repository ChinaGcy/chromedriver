package com.sdyk.ai.crawler.specific.mihuashi.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ProjectTask;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import org.jsoup.nodes.Document;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class ProjectScanTask extends ScanTask {

	static {
		registerBuilder(
				ProjectScanTask.class,
				"https://www.mihuashi.com/projects?zone_id={{zone_id}}&page={{page}}",
				ImmutableMap.of("zone_id", String.class, "page", String.class),
				ImmutableMap.of("tenderer_id", "","page","")
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
        this.setPriority(Priority.MEDIUM);

        this.setBuildDom();

        this.addDoneCallback((t) -> {

	        int page = 0;
	        Pattern pattern_url = Pattern.compile("page=(?<page>.+?)");
	        Matcher matcher_url = pattern_url.matcher(url);
	        if (matcher_url.find()) {
		        page = Integer.parseInt(matcher_url.group("page"));
	        }

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
                    usernames.add("https://www.mihuashi.com/projects/" + matcher.group());
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
		            ChromeDriverDistributor.getInstance().submit(holder);


	            } catch (Exception e) {
		            logger.error("error for submit ProjectTask", e);
	            }

            }

            //判断是否为最大页
            if( pageTurning(pagePath, page) )
            {

	            try {

		            //设置参数
		            Map<String, Object> init_map = new HashMap<>();
		            init_map.put("page", String.valueOf(++page));
		            init_map.put("zoneId", zoneId);

		            //生成holder
		            ChromeTaskHolder holder = ChromeTask.buildHolder(ProjectScanTask.class, init_map);

		            //提交任务
		            Distributor.getInstance().submit(holder);


	            } catch (Exception e) {
		            logger.error("error for submit ProjectScanTask", e);
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
        return null;
    }

	public static void registerBuilder(Class<? extends ChromeTask> clazz, String url_template, Map<String, Class> init_map_class, Map<String, Object> init_map_defaults){
		ChromeTask.registerBuilder( clazz, url_template, init_map_class, init_map_defaults );
	}

}
