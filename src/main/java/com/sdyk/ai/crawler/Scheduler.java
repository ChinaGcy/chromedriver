package com.sdyk.ai.crawler;

import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.account.model.AccountImpl;
import com.sdyk.ai.crawler.proxy.exception.NoAvailableProxyException;
import com.sdyk.ai.crawler.proxy.model.ProxyImpl;
import com.sdyk.ai.crawler.proxy.AliyunHost;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import com.sdyk.ai.crawler.specific.zbj.AuthorizedRequester;
import com.sdyk.ai.crawler.specific.zbj.task.action.GetProjectContactAction;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.db.DaoManager;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.proxy.Proxy;
import org.apache.logging.log4j.LogManager;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
/**
 * 任务生成器
 */
public abstract class Scheduler {

	public static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Scheduler.class.getName());

	int driverCount = 1;

	public String domain;

	public Scheduler(String domain, int driverCount) {

		this.driverCount = driverCount;

		this.domain = domain;

		new Thread(()->{ServiceWrapper.getInstance();}).start();

		init();
	}

	/**
	 * 重置所有的账号和代理
	 * @throws Exception
	 */
	public void resetAccountAndProxy() throws Exception {

		List<ProxyImpl> proxies = DaoManager.getDao(ProxyImpl.class).queryForAll();
		for(ProxyImpl proxy : proxies) {
			proxy.status = Proxy.Status.Free;
			proxy.update();
		}

		List<AccountImpl> accounts = DaoManager.getDao(AccountImpl.class).queryForAll();
		for(AccountImpl account : accounts) {
			account.status = Account.Status.Free;
			account.update();
		}
	}

	// 初始化方法
	public void init() {

		Requester.URL_VISITS.clear();

		// TODO 根据情况使用
		try {
			resetAccountAndProxy();
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {

			// 替换Requester
			logger.info("Replace ChromeDriverRequester with {}.", Requester.class.getName());

			ChromeDriverRequester.instance = new Requester();
			ChromeDriverRequester.requester_executor.submit(ChromeDriverRequester.instance);

			// 创建阿里云host
			//AliyunHost.batchBuild(driverCount);

			// 删除所有docker container
			DockerHostManager.getInstance().delAllDockerContainers();

			// 创建 container
			DockerHostManager.getInstance().createDockerContainers(driverCount);

			// 读取有效账户 driverCount 个
			List<AccountImpl> accounts = AccountManager.getAccountByDomain(domain, driverCount);

			// 分别为每个账号创建容器 和 chromedriver对象
			CountDownLatch latch = new CountDownLatch(accounts.size());

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
							Task task = getLoginTask(account);

							ChromeDriverDockerContainer container = DockerHostManager.getInstance().getFreeContainer();

							//ChromeDriverAgent agent = new ChromeDriverAgent(container.getRemoteAddress());
							ChromeDriverAgent agent = new ChromeDriverAgent(container.getRemoteAddress(), container, proxy);

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

								logger.info("Container {} {}:{} Terminated.", container.name, container.ip, container.vncPort);

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

			logger.info("ChromeDriverAgents are ready.");

		} catch (Exception e) {
			logger.error(e);
		}
	}

	/**
	 *
	 */
	public void upateRedisUrlVisitQueue() {

	}

	/**
	 *
	 * @param account
	 * @return
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public abstract Task getLoginTask(Account account) throws MalformedURLException, URISyntaxException;

	/**
	 *
	 * @param backtrace
	 * @return
	 */
	public abstract List<Task> getTask(boolean backtrace);

	/**
	 * 获取历史数据
	 */
	public abstract void getHistoricalData();

	/**
	 * 监控调度
	 */
	public abstract void monitoring();
}
