package com.sdyk.ai.crawler.specific.mihuashi.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ServiceRatingTask;
import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ServiceProviderTask;
import com.sdyk.ai.crawler.specific.mihuashi.action.LoadMoreContentAction;
import com.sdyk.ai.crawler.task.Task;
import com.sdyk.ai.crawler.util.URLUtil;
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

public class ServiceScanTask extends ScanTask {

	static {
		// init_map_class
		init_map_class = ImmutableMap.of("page", String.class);
		// init_map_defaults
		init_map_defaults = ImmutableMap.of("q", "ip");
		// url_template
		url_template = "https://www.mihuashi.com/artists?page={{page}}";
	}

    public ServiceScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

        super(url);

        this.setPriority(Priority.HIGH);

        this.addDoneCallback((t) -> {

            String pagePath = "#artists-index > div.container-fluid > div.container > div > div > nav > span.last > a";

	        int page = 0;
	        Pattern pattern_url = Pattern.compile("page=(?<page>.+?)");
	        Matcher matcher_url = pattern_url.matcher(url);
	        if (matcher_url.find()) {
		        page = Integer.parseInt(matcher_url.group("page"));
	        }

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

	            //添加抓取服务商信息任务
	            try {
		            URLUtil.PostTask(ServiceProviderTask.class,
				            null,
				            ImmutableMap.of("service_id", un),
				            null,
				            null,
				            null,
				            null,
				            null);

	            } catch (Exception e) {
		            logger.error("error for URLUtil.PostTask ServiceProviderTask.class", e);
	            }

	            //添加服务商评价任务
	            try {
		            URLUtil.PostTask(ServiceRatingTask.class,
				            null,
				            ImmutableMap.of("service_id", un),
				            null,
				            null,
				            null,
				            null,
				            null);

	            } catch (Exception e) {
		            logger.error("error for URLUtil.PostTask ServiceRatingTask.class", e);
	            }

            }

            if( pageTurning(pagePath, page) ){
                int nextP = page+1;
	            try {
		            URLUtil.PostTask(ServiceScanTask.class,
				            null,
				            ImmutableMap.of("page", String.valueOf(nextP)),
				            null,
				            null,
				            null,
				            null,
				            null);

	            } catch (Exception e) {
		            logger.error("error for URLUtil.PostTask ServiceRatingTask.class", e);
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

}
