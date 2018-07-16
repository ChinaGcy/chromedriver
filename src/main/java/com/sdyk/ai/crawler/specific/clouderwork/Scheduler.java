package com.sdyk.ai.crawler.specific.clouderwork;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.specific.clouderwork.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.specific.clouderwork.task.scanTask.ServiceScanTask;
import com.sdyk.ai.crawler.specific.clouderwork.util.ClouderworkLoginAction;
import com.sdyk.ai.crawler.specific.itijuzi.task.CompanyListScanTask;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

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
    public void getLoginTask(ChromeDriverAgent agent, Account account) throws MalformedURLException, URISyntaxException, ChromeDriverException.IllegalStatusException, InterruptedException {

	    try {
		    agent.submit(new ChromeTask("https://passport.clouderwork.com/signin").addAction(new ClouderworkLoginAction(account)));
	    } catch (Exception e) {
		    e.printStackTrace();
	    }

    }

    /**
     * @param backtrace
     * @return
     */
    public void getTask(boolean backtrace) {


	    try {

		    //设置参数
		    Map<String, Object> init_map = new HashMap<>();
		    init_map.put("page", "1");

		    ProjectScanTask projectScanTask = new ProjectScanTask("https://www.clouderwork.com/api/v2/jobs/search?ts=pagesize=20&pagenum=1");

		    //生成holder
		    ChromeTaskHolder holder = ChromeTask.buildHolder(ProjectScanTask.class, init_map);

		    //提交任务
		    ChromeDriverDistributor.getInstance().submit(holder);

	    } catch ( Exception e) {

	    	logger.error("error fro HttpTaskPoster.submit ProjectScanTask.class", e);
	    }

	    /*try {
		    HttpTaskPoster.getInstance().submit(ServiceScanTask.class,
				    ImmutableMap.of("page", "1"));
	    } catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {

		    logger.error("error fro HttpTaskPoster.submit ProjectScanTask.class", e);
	    }*/

    }

    /**
     * 获取历史数据
     */
    public void getHistoricalData() {

	    // 需求
	    getTask(true);

    }

    /**
     * 监控调度
     */
    public void monitoring() {

	    getTask(false);

    }

	public static void main(String[] args) {

		int num = 1;

		Scheduler scheduler = new Scheduler("clouderwork", num);

		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		scheduler.getHistoricalData();

    //    scheduler.monitoring();
	}
}
