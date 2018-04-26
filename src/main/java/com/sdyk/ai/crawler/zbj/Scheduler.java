package com.sdyk.ai.crawler.zbj;

import com.sdyk.ai.crawler.zbj.account.AccountManager;
import com.sdyk.ai.crawler.zbj.docker.DockerHostManager;
import com.sdyk.ai.crawler.zbj.account.model.AccountImpl;
import com.sdyk.ai.crawler.zbj.docker.model.DockerContainer;
import com.sdyk.ai.crawler.zbj.proxy.model.ProxyImpl;
import com.sdyk.ai.crawler.zbj.proxy.AliyunHost;
import com.sdyk.ai.crawler.zbj.proxy.ProxyManager;
import com.sdyk.ai.crawler.zbj.task.Task;
import com.sdyk.ai.crawler.zbj.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.zbj.task.scanTask.ScanTask;
import com.sdyk.ai.crawler.zbj.task.scanTask.ServiceScanTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;

import static spark.route.HttpMethod.get;

/**
 * 任务生成器
 */
public class Scheduler {

	private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Scheduler.class.getName());


	protected static Scheduler instance;

	public static Scheduler getInstance() {

		if (instance == null) {

			synchronized (Scheduler.class) {
				if (instance == null) {
					instance = new Scheduler();
				}
			}
		}

		return instance;
	}

	// 项目频道参数
	public String[] project_channels = {
			"t-pxfw",
			"t-consult",
			"t-paperwork",
			"t-ppsj",
			"t-sign",
			"t-ad",
			"t-dhsjzbj",
			"t-video",
			"t-xcpzzzbj",
			"t-uisheji",
			"t-rjkf",
			"t-ydyykf",
			"t-wzkf",
			"t-vrthreed",
			"t-hkaifa",
			"t-zhjjfazbjzbj",
			"t-wxptkf",
			"t-dianlu",
			"t-xxtg",
			"t-yxtg",

	};

	// 服务商频道参数
	public static String[] service_supplier_channels = {
			"pxfw",
			"consult",
			"paperwork",
			"ppsj",
			"sign",
			"ad",
			"dhsjzbj",
			"video",
			"xcpzzzbj",
			"uisheji",
			"rjkf",
			"ydyykf",
			"wzkf",
			"vrthreed",
			"hkaifa",
			"zhjjfazbjzbj",
			"wxptkf",
			"dianlu",
			"xxtg",
			"yxtg",
	};

	/**
	 * TODO 初始化ChromeDriverAgent
	 * 增加Exception Callbacks
	 */
	public Scheduler() {

		String domain = "zbj.com";
		int driverCount = 1;

		try {

			// 创建阿里云host
			//  AliyunHost.batchBuild(driverCount + 2);

			// 删除所有docker container
			DockerHostManager.getInstance().delAllDockerContainers();


			// 创建 container
			DockerHostManager.getInstance().createDockerContainers(driverCount);
			// 读取全部有效账户 N个
			List<AccountImpl> accounts = AccountManager.getAccountByDomain(domain, driverCount);

			// 创建N+2个有效代理，并保存到数据库中
			for(AccountImpl account : accounts) {

				ProxyImpl proxy = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);

				if(proxy != null) {

					proxy.setFailedCallback(()->{

						if(proxy.source == ProxyImpl.Source.ALIYUN_HOST) {

							AliyunHost aliyunHost = null;

							try {
								aliyunHost = AliyunHost.getByHost(proxy.host);
							} catch (Exception e) {
								e.printStackTrace();
							}

							if(aliyunHost != null) {
								aliyunHost.stop();
							}

							// TODO 删掉该Proxy记录

							if(ProxyManager.getInstance().getValidProxyNum() < driverCount + 2) {
								try {
									AliyunHost.batchBuild(1);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						}
					});

					one.rewind.io.requester.Task task = new one.rewind.io.requester.Task("https://www.zbj.com");

					task.addAction(new LoginWithGeetestAction(account));

					DockerContainer container = DockerHostManager.getInstance().getFreeContainer();


					ChromeDriverAgent agent = new ChromeDriverAgent(container.getRemoteAddress(), proxy);

					// agent 添加异常回调
					agent.addAccountFailedCallback(()->{
						logger.info("Account {}:{} failed.", account.domain, account.username);
					}).addProxyFailedCallback(()->{
						logger.info("Proxy {}:{} failed.", proxy.host, proxy.port);
					}).addTerminatedCallback(()->{
						logger.info("Container {} {}:{} failed.", container.name, container.ip, container.vncPort);
					}).addNewCallback(()->{
						try {
							agent.submit(task);
						} catch (ChromeDriverException.IllegalStatusException e) {
							e.printStackTrace();
						}
					});

					// agent.bmProxy.getClientBindAddress();

					ChromeDriverRequester.getInstance().addAgent(agent);

					agent.start();

					logger.info("Local Proxy {}:{}", agent.bmProxy.getClientBindAddress(), agent.bmProxy_port);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param backtrace
	 * @return
	 */
	public List<ScanTask> getTask(boolean backtrace) {

		List<ScanTask> tasks = new ArrayList<>();

		for(String channel : project_channels) {
			ScanTask t = ProjectScanTask.generateTask(channel, 1);
			t.backtrace = backtrace;
			tasks.add(t);
			System.out.println("PROJECT:" + channel);
		}

		if (backtrace == true) {
			for (String channel : service_supplier_channels) {
				ScanTask t = ServiceScanTask.generateTask(channel, 1);
				t.backtrace = backtrace;
				tasks.add(t);
				System.out.println("SERVICE:" + channel);
			}
		}

		return tasks;
	}

	/**
	 * 获取历史数据
	 */
	public void getHistoricalData() {

		// 需求
		for (Task task : getTask(true)) {
			ChromeDriverRequester.getInstance().submit(task);
		}
	}

	/**
	 * 监控调度
	 */
	public void monitor() {

		try {

			it.sauronsoftware.cron4j.Scheduler s = new it.sauronsoftware.cron4j.Scheduler();

			// 每隔十分钟，生成实时扫描任务
			s.schedule("*/10 * * * *", new Runnable() {

				public void run() {

					for (Task task : getTask(false)) {
						ChromeDriverRequester.getInstance().submit(task);
					}
				}
			});

			s.start();

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		Scheduler.getInstance();

		/*if (args.length == 1 && args[0].equals("H")){
			// 获取历史数据
			logger.info("历史数据");
			Scheduler.getInstance().getHistoricalData();

		}
		else {
			// 监控数据
			logger.info("监控数据");
			Scheduler.getInstance().monitor();
		}*/
	}

}
