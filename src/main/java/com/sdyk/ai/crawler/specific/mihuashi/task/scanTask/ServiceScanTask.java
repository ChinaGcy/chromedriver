package com.sdyk.ai.crawler.specific.mihuashi.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ServiceRatingTask;
import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ServiceProviderTask;
import one.rewind.io.requester.exception.ProxyException;
import org.jsoup.nodes.Document;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceScanTask extends ScanTask {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				ServiceScanTask.class,
				"https://www.mihuashi.com/artists?page={{page}}",
				ImmutableMap.of("page", String.class),
				ImmutableMap.of("page","")
		);
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
		            HttpTaskPoster.getInstance().submit(ServiceProviderTask.class,
				            ImmutableMap.of("service_id", un));

	            } catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {
		            logger.error("error for HttpTaskPoster.submit ServiceProviderTask", e);
	            }

	            //添加服务商评价任务
	            try {
		            HttpTaskPoster.getInstance().submit(ServiceRatingTask.class,
				            ImmutableMap.of("service_id", un));

	            } catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {
		            logger.error("error for HttpTaskPoster.submit ServiceProviderTask", e);
	            }

            }

            if( pageTurning(pagePath, page) ){
                int nextP = page+1;

	            try {
		            HttpTaskPoster.getInstance().submit(ServiceScanTask.class,
				            ImmutableMap.of("page", String.valueOf(nextP)));

	            } catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {
		            logger.error("error for HttpTaskPoster.submit ServiceScanTask", e);
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
