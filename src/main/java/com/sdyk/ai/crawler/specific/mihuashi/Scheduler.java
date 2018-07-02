package com.sdyk.ai.crawler.specific.mihuashi;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.specific.mihuashi.task.MihuashiLoginTask;
import com.sdyk.ai.crawler.specific.mihuashi.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.specific.mihuashi.task.scanTask.ServiceScanTask;
import com.sdyk.ai.crawler.util.URLUtil;
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
    public void getLoginTask() throws MalformedURLException, URISyntaxException {

	    try {
		    URLUtil.PostTask(MihuashiLoginTask.class,
				    null,
				    ImmutableMap.of("domain", "mihuashi"),
				    null,
				    null,
				    null,
				    null,
				    null);
	    } catch (ClassNotFoundException e) {
		    logger.error("", e);
	    }

    }

    /**
     * @param backtrace
     * @return
     */
    @Override
    public void getTask(boolean backtrace) {

	    try {
		    URLUtil.PostTask(ProjectScanTask.class,
				    null,
				    ImmutableMap.of("page", "1", "zone_id", "1"),
				    null,
				    null,
				    null,
				    null,
				    null);

		    URLUtil.PostTask(ProjectScanTask.class,
				    null,
				    ImmutableMap.of("page", "1", "zone_id", "2"),
				    null,
				    null,
				    null,
				    null,
				    null);

	    } catch (ClassNotFoundException e) {
		    logger.error("URLUtil.PostTask", e);
	    }

	    if( backtrace == true ){
		    try {
			    URLUtil.PostTask(ServiceScanTask.class,
					    null,
					    ImmutableMap.of("page", "1"),
					    null,
					    null,
					    null,
					    null,
					    null);

		    } catch (ClassNotFoundException e) {
			    logger.error("URLUtil.PostTask", e);
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
