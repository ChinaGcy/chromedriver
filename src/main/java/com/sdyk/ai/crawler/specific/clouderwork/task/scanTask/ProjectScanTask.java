package com.sdyk.ai.crawler.specific.clouderwork.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.WorkTask;
import net.bytebuddy.implementation.bytecode.Throw;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.ChromeTaskScheduler;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import one.rewind.io.requester.task.ScheduledChromeTask;
import one.rewind.io.requester.task.Task;
import one.rewind.txt.URLUtil;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectScanTask extends ScanTask {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				ProjectScanTask.class,
				"https://www.clouderwork.com/api/v2/jobs/search?pagesize=20&pagenum={{page}}",
				ImmutableMap.of("page", String.class, "max_page", String.class),
				ImmutableMap.of("page", "1", "max_page", "3")
		);
	}


    //设置任务
    public ProjectScanTask(String url) throws Exception{

		super(url);

		this.setParam("page", url.split("pagenum=")[1]);

        this.setPriority(Priority.HIGH);

	    this.setNoFetchImages();

        this.setValidator((a, t) -> {

		    String text = t.getResponse().getText();

		    //代理出错
		    if ( text.contains("check proxy address") ) {
			    throw new ProxyException.Failed(a.proxy);
		    }
		    // 账号出错
		    else if( text.contains( "账号异常" ) || text.contains( "登陆异常" ) || text.contains( "重新登陆" ) ) {
			    throw new AccountException.Failed(a.accounts.get(t.getDomain()));
		    }


	    });

	    this.addDoneCallback((t) -> {

	        int page = Integer.valueOf(url.split("pagenum=")[1]);

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

		            //设置参数
		            Map<String, Object> init_map = new HashMap<>();
		            init_map.put("project_id", user);

		            Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.ProjectTask");

		            //生成holder
		            ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

		            //提交任务
		            ((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

	            } catch ( Exception e) {

		            logger.error("error for submit ProjectTask.class", e);
	            }

            }

            String maxPageSrc =  String.valueOf(((ChromeTask) t).init_map.get("max_page"));

            // 不含 max_page 参数，则表示可以一直翻页
            if( maxPageSrc.length() < 1 ){

	            if( usernames.size()>0 ){

		            try {

			            int next = page + 1;

			            //设置参数
			            Map<String, Object> init_map = new HashMap<>();
			            init_map.put("page", String.valueOf(next));
			            init_map.put("max_page", "");

			            //生成holder
			            ChromeTaskHolder holder = ChromeTask.buildHolder(ProjectScanTask.class, init_map);

			            //提交任务
			            ((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

		            } catch ( Exception e) {

			            logger.error("error for submit ProjectScanTask.class", e);
		            }

	            }
            }
            // 含有 max_page 参数，若max_page小于当前页则不进行翻页
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

    @Override
    public TaskTrace getTaskTrace() {

		return new TaskTrace(this.getClass(), "all", this.getParamString("page"));
    }

}