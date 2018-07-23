package com.sdyk.ai.crawler.specific.mihuashi.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ServiceRatingTask;
import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ServiceProviderTask;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import org.jsoup.nodes.Document;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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

        this.setParam("page", init_map.get("page"));

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

		            //设置参数
		            Map<String, Object> init_map = new HashMap<>();
		            ImmutableMap.of("service_id", un);

		            Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ServiceProviderTask");

		            //生成holder
		            ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

		            //提交任务
		            ChromeDriverDistributor.getInstance().submit(holder);

	            } catch ( Exception e) {

		            logger.error("error for submit ServiceProviderTask.class", e);
	            }

	            //添加服务商评价任务
	            try {

		            //设置参数
		            Map<String, Object> init_map = new HashMap<>();
		            ImmutableMap.of("service_id", un);

		            Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ServiceRatingTask");

		            //生成holder
		            ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

		            //提交任务
		            ChromeDriverDistributor.getInstance().submit(holder);

	            } catch ( Exception e) {

		            logger.error("error for submit ServiceRatingTask.class", e);
	            }

            }

            if( pageTurning(pagePath, page) ){
                int nextP = page+1;

	            try {

		            //设置参数
		            Map<String, Object> init_map = new HashMap<>();
		            ImmutableMap.of("page", String.valueOf(nextP));

		            Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.mihuashi.task.scanTask.ServiceScanTask");

		            //生成holder
		            ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

		            //提交任务
		            ChromeDriverDistributor.getInstance().submit(holder);

	            } catch ( Exception e) {

		            logger.error("error for submit ServiceScanTask.class", e);
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

	    return new TaskTrace(this.getClass(), "all", this.getParamString("page"));
    }

}
