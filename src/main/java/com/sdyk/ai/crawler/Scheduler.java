package com.sdyk.ai.crawler;

import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.model.Domain;
import com.sdyk.ai.crawler.model.TaskInitializer;
import com.sdyk.ai.crawler.proxy.exception.NoAvailableProxyException;
import com.sdyk.ai.crawler.proxy.model.ProxyImpl;
import com.sdyk.ai.crawler.proxy.AliyunHost;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import com.sdyk.ai.crawler.task.LoginTask;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.action.LoginAction;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.proxy.Proxy;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import one.rewind.io.requester.task.Task;
import org.apache.commons.collections.map.HashedMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import java.net.URL;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static com.sdyk.ai.crawler.Scheduler.flag.PerformLoginTasks;
import static one.rewind.util.FileUtil.readFileByLines;

/**
 * Agent 初始化
 * Account 调用
 * Proxy 调用
 * 调度器
 */
public class Scheduler {

	public static final Logger logger = LogManager.getLogger(Scheduler.class.getName());

	public static enum flag {
		PerformLoginTasks
	}

	public static List<flag> Flags = Arrays.asList(PerformLoginTasks);

	public static int DefaultDriverCount = 1;

	public static Map<String, LoginTask> loginTasks = new HashedMap();

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

		new Thread(() -> {
			ServiceWrapper.getInstance();
		}).start();

		resetDockerHost();

		AccountManager.getInstance().setAllAccountFree();

		resetProxy();

		resetRedis();

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
	 * 获取所有登陆任务
	 */
	public void initLoginTask() throws URISyntaxException, MalformedURLException, ClassNotFoundException, ProxyException.Failed {

		File file = new File("login_tasks");
		File[] tempList = file.listFiles();

		for( File f : tempList ){

			loginTasks.put(
					f.getName().replace(".json",""),
					LoginTask.buildFromJson(readFileByLines("login_tasks/" + f.getName())) );

		}
	}

	/**
	 * 重置Proxy 并根据情况 新建AliyunHost
	 * @throws Exception
	 */
	public void resetProxy() throws Exception {

		ProxyManager.getInstance().setAllProxyFree();

		int currentFreeProxy = ProxyManager.getInstance().getValidProxyNum();

		if(currentFreeProxy < DefaultDriverCount + 2 ) {

			int proxyNumToCreate = DefaultDriverCount + 2 - currentFreeProxy;

			AliyunHost.batchBuild(proxyNumToCreate);

		}

		logger.info("Driver num:{}, proxy num:{}.", DefaultDriverCount, ProxyManager.getInstance().getValidProxyNum());
	}

	/**
	 * 清空redis中记录的proxy被domain封禁信息以及采集网页的时间信息
	 */
	public void resetRedis() {

		ProxyManager.getInstance().proxyDomainBannedMap.clear();

		Distributor.URL_VISITS.clear();
	}

	/**
	 * 启动一个Agent
	 * @param container
	 * @param proxy
	 */
	public ChromeDriverAgent addAgent(ChromeDriverDockerContainer container, Proxy proxy, Account oldAccount, boolean needLogin) throws Exception {

		final URL remoteAddress = container.getRemoteAddress();

		// 创建 Agent
		ChromeDriverAgent agent = new ChromeDriverAgent(remoteAddress, container, proxy);

		// A 账号异常回调
		agent.addAccountFailedCallback((agent_, account_)->{

			try {

				// 设置账户状态
				account_.status = Account.Status.Broken;

				// 更新账户状态
				account_.update();

				// A1. 判断是否还有可用账号
				Account account_new = AccountManager.getInstance().getAccountByDomain(account_.getDomain());

				logger.info("have account_new : {}", account_new);

				// A1.1 有特定domain的账号
				if (account_new != null) {

					// A1.1.1 判断是否有 没有加载domain 的Agent
					ChromeDriverAgent chromeDriverAgent = ((Distributor)ChromeDriverDistributor.getInstance()).
							findAgentWithoutDomain(account_.getDomain());

					logger.info("have Agent : {} without domain : {} ",chromeDriverAgent, account_new.domain );

					// 如果有添加登陆任务
					if (chromeDriverAgent != null) {

						logger.info("submitLoginTask chromeDriverAgent : {}, LoginTask : {}", chromeDriverAgent, getLoginTask(account_));
						((Distributor)ChromeDriverDistributor.getInstance()).submitLoginTask(chromeDriverAgent, getLoginTask(account_));

					}
					// A1.1.2 如果没有，新建一个ChromeDriverAgent，并提交登陆任务
					else {

						DockerHostManager.getInstance().createDockerContainers(1);
						logger.info("createDockerContainers");

						ChromeDriverDockerContainer container_new = DockerHostManager.getInstance().getFreeContainer();

						ProxyImpl proxy_new = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);
						logger.info("getValidProxy : {}", proxy_new);

						((Distributor) ChromeDriverDistributor.getInstance()).submitLoginTask(
								addAgent(container_new, proxy, account_new, true), // 创建新agent
								getLoginTask(account_new) // 获取账号对应登陆任务
						);
						logger.info("submitLoginTask");
					}
				}
			} catch (Exception e) {
				logger.error("Error execute account callback, {}.", account_, e);
			}

		})
		// B 代理异常回调
		.addProxyFailedCallback((agent_, p, t)->{

			logger.info("Proxy {}:{} failed.", p.host, p.port);

			// 增加封禁的 domain
			ProxyManager.getInstance().addProxyBannedRecord(p, t.getDomain());

			try {

				if(!p.validate()) {

					logger.info("{} invalid.", p.getInfo());

					p.failed();
					// 给agent 换 proxy
					// 如果 agent 加载的 domain account 需要重新登陆 会继续报账户异常
					// 不在当前分支处理
					Proxy proxy_new = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);
					agent_.changeProxy(proxy_new);

					return;
				}

				if(ProxyManager.getInstance().isProxyBannedByAllDomain(p)) {

					logger.info("{} banned by all domain.", p.getInfo());

					p.failed();
					// agent 只加 t.domain
					// 换 proxy
					Proxy proxy_new = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);
					agent_.changeProxy(proxy_new);

					logger.info("agent change proxy : {}", proxy_new.getInfo());

				}
				else {

					logger.info("{} banned by {}.", t.getDomain());

					// t.domain 的 account 找 其他 agent (agent.proxy 没有被 t.domain封禁) 登陆
					// 原agent 删除 加载 对应 account 的相关信息

					// 如果没找到agent 创建新agent(container / proxy / agent)
					// t.domain 的 account 登陆

					Account account = agent_.accounts.get(t.getDomain()); // TODO 有可能为null

					logger.info("proxy with account : {}", account);

					// 有关联账号
					if(account != null) {

						// 清除 accounts 中的 domain
						agent_.accounts.remove(t.getDomain());

						// 清除 domain_agent_map 中的 agent_
						((Distributor)ChromeDriverDistributor.getInstance()).domain_agent_map.get(t.getDomain()).remove(agent_);

						ChromeDriverAgent agent_new =
								((Distributor) ChromeDriverDistributor.getInstance()).findAgentWithoutDomain(t.getDomain());

						logger.info("Agent wicount : {} and agent_new : {}",account, agent_new);

						if (agent_new != null) {

							logger.info("Agent Add LoginTask : {} without account : {} on agent_new : {}",getLoginTask(account), account, agent_new);

							((Distributor) ChromeDriverDistributor.getInstance()).submitLoginTask(agent_new, getLoginTask(account));

						} else {

							DockerHostManager.getInstance().createDockerContainers(1);

							ChromeDriverDockerContainer container_new = DockerHostManager.getInstance().getFreeContainer();

							ProxyImpl proxy_new = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);

							((Distributor) ChromeDriverDistributor.getInstance()).submitLoginTask(
									addAgent(container_new, proxy_new, account, true), // 创建新agent
									getLoginTask(account)  // 获取账号对应登陆任务
							);

							logger.info("Create New Agent with container ： {} and proxy : {}",container_new, proxy_new );
						}
					}
					// 无关联账号
					else {

						ChromeDriverAgent agent_new =
								((Distributor) ChromeDriverDistributor.getInstance()).findAgentWithoutDomain(t.getDomain());

						logger.info("Agent without account and agent_new : {}", agent_new);

						if(agent_new == null) {

							DockerHostManager.getInstance().createDockerContainers(1);

							ChromeDriverDockerContainer container_new = DockerHostManager.getInstance().getFreeContainer();

							ProxyImpl proxy_new = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);

							logger.info("addAgent container_new : {} , proxy_new : {}",container_new ,proxy_new);

							addAgent(container_new, proxy_new, null, false);

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

		// 预添加所有domain的登陆任务到agent
		if(Flags.contains(PerformLoginTasks) && needLogin)
			addPresetLoginTasksToAgent(agent, oldAccount);

		ChromeDriverDistributor.getInstance().addAgent(agent);

		return agent;
	}

	/**
	 *
	 * @param agent
	 */
	public void addPresetLoginTasksToAgent(ChromeDriverAgent agent, Account account_) {

		// 登陆所有网站
		if( account_ == null ){

			Domain.getAll().stream()
					.map(d -> d.domain)
					.forEach(d -> {

						Account account = null;

						try {

							account = AccountManager.getInstance().getAccountByDomain(d);

							if(account != null) {

								// 根据 domain 获取 LoginTask
								LoginTask loginTask = loginTasks.get(d);

								// 设定账户
								((LoginAction)loginTask.getActions().get(loginTask.getActions().size()-1)).setAccount(account);

								// 预设值登陆任务
								((Distributor) ChromeDriverDistributor.getInstance()).submitLoginTask(agent, loginTask);

							} else {
								return;
							}

						} catch (Exception e) {
							logger.info("Error get {}:{}. ", d, account, e);
						}

					});
		}
		// 只登陆单个网站
		else {

			// 根据 domain 获取 LoginTask
			LoginTask loginTask = loginTasks.get(account_.domain);

			// 设定账户
			((LoginAction)loginTask.getActions().get(loginTask.getActions().size()-1)).setAccount(account_);

			// 预设值登陆任务
			((Distributor) ChromeDriverDistributor.getInstance()).submitLoginTask(agent, loginTask);

		}


	}


	// 初始化方法
	public void init() throws Exception {

		//ChromeDriverDistributor.instance = new Distributor();

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
								logger.info("stopAndDeleteAliyunHost: {}", proxy.host);
								//aliyunHost = AliyunHost.getByHost(proxy.host);
								//aliyunHost.stopAndDelete();

								// 删除数据库
								logger.info("delete From Mysql.proxys: {}", proxy.host);
								//ProxyManager.getInstance().deleteProxyById(proxy.id);

							} catch (Exception e) {
								logger.error("AliyunHost:{} Error, ", proxy.host, e);
							}

						}
					});

					// B. Container
					ChromeDriverDockerContainer container = DockerHostManager.getInstance().getFreeContainer();

					addAgent(container, proxy, null, true);

					downLatch.countDown();

				} catch (Exception e) {
					e.printStackTrace();
				}

			}).start();
		}

		downLatch.await();

		// 为Agent添加多个 domain 登陆任务

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

		LoginTask loginTask = loginTasks.get(domain);

		((LoginAction)loginTask.getActions().get(loginTask.getActions().size()-1)).setAccount(account);

		loginTask.setPriority(Task.Priority.HIGH);

		return loginTask;
	}

	/**
	 * 主方法
	 */
	public static void main(String[] args) throws Exception {

		// Scheduler 初始化
		Scheduler.getInstance();

		Thread.sleep(10000);

		TaskInitializer.getAll().stream().filter(t -> {
			return t.enable == true;
		}).forEach( t ->{

			try {

				t.scheduled_task_id = HttpTaskPoster.getInstance().submit(t.class_name, t.init_map_json);

				t.start_time = new Date();

				t.update();

			} catch (Exception e) {
				e.printStackTrace();
			}

		});

	}
}
