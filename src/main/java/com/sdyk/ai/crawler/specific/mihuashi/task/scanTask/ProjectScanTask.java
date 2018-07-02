package com.sdyk.ai.crawler.specific.mihuashi.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.util.URLUtil;
import one.rewind.io.requester.exception.ProxyException;
import org.jsoup.nodes.Document;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class ProjectScanTask extends ScanTask {

	static {
		// init_map_class
		init_map_class = ImmutableMap.of("page", String.class, "zone_id", String.class);
		// init_map_defaults
		init_map_defaults = ImmutableMap.of("q", "ip");
		// url_template
		url_template = "https://www.mihuashi.com/projects?zone_id={{zone_id}}&page={{page}}";
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
		            URLUtil.PostTask(ProjectTask.class,
				            null,
				            ImmutableMap.of("project_id", un),
				            null,
				            null,
				            null,
				            null,
				            null);

	            } catch (Exception e) {
		            logger.error("error for URLUtil.PostTask ProjectTask.class", e);
	            }

            }

            //判断是否为最大页
            if( pageTurning(pagePath, page) )
            {
	            URLUtil.PostTask(ProjectScanTask.class,
			            null,
			            ImmutableMap.of( "page", String.valueOf(++page), "zoneId", zoneId),
			            null,
			            null,
			            null,
			            null,
			            null);
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

}
