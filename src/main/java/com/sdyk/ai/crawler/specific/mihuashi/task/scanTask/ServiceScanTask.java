package com.sdyk.ai.crawler.specific.mihuashi.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ServiceRatingTask;
import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ServiceProviderTask;
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
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceScanTask extends ScanTask {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				ServiceScanTask.class,
				"https://www.mihuashi.com/artists?page={{page}}",
				ImmutableMap.of("page", String.class, "max_page", String.class),
				ImmutableMap.of("page","", "max_page", "3")
		);
	}

    public ServiceScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

        super(url);

        this.setPriority(Priority.HIGH);

	    this.setNoFetchImages();

        this.setParam("page", url.split("page=")[1]);

        this.addDoneCallback((t) -> {

            String pagePath = "#artists-index > div.container-fluid > div.container > div > div > nav > span.last > a";

	        int page = Integer.valueOf(getUrl().split("page=")[1]);

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
		            init_map.put("service_id", un);

		            Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ServiceProviderTask");

		            //生成holder
		            ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

		            //提交任务
		            ((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

	            } catch ( Exception e) {

		            logger.error("error for submit ServiceProviderTask.class", e);
	            }

	            //添加服务商评价任务
	            try {

		            //设置参数
		            Map<String, Object> init_map = new HashMap<>();
		            init_map.put("service_id", un);

		            Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ServiceRatingTask");

		            //生成holder
		            ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

		            //提交任务
		            ((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

	            } catch ( Exception e) {

		            logger.error("error for submit ServiceRatingTask.class", e);
	            }

            }

	        String maxPageSrc =  String.valueOf(((ChromeTask) t).init_map.get("max_page"));
            if( maxPageSrc.length() < 1 ){
	            if( pageTurning(pagePath, page) ){
		            int nextP = page+1;

		            try {

			            //设置参数
			            Map<String, Object> init_map = new HashMap<>();
			            init_map.put("page", String.valueOf(nextP));
			            init_map.put("max_page", "");

			            Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.mihuashi.task.scanTask.ServiceScanTask");

			            //生成holder
			            ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

			            //提交任务
			            ((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

		            } catch ( Exception e) {

			            logger.error("error for submit ServiceScanTask.class", e);
		            }

	            }
            }
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
