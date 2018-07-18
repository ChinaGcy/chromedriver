package com.sdyk.ai.crawler;

import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.model.Domain;
import com.sdyk.ai.crawler.proxy.exception.NoAvailableProxyException;
import com.sdyk.ai.crawler.proxy.model.ProxyImpl;
import com.sdyk.ai.crawler.proxy.AliyunHost;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import com.sdyk.ai.crawler.task.LoginTask;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.action.LoginAction;
import one.rewind.io.requester.proxy.Proxy;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import one.rewind.io.requester.task.Task;
import one.rewind.util.Configs;
import org.apache.commons.collections.map.HashedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
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

	public static Map<String, LoginTask> loginMap = new HashedMap();

	/**
	 * 获取所有登陆任务
	 */
	public static void initLoginTask() throws URISyntaxException, MalformedURLException, ClassNotFoundException {

		File file = new File("login_tasks");
		File[] tempList = file.listFiles();

		for( File f : tempList ){

			loginMap.put(
					f.getName().replace(".json",""),
					LoginTask.buildFromJson(one.rewind.util.FileUtil.readFileByLines("login_tasks/" + f.getName())) );

		}

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

		initLoginTask();

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

	/**
	 * 启动一个Agent
	 * @param container
	 * @param proxy
	 */
	public ChromeDriverAgent addAgent(ChromeDriverDockerContainer container, Proxy proxy) throws Exception {

		final URL remoteAddress = container.getRemoteAddress();

		// 创建 Agent
		ChromeDriverAgent agent = new ChromeDriverAgent(/*remoteAddress, container, proxy*/);

		// A 账号异常回调
		agent.addAccountFailedCallback((agent_, account_)->{

			try {

				// 设置账户状态
				account_.status = Account.Status.Broken;

				// 更新账户状态
				account_.update();

				// A1. 判断是否还有可用账号
				Account account_new = AccountManager.getInstance().getAccountByDomain(account_.getDomain());

				// A1.1 有特定domain的账号
				if (account_new != null) {

					// A1.1.1 判断是否有 没有加载domain 的Agent
					ChromeDriverAgent chromeDriverAgent = Distributor.getInstance().findAgentWithoutDomain(account_.getDomain());

					// 如果有添加登陆任务
					if (chromeDriverAgent != null) {

						Distributor.getInstance().submitLoginTask(chromeDriverAgent, getLoginTask(account_));

					}
					// A1.1.2 如果没有，新建一个ChromeDriverAgent，并提交登陆任务
					else {

						DockerHostManager.getInstance().createDockerContainers(1);
						ChromeDriverDockerContainer container_new = DockerHostManager.getInstance().getFreeContainer();

						ProxyImpl proxy_new = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);

						((Distributor) ChromeDriverDistributor.getInstance()).submitLoginTask(
								addAgent(container_new, proxy), // 创建新agent
								getLoginTask(account_new) // 获取账号对应登陆任务
						);
					}
				}
			} catch (Exception e) {
				logger.error("Error execute account callback, {}.", account_, e);
			}

		})
		// B 代理异常毁掉
		.addProxyFailedCallback((agent_, p, t)->{

			logger.info("Proxy {}:{} failed.", p.host, p.port);

			// 增加封禁的 domain
			ProxyManager.getInstance().addProxyBannedRecord(p, t.getDomain());

			try {

				if(!p.validate()) {

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

				}
				else {
					// t.domain 的 account 找 其他 agent (agent.proxy 没有被 t.domain封禁) 登陆
					// 原agent 删除 加载 对应 account 的相关信息

					// 如果没找到agent 创建新agent(container / proxy / agent)
					// t.domain 的 account 登陆

					Account account = agent_.accounts.get(t.getDomain()); // TODO 有可能为null

					if(account != null) {

						agent_.accounts.remove(t.getDomain());

						ChromeDriverAgent agent_new =
								((Distributor) ChromeDriverDistributor.getInstance()).findAgentWithoutDomain(t.getDomain());

						if (agent_new != null) {

							((Distributor) ChromeDriverDistributor.getInstance()).submitLoginTask(agent_new, getLoginTask(account));

						} else {

							DockerHostManager.getInstance().createDockerContainers(1);

							ChromeDriverDockerContainer container_new = DockerHostManager.getInstance().getFreeContainer();

							ProxyImpl proxy_new = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);

							((Distributor) ChromeDriverDistributor.getInstance()).submitLoginTask(
									addAgent(container_new, proxy_new), // 创建新agent
									getLoginTask(account) // 获取账号对应登陆任务
							);

						}
					}
					else {

						ChromeDriverAgent agent_new =
								((Distributor) ChromeDriverDistributor.getInstance()).findAgentWithoutDomain(t.getDomain());

						if(agent_new == null) {

							DockerHostManager.getInstance().createDockerContainers(1);

							ChromeDriverDockerContainer container_new = DockerHostManager.getInstance().getFreeContainer();

							ProxyImpl proxy_new = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);

							addAgent(container_new, proxy_new);

						}
					}

				}

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

		return agent;
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

					addAgent(container, proxy);

					downLatch.countDown();

				} catch (Exception e) {
					e.printStackTrace();
				}

			}).start();
		}

		downLatch.await();

		// 为Agent添加多个 domain 登陆任务
		Domain.getAll().stream()
			.map(d -> d.domain)
			.forEach(d -> {
				for(ChromeDriverAgent agent : ChromeDriverDistributor.getInstance().queues.keySet()) {

					Account account = null;

					try {

						account = AccountManager.getInstance().getAccountByDomain(d);



						if(account != null) {

							LoginTask loginTask = loginMap.get(d);

							((LoginAction)loginTask.getActions().get(loginTask.getActions().size()-1)).setAccount(account);

							((Distributor) ChromeDriverDistributor.getInstance()).submitLoginTask(agent, loginTask);
						} else {
							return;
						}

					} catch (Exception e) {
						logger.info("Error get {}:{}. ", d, account, e);
					}
				}
			});



		logger.info("ChromeDriverAgents are ready.");
	}

	/**
	 * 生成 account 登陆任务
	 * @param account
	 * @return
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public ChromeTask getLoginTask(Account account) throws MalformedURLException, URISyntaxException {

		String domain = account.domain;

		LoginTask loginTask = loginMap.get(domain);

		((LoginAction)loginTask.getActions().get(loginTask.getActions().size()-1)).setAccount(account);

		loginTask.setPriority(Task.Priority.HIGH);

		return loginTask;
	}

	/**
	 * 主方法
	 */
	public static void main(String[] args) throws Exception {

		// 初始化
		Scheduler scheduler = new Scheduler();

		// 执行抓取任务
		Map<String, Object> init_map = new HashMap<>();
		init_map.put("page", "1");

		Class clazz = Class.forName("com.sdyk.ai.crawler.specific.itijuzi.task.CompanyListScanTask");

		ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map );

		((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

		Thread.sleep(60000);

		Map<String, Object> init_map_ = new HashMap<>();
		init_map.put("page", "1");

		Class clazz_ = Class.forName("com.sdyk.ai.crawler.specific.itijuzi.task.CompanyListScanTask");

		ChromeTaskHolder holder_ = ChromeTask.buildHolder(clazz_, init_map_ );

		((Distributor)ChromeDriverDistributor.getInstance()).submit(holder_);



	}
}
