package com.sdyk.ai.crawler.specific.clouderwork.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ServiceProviderTask;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.ChromeTaskScheduler;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskFactory;
import one.rewind.io.requester.task.TaskHolder;
import one.rewind.io.requester.task.ScheduledChromeTask;

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
				"https://www.clouderwork.com/api/v2/freelancers/search?pagesize=10&pagenum={{page}}",
				ImmutableMap.of("page", String.class, "max_page", String.class),
				ImmutableMap.of("page", "1", "max_page", "")
		);
	}

    /**
     *
     * @param url
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public ServiceScanTask (String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

        super(url);

        this.setPriority(Priority.HIGHER);

	    this.setNoFetchImages();

        this.setParam("page", url.split("pagenum=")[1]);

        this.addDoneCallback((t) -> {

        	//获取当前页数
	        int page = Integer.valueOf(url.split("pagenum=")[1]);
	        System.out.println("当前页数为 ： " + page);

	        //获取页面信息
            String src = getResponse().getDoc().text();

	        //解析uesrId
	        Pattern pattern = Pattern.compile("\"id\":\"(?<username>.{16}?)\",\"plus_type\"");
            Matcher matcher = pattern.matcher(src);

            Set<String> usernames = new HashSet<>();

            //userId去重
            while(matcher.find()) {
                try {
                    usernames.add(URLDecoder.decode(matcher.group("username"), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            //生成智库任务
            for(String user : usernames){

	            try {

		            //设置参数
		            Map<String, Object> init_map = new HashMap<>();
		            init_map.put("servicer_id", user);

		            Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ServiceProviderTask");

		            //生成holder
		            TaskHolder holder =  ChromeTaskFactory.getInstance().newHolder(clazz, init_map);

		            //提交任务
		            ((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

	            } catch ( Exception e) {

		            logger.error("error for submit ServiceProviderTask.class", e);
	            }

            }

	        String maxPageSrc =  t.getStringFromVars("max_page");

            // 不含 max_page 参数，则表示可以一直翻页
	        if( maxPageSrc.length() < 1 ){

		        //生成智库列表任务
		        if( usernames.size()>0 ){
			        try {
				        int next = page + 1;

				        //设置参数
				        Map<String, Object> init_map = new HashMap<>();
				        init_map.put("page", String.valueOf(next));
				        init_map.put("max_page", "");

				        //生成holder
				        TaskHolder holder =  ChromeTaskFactory.getInstance().newHolder(ServiceScanTask.class, init_map);

				        //提交任务
				        ((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

			        } catch ( Exception e) {
				        logger.error("error for submit ServiceScanTask.class", e);
			        }

		        }
	        }
	        else {

		        int maxPage = Integer.valueOf(maxPageSrc);
		        int current_page = Integer.valueOf(t.getStringFromVars("page"));

		        // 从当前页翻至最大页
		        for(int i = current_page + 1; i <= maxPage; i++) {

			        Map<String, Object> init_map = new HashMap<>();
			        init_map.put("page", String.valueOf(i));
			        init_map.put("max_page", "0");

			        TaskHolder holder =  ChromeTaskFactory.getInstance().newHolder(t.getClass(), init_map);

			        ChromeDriverDistributor.getInstance().submit(holder);
		        }
	        }

        });
    }

    @Override
    public TaskTrace getTaskTrace() {

	    return new TaskTrace(this.getClass(), "all", this.getParamString("page"));
    }

}