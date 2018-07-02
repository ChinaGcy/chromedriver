package com.sdyk.ai.crawler.specific.clouderwork.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ServiceProviderTask;
import com.sdyk.ai.crawler.util.URLUtil;
import one.rewind.io.requester.exception.ProxyException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashSet;
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
		url_template = "https://www.clouderwork.com/api/v2/freelancers/search?pagesize=10&pagenum={{page}}";
	}

    /**
     *
     * @param url
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    public ServiceScanTask (String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

        super(url);

        this.setPriority(Priority.HIGH);

        this.addDoneCallback((t) -> {

        	//获取当前页数
	        int page = 0;
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
		            URLUtil.PostTask(ServiceProviderTask.class,
				            null,
				            ImmutableMap.of("servicer_id", user),
				            null,
				            null,
				            null,
				            null,
				            null);

	            } catch (Exception e) {
		            logger.error("error for URLUtil.PostTask ServiceProviderTask.class", e);
	            }
            }

            //生成智库列表任务
            if( usernames.size()>0 ){

            	URLUtil.PostTask(ProjectScanTask.class,
			            null,
			            ImmutableMap.of( "page", String.valueOf(++page)),
			            null,
			            null,
			            null,
			            null,
			            null);
            }

        });
    }

    @Override
    public TaskTrace getTaskTrace() {
        return null;
    }
}