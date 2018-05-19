package com.sdyk.ai.crawler.main.test;

import com.sdyk.ai.crawler.zbj.Requester;
import com.sdyk.ai.crawler.zbj.Scheduler;
import com.sdyk.ai.crawler.zbj.account.AccountManager;
import com.sdyk.ai.crawler.zbj.account.model.AccountImpl;
import com.sdyk.ai.crawler.zbj.docker.DockerHostManager;
import com.sdyk.ai.crawler.zbj.docker.model.DockerHostImpl;
import com.sdyk.ai.crawler.zbj.proxy.AliyunHost;
import com.sdyk.ai.crawler.zbj.proxy.ProxyManager;
import com.sdyk.ai.crawler.zbj.proxy.exception.NoAvailableProxyException;
import com.sdyk.ai.crawler.zbj.proxy.model.ProxyImpl;
import com.sdyk.ai.crawler.zbj.task.Task;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import org.apache.logging.log4j.LogManager;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class SchedulerTest {

	@Test
	public void test() throws Exception {

		DockerHostImpl host = DockerHostManager.getInstance().getHostByIp("10.0.0.62");
		DockerHostManager.getInstance().delAllDockerContainers(host);

		ProxyManager.getInstance().deleteProxyByGroup(AliyunHost.Proxy_Group_Name);

		AliyunHost.stopAndDeleteAll();

		Scheduler scheduler = new Scheduler();
		Thread.sleep(6000000);

	}

	@Test
	public void scheduleTest() {
		Scheduler.getInstance();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void scheduleProxyTest() {
		org.apache.logging.log4j.Logger logger = LogManager.getLogger(Scheduler.class.getName());

		Requester.URL_VISITS.clear();

		String domain = "zbj.com";
		int driverCount = 1;

		try {

			logger.info("Replace ChromeDriverRequester with {}.", Requester.class.getName());
			ChromeDriverRequester.instance = new Requester();
			ChromeDriverRequester.requester_executor.submit(ChromeDriverRequester.instance);

			// 创建阿里云host
			//AliyunHost.batchBuild(driverCount + 2);

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
							//ChromeDriverAgent agent = new ChromeDriverAgent(container.getRemoteAddress(), container, proxy);

							ChromeDriverAgent agent = new ChromeDriverAgent();
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

							logger.info("ChromeDriverAgent remote address {}, local proxy {}:{}",
									agent.remoteAddress,
									agent.bmProxy.getClientBindAddress(), agent.bmProxy_port);
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
}
