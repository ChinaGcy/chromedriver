package com.sdyk.ai.crawler.specific.mihuashi;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.specific.mihuashi.action.MihuashiLoginAction;
import com.sdyk.ai.crawler.specific.mihuashi.task.MihuashiLoginTask;
import com.sdyk.ai.crawler.specific.mihuashi.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.specific.mihuashi.task.scanTask.ServiceScanTask;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.task.ChromeTask;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 *
 */
public class Scheduler extends com.sdyk.ai.crawler.Scheduler {

	public String cron = "*/30 * * * *";

	public Scheduler() {
		super();
	}

	public Scheduler(String domain, int driverCount) {
		super(domain, driverCount);
	}

    /**
     * @return
     * @throws MalformedURLException
     * @throws URISyntaxException
     */
    @Override
    public void getLoginTask(ChromeDriverAgent agent, Account account) throws MalformedURLException, URISyntaxException, ChromeDriverException.IllegalStatusException, InterruptedException {

	    agent.submit(new ChromeTask("https://www.mihuashi.com/login").addAction(new MihuashiLoginAction(account)));

    }

    /**
     * @param backtrace
     * @return
     */
    @Override
    public void getTask(boolean backtrace) {

	    try {
		    HttpTaskPoster.getInstance().submit(ProjectScanTask.class,
				    ImmutableMap.of("page", "1", "zone_id", "1"));

		    HttpTaskPoster.getInstance().submit(ProjectScanTask.class,
				    ImmutableMap.of("page", "1", "zone_id", "2"));

	    } catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {
		    logger.error("error for HttpTaskPoster.submit ProjectScanTask", e);
	    }


	    if( backtrace == true ){

		    try {
			    HttpTaskPoster.getInstance().submit(ServiceScanTask.class,
					    ImmutableMap.of("page", "1"));

		    } catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {
			    logger.error("error for HttpTaskPoster.submit ServiceScanTask", e);
		    }

	    }

    }

    /**
     * 获取历史数据
     */
    @Override
    public void getHistoricalData() {

	    // 需求
	    getTask(true);

    }

    /**
     * 监控调度
     */
    @Override
    public void monitoring() {

	    getTask(false);

    }

	public static void main(String[] args) {

		int num = 0;

		if (args.length >= 1 && !args[0].equals("") && Integer.parseInt(args[0]) > 1) {
			num = Integer.parseInt(args[0]);
		}

		Scheduler scheduler = new Scheduler("mihuashi", num);

		scheduler.getHistoricalData();

		scheduler.monitoring();
	}
}
