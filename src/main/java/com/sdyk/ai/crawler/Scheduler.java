package com.sdyk.ai.crawler;

import com.google.gson.Gson;
import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.account.model.AccountImpl;
import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.model.CrawlerTaskParameter;
import com.sdyk.ai.crawler.model.Domain;
import com.sdyk.ai.crawler.model.WebDirverCount;
import com.sdyk.ai.crawler.proxy.exception.NoAvailableProxyException;
import com.sdyk.ai.crawler.proxy.model.ProxyImpl;
import com.sdyk.ai.crawler.proxy.AliyunHost;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import com.sdyk.ai.crawler.specific.itijuzi.task.CompanyListScanTask;
import com.sdyk.ai.crawler.task.LogTask;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import one.rewind.db.DaoManager;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.proxy.Proxy;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import one.rewind.io.requester.task.Task;
import one.rewind.util.Configs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CountDownLatch;

/**
 * Agent 初始化
 * Account 调用
 * Proxy 调用
 * 调度器
 */
public class Scheduler {

	public static final Logger logger = LogManager.getLogger(Scheduler.class.getName());

	static int DefaultDriverCount = 1;

	static {

	}

	public static Scheduler instance;

	/**
	 * 单例方法
	 * @return
	 */
	public static Scheduler getInstance() throws Exception {

		if (instance == null) {

			synchronized(Scheduler.class) {
				if (instance == null) {
					instance = new Scheduler();
				}
			}
		}

		return instance;
	}

	/**
	 *
	 * @throws Exception
	 */
	public Scheduler() throws Exception {

		resetDockerHost();

		AccountManager.getInstance().setAllAccountFree();

		resetProxy();

		init();
	}

	/**
	 * 初始化DockerHost
	 * 删除所有容器
	 * 创建对应数量的容器
	 * @throws Exception
	 */
	public void resetDockerHost() throws Exception {

		DockerHostManager.getInstance().delAllDockerContainers();
		DockerHostManager.getInstance().createDockerContainers(DefaultDriverCount);
	}

	/**
	 * 重置Proxy 并根据情况 新建AliyunHost
	 * @throws Exception
	 */
	public void resetProxy() throws Exception {

		ProxyManager.getInstance().setAllProxyFree();

		int currentFreeProxy = ProxyManager.getInstance().getValidProxyNum();

		if(currentFreeProxy < DefaultDriverCount + 2) {

			int proxyNumToCreate = DefaultDriverCount + 2 - currentFreeProxy;

			AliyunHost.batchBuild(proxyNumToCreate);

		}

		logger.info("Driver num:{}, proxy num:{}.", DefaultDriverCount, ProxyManager.getInstance().getValidProxyNum());
	}


	// 初始化方法
	public void init() throws Exception {

		ChromeDriverDistributor.instance = new Distributor();

		CountDownLatch downLatch = new CountDownLatch(DefaultDriverCount);

		for(int i=0; i<DefaultDriverCount; i++) {

			new Thread(()->{

				try {

					// A. Proxy
					ProxyImpl proxy = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);
					if(proxy == null) throw new Exception("No valid proxy.");

					// 设置代理的失败回调方法
					proxy.setFailedCallback(()->{

						if(proxy.source == ProxyImpl.Source.ALIYUN_HOST) {

							AliyunHost aliyunHost = null;

							try {

								// 删除AliyunHost主机
								aliyunHost = AliyunHost.getByHost(proxy.host);
								aliyunHost.stopAndDelete();

								// 删除数据库
								ProxyManager.getInstance().deleteProxyById(proxy.id);

							} catch (Exception e) {
								logger.error("AliyunHost:{} Error, ", proxy.host, e);
							}

						}
					});

					// B. Container
					ChromeDriverDockerContainer container = DockerHostManager.getInstance().getFreeContainer();
					final URL remoteAddress = container.getRemoteAddress();

					// C. 创建 Agent
					ChromeDriverAgent agent = new ChromeDriverAgent(/*remoteAddress, container, proxy*/);

					// Agent 账号异常回调
					agent.addAccountFailedCallback((agent_ ,account_, task_)->{

						// 设置账户状态
						account_.status = Account.Status.Broken;

						// 更新账户状态
						account_.update();

						// C1. 判断是否还有可用账号
						Account account_new = AccountManager.getInstance().getAccountByDomain(task_.getDomain());

						// C2. 有特定domain的账号
						if( account_new != null ){

							// C2. 判断是否有 没有加载domain 的Agent
							ChromeDriverAgent chromeDriverAgent = Distributor.getInstance().findAgentWithoutDomain(task_.getDomain());

							// 3. 如果2有，添加登陆任务
							// TODO
							if( chromeDriverAgent != null ){

								Distributor.getInstance().submitLoginTask(chromeDriverAgent, getLoginTaskByDomain(account_));

							}
							// 4. 如果2没有，新建一个ChromeDriverAgent
							else {

								DockerHostManager.getInstance().createDockerContainers(1);

								ChromeDriverDockerContainer container_new = DockerHostManager.getInstance().getFreeContainer();

								ProxyImpl proxy_new = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);

								ChromeDriverAgent agent_new = new ChromeDriverAgent(
										container_new.getRemoteAddress(), container_new, proxy_new);


								// TODO 循环调用
								agent_new.submit(getLoginTaskByDomain(account_new));

								ChromeDriverDistributor.getInstance().addAgent(agent_new);

							}

						}

					}).addProxyFailedCallback((agent_, p, t)->{

						// 代理异常回掉
						logger.info("Proxy {}:{} failed.", p.host, p.port);

						// 增加封禁的 domain
						ProxyManager.getInstance().addProxyBannedRecord(p, t.getDomain());

						try {

							if(!proxy.validate()) {

								p.failed();
								// 给agent 换 proxy
								// 如果 agent 加载的 domain account 需要重新登陆 会继续报账户异常
								// 不在当前分支处理
								Proxy proxy_new = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);
								agent_.changeProxy(proxy_new);

								return;
							}

							if(ProxyManager.getInstance().isProxyBannedByAllDomain(proxy)) {

								p.failed();
								// agent 只加 t.domain
								// 换 proxy
								Proxy proxy_new = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);
								agent_.changeProxy(proxy_new);

								return;
							}
							else {
								// t.domain 的 account 找 其他 agent (agent.proxy 没有被 t.domain封禁) 登陆
								// 原agent 删除 加载 对应 account 的相关信息

								// 如果没找到agent 创建新agent(container / proxy / agent)
								// t.domain 的 account 登陆

								Account account = agent_.accounts.get(t.getDomain());
								agent_.accounts.remove(t.getDomain());

								ChromeDriverAgent agent_new =
										((Distributor) ChromeDriverDistributor.getInstance()).findAgentWithoutDomain(t.getDomain());

								if(agent_new != null) {

									((Distributor) ChromeDriverDistributor.getInstance()).submitLoginTask(agent_new, getLoginTask(account));

								}
								else {

									DockerHostManager.getInstance().createDockerContainers(1);

									ChromeDriverDockerContainer container_new = DockerHostManager.getInstance().getFreeContainer();

									ProxyImpl proxy_new = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);

									agent_new = new ChromeDriverAgent(
											container_new.getRemoteAddress(), container_new, proxy_new);


									// TODO 循环调用
									agent_new.submit(getLoginTaskByDomain(account));

									ChromeDriverDistributor.getInstance().addAgent(agent_new);

								}

							}

							// 对代理异常进行处理
							//dealWithProxyFailed(agent_, p, t);

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

					});

					// todo 添加 agent
					ChromeDriverDistributor.getInstance().addAgent(agent);

					downLatch.countDown();

				} catch (Exception e) {
					e.printStackTrace();
				}

			}).start();
		}

		downLatch.await();

		// 为Agent添加多个登陆任务
		Domain.getAll().stream()
			.map(d -> d.domain)
			.forEach(d -> {
				for(ChromeDriverAgent agent : ChromeDriverDistributor.getInstance().queues.keySet()) {

					try {
						Account account = AccountManager.getInstance().getAccountByDomain(d);

						if(account != null) {
							ChromeTask loginTask = getLoginTask(account);
							((Distributor) ChromeDriverDistributor.getInstance()).submitLoginTask(agent, loginTask);
						} else {
							return;
						}

					} catch (Exception e) {
						logger.info("Error get {}:account. ", d);
					}
				}
			});

		logger.info("ChromeDriverAgents are ready.");
	}

	/**
	 * TODO
	 * @param account
	 * @return
	 */
	public ChromeTask getLoginTask(Account account) throws MalformedURLException, URISyntaxException {
		return getLoginTaskByDomain(account);
	}

	/**
	 * 通过 domain 生成登陆任务
	 * @param account
	 * @return
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public ChromeTask getLoginTaskByDomain(Account account) throws MalformedURLException, URISyntaxException {

		String domain = account.domain;

		Config base = ConfigFactory.load();
		InputStream stream = Configs.class.getClassLoader().getResourceAsStream("conf/LogPath.conf");
		Config config = ConfigFactory.parseReader(new InputStreamReader(stream)).withFallback(base);

		String url = config.getConfig(domain).getString("url");

		ChromeTask logTask = new ChromeTask(url);
		logTask.setPriority(Task.Priority.HIGH);
		logTask.addAction(LogTask.getLoginActionByDomain(domain, account));

		return logTask;
	}

	/**
	 * 主方法
	 */
	public static void main(String[] args) throws Exception {

		// 初始化
		Scheduler scheduler = new Scheduler();

		Thread.sleep(30000);


		// 执行抓取任务
		Map<String, Object> init_map = new HashMap<>();
		init_map.put("page", 1);

		Class clazz = Class.forName("com.sdyk.ai.crawler.specific.itijuzi.task.CompanyListScanTask");

		ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map );

		ChromeDriverDistributor.getInstance().submit(holder);




	}
}
