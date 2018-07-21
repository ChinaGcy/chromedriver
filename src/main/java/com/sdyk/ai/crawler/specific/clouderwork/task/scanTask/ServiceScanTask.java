package com.sdyk.ai.crawler.specific.clouderwork.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ServiceProviderTask;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;

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
				"https://www.clouderwork.com/api/v2/freelancers/search?pagesize=10&pagenum={{page}}",
				ImmutableMap.of("page", String.class),
				ImmutableMap.of("page", "")
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

        setBuildDom();

        this.setPriority(Priority.HIGH);

        this.setParam("page", url.split("pagenum=")[1]);

        this.addDoneCallback((t) -> {

        	//获取当前页数
	        int page = Integer.valueOf(url.split("pagenum=")[1]);
	        Pattern pattern_url = Pattern.compile("pagesize=10&pagenum=(?<page>.+?)");
	        Matcher matcher_url = pattern_url.matcher(url);
	        if (matcher_url.find()) {
		        page = Integer.parseInt(matcher_url.group("page"));
	        }

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
		            ImmutableMap.of("servicer_id", user);

		            Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ServiceProviderTask");

		            //生成holder
		            ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

		            //提交任务
		            ChromeDriverDistributor.getInstance().submit(holder);

	            } catch ( Exception e) {

		            logger.error("error for submit WorkTask.class", e);
	            }

            }

            //生成智库列表任务
            if( usernames.size()>0 ){

	            try {

	            	int next = page + 1;

		            //设置参数
		            Map<String, Object> init_map = new HashMap<>();
		            ImmutableMap.of("page", String.valueOf(next));

		            //生成holder
		            ChromeTaskHolder holder = ChromeTask.buildHolder(ServiceScanTask.class, init_map);

		            //提交任务
		            ChromeDriverDistributor.getInstance().submit(holder);

	            } catch ( Exception e) {

		            logger.error("error for submit ServiceScanTask.class", e);
	            }

            }

        });
    }

    @Override
    public TaskTrace getTaskTrace() {

	    return new TaskTrace(this.getClass(), "all", this.getParamString("page"));
    }

}