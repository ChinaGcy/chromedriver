package com.sdyk.ai.crawler.specific.clouderwork.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ProjectTask;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.txt.URLUtil;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectScanTask extends ScanTask {

	static {
		// init_map_class
		init_map_class = ImmutableMap.of("page", String.class);
		// init_map_defaults
		init_map_defaults = ImmutableMap.of("page", "1");
		// url_template
		url_template = "https://www.clouderwork.com/api/v2/jobs/search?ts=pagesize=20&pagenum={{page}}";

	}

	public static String domain() {
		return "clouderwork";
	}


    //设置任务
    public ProjectScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setBuildDom();

        this.setPriority(Priority.HIGH);

        this.addDoneCallback((t) -> {

	        int page = 0;
	        Pattern pattern_url = Pattern.compile("ts=pagesize=20&pagenum=(?<page>.+?)");
	        Matcher matcher_url = pattern_url.matcher(url);
	        if (matcher_url.find()) {
		        page = Integer.parseInt(matcher_url.group("page"));
	        }

            String src = getResponse().getDoc().text();

            Pattern pattern = Pattern.compile("\"id\":\"(?<username>.{16}?)\",\"user_id\"");
            Matcher matcher = pattern.matcher(src);

            Set<String> usernames = new HashSet<>();

            while(matcher.find()) {
                try {
                    usernames.add(URLDecoder.decode(matcher.group("username"), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }

            for(String user : usernames){

	            try {
		            HttpTaskPoster.getInstance().submit(ProjectTask.class,
				            ImmutableMap.of("project_id", user));
	            } catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {

		            logger.error("error fro HttpTaskPoster.submit ProjectScanTask.class", e);
	            }

            }

            if( usernames.size()>0 ){

	            try {
		            HttpTaskPoster.getInstance().submit(ProjectScanTask.class,
				            ImmutableMap.of("page", String.valueOf(++page)));
	            } catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {

		            logger.error("error fro HttpTaskPoster.submit ProjectScanTask.class", e);
	            }

            }

        });

    }

    @Override
    public TaskTrace getTaskTrace() {
        return null;
    }

}