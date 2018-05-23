package com.sdyk.ai.crawler.requester.test;

import com.sdyk.ai.crawler.Requester;
import com.sdyk.ai.crawler.Scheduler;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.account.model.AccountImpl;
import com.sdyk.ai.crawler.proxy.AliyunHost;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import com.sdyk.ai.crawler.proxy.exception.NoAvailableProxyException;
import com.sdyk.ai.crawler.proxy.model.ProxyImpl;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.ScanTask;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.ServiceScanTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import org.apache.logging.log4j.LogManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 单机完整测试代理更换（无docker环境下）
 */
public class SchedulerProxyFailedTest {

	private static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Scheduler.class.getName());

	public static int num = 1;

	protected static SchedulerProxyFailedTest instance;

	public static SchedulerProxyFailedTest getInstance() {

		if (instance == null) {

			synchronized (SchedulerProxyFailedTest.class) {
				if (instance == null) {
					instance = new SchedulerProxyFailedTest();
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
			"t-yxtg"
	};

	// 服务商频道参数
	public static String[] service_supplier_channels = {
			/*"pxfw",
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
			"yxtg"*/
	};

	/**
	 * TODO 初始化ChromeDriverAgent
	 * 增加Exception Callbacks
	 */
	public SchedulerProxyFailedTest() {
		init();
	}

	// 初始化方法
	public void init() {

		Requester.URL_VISITS.clear();

		String domain = "zbj.com";
		int driverCount = 1;

		try {

			logger.info("Replace ChromeDriverRequester with {}.", Requester.class.getName());
			ChromeDriverRequester.instance = new Requester();
			ChromeDriverRequester.requester_executor.submit(ChromeDriverRequester.instance);

			// 创建阿里云host
			//AliyunHost.batchBuild(driverCount);

			// 删除所有docker container
			//DockerHostManager.getInstance().delAllDockerContainers();

			// 创建 container
			//DockerHostManager.getInstance().createDockerContainers(driverCount);

			// 读取全部有效账户 N个
			List<AccountImpl> accounts = AccountManager.getAccountByDomain(domain, driverCount);

			CountDownLatch latch = new CountDownLatch(accounts.size());

			// 创建N+2个有效代理，并保存到数据库中
			for(AccountImpl account : accounts) {

				Thread thread = new Thread(() -> {

					try {

						ProxyImpl proxy = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);

						if(proxy != null) {

							proxy.setFailedCallback(()->{

								if(proxy.source == ProxyImpl.Source.ALIYUN_HOST) {

									AliyunHost aliyunHost = null;

									try {

										aliyunHost = AliyunHost.getByHost(proxy.host);
										aliyunHost.stopAndDelete();

									} catch (Exception e) {
										logger.error("AliyunHost:{} Error, ", proxy.host, e);
									}

									if(ProxyManager.getInstance().getValidProxyNum() < driverCount + 2) {
										try {
											AliyunHost.batchBuild(2);
										} catch (InterruptedException e) {
											e.printStackTrace();
										}
									}
								}

								// TODO 删掉该Proxy记录
							});

							Task task = new Task("https://www.zbj.com");

							task.addAction(new LoginWithGeetestAction(account));

							//ChromeDriverDockerContainer container = DockerHostManager.getInstance().getFreeContainer();

							//ChromeDriverAgent agent = new ChromeDriverAgent(container.getRemoteAddress());

							ChromeDriverAgent agent = new ChromeDriverAgent(proxy);

							// agent 添加异常回调
							agent.addAccountFailedCallback(()->{

								logger.info("Account {}:{} failed.", account.domain, account.username);

							}).addProxyFailedCallback(()->{

								// 代理被禁
								logger.info("Proxy {}:{} failed.", proxy.host, proxy.port);

								try {

									agent.proxy.failed();

									// 获取一个新的代理服务器地址
									// TODO 需要设置callback
									ProxyImpl new_proxy = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);
									agent.changeProxy(new_proxy);

								}
								// 正常情况下不应该进入这个分支
								// 添加代理服务器操作 应该在Proxy 失败回调方法中执行
								catch (NoAvailableProxyException e) {
									logger.fatal("No Available Proxy, system exit.", e);
									System.exit(-1);
								}
								catch (Exception e) {
									logger.error(e);
								}

							}).addTerminatedCallback(()->{

								//logger.info("Container {} {}:{} Terminated.", container.name, container.ip, container.vncPort);

							}).addNewCallback(()->{

								try {
									agent.submit(task, 300000);
								} catch (Exception e) {
									logger.error(e);
								}

							});

							// agent.bmProxy.getClientBindAddress();
							ChromeDriverRequester.getInstance().addAgent(agent);

							agent.start();

							/*logger.info("ChromeDriverAgent remote address {}, local proxy {}:{}",
									agent.remoteAddress,
									agent.bmProxy.getClientBindAddress(), agent.bmProxy_port);*/
						}

						latch.countDown();
					}
					catch (Exception ex) {
						logger.error(ex);
					}
				});

				thread.start();
			}

			latch.await();

			logger.info("All ChromeDriverAgents are ready.");

		} catch (Exception e) {
			logger.error(e);
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

						task.setBuildDom();
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

		if (!args[1].equals("") && Integer.parseInt(args[1]) > 1) {
			num = Integer.parseInt(args[1]);
		}

		if (args.length == 2 && args[0].equals("H")) {
			// 获取历史数据
			logger.info("历史数据");
			SchedulerProxyFailedTest.getInstance().getHistoricalData();

		} else {
			// 监控数据
			logger.info("监控数据");
			SchedulerProxyFailedTest.getInstance().monitor();
		}
	}
}
