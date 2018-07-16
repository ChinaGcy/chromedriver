package com.sdyk.ai.crawler;

import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.account.model.AccountImpl;
import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.proxy.AliyunHost;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import com.sdyk.ai.crawler.proxy.exception.NoAvailableProxyException;
import com.sdyk.ai.crawler.proxy.model.ProxyImpl;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class CrawlerManager {

	public static final Logger logger = LogManager.getLogger(Scheduler.class.getName());

	public int chromeDriverCount = 4;

	public int driverCount = 4;

	public String domain;

	public CrawlerManager() {

		new Thread(()->{ServiceWrapper.getInstance();}).start();

		init();
	}

	// 初始化方法
	public void init() {

		Distributor.URL_VISITS.clear();

		// TODO 根据情况使用
		try {
			//resetAccountAndProxy();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {

			// 替换Requester
			/*logger.info("Replace ChromeDriverRequester with {}.", Distributor.class.getName());

			ChromeDriverRequester.instance = new Distributor();
			ChromeDriverRequester.requester_executor.submit(ChromeDriverRequester.instance);*/

			// 创建阿里云host
			//AliyunHost.batchBuild(driverCount);

			// 删除所有docker container
			DockerHostManager.getInstance().delAllDockerContainers();

			// 创建 container
			DockerHostManager.getInstance().createDockerContainers(driverCount);

			// 读取有效账户 driverCount 个
			List<AccountImpl> accounts = AccountManager.getAccountByDomain(domain, driverCount);

			// 分别为每个账号创建容器 和 chromedriver对象
			/*CountDownLatch latch = new CountDownLatch(driverCount);*/

			for(AccountImpl account : accounts) {

				Thread thread = new Thread(() -> {

					try {

						// 获取有效代理
						ProxyImpl proxy = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);

						if(proxy != null) {

							// 设置代理的失败回调方法
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

							// 生成登录任务
							ChromeDriverDockerContainer container = DockerHostManager.getInstance().getFreeContainer();

							//ChromeDriverAgent agent = new ChromeDriverAgent(container.getRemoteAddress());
							ChromeDriverAgent agent = new ChromeDriverAgent(container.getRemoteAddress(), container/*, proxy*/);

							// agent 添加异常回调
							agent.addAccountFailedCallback((agent1 ,account1,task)->{

								logger.info("Account {}:{} failed.", account.domain, account.username);

							}).addProxyFailedCallback((agent1, proxy1,t)->{

								// 代理被禁
								logger.info("Proxy {}:{} failed.", proxy1.host, proxy1.port);

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

							}).addTerminatedCallback((agent1)->{

								logger.info("Container {} {}:{} Terminated.", container.name, container.ip, container.vncPort);

							}).addNewCallback((agent1)->{

								try {
									getLoginTask(agent, account);
								} catch (Exception e) {
									logger.error(e);
								}
							});

							ChromeDriverDistributor.getInstance().addAgent(agent);

							/*latch.countDown();*/
							logger.info("ChromeDriverAgent remote address {}, local proxy {}:{}",
									agent.remoteAddress,
									agent.bmProxy.getClientBindAddress(), agent.bmProxy_port);
						}

					}
					catch (Exception ex) {
						logger.error("", ex);
					}
				});

				thread.start();
			}


			/*latch.await();*/

			logger.info("ChromeDriverAgents are ready.");

		} catch (Exception e) {
			logger.error(e);
		}
	}

	private void getLoginTask(ChromeDriverAgent agent, AccountImpl account) {
	}
}
