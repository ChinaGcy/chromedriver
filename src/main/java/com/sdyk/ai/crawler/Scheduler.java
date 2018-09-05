package com.sdyk.ai.crawler;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.exception.NoAvailableAccountException;
import com.sdyk.ai.crawler.exception.NoAvailableProxyException;
import com.sdyk.ai.crawler.model.Domain;
import com.sdyk.ai.crawler.model.LoginTaskWrapper;
import com.sdyk.ai.crawler.model.TaskInitializer;
import com.sdyk.ai.crawler.proxy.AliyunHost;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import com.sdyk.ai.crawler.proxy.model.ProxyImpl;
import com.sdyk.ai.crawler.task.LoginTask;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.HttpTaskSubmitter;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.action.LoginAction;
import one.rewind.io.requester.proxy.Proxy;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.server.Msg;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static com.sdyk.ai.crawler.Scheduler.flag.PerformLoginTasks;

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

	public static int DriverCount_ProxyAliyun = 2;

	public static int DriverCount_ProxyOwn = 3;

	// 定义启动agent个数
	public static int DefaultDriverCount = DriverCount_ProxyAliyun + DriverCount_ProxyOwn;

	// 定义每组proxy使用个数
	public static Map<String, Integer> DefaultDriverCount_ProxyGroup = ImmutableMap.of(
			AliyunHost.Proxy_Group_Name , DriverCount_ProxyAliyun ,
			"own", DriverCount_ProxyOwn
	);

	// 定义黑名单
	public static Map<String, List<String>> BLACK_DOMAIN_PROXYGROUP = ImmutableMap.of(
			AliyunHost.Proxy_Group_Name , Arrays.asList("tianyancha.com"),
			"own", new ArrayList<>()
	);

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

		// Setup Web API
		new Thread(() -> {
			ServiceWrapper.getInstance();
		}).start();

		// 重启 容器
		resetDockerHost();

		// 重置 账号
		AccountManager.getInstance().setAllAccountFree();

		// 重置 代理
		resetProxy();

		// 重置 缓存
		resetRedis();

		// 初始化
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

		if(currentFreeProxy < DriverCount_ProxyAliyun ) {

			int proxyNumToCreate = DriverCount_ProxyAliyun - currentFreeProxy;

			AliyunHost.batchBuild(proxyNumToCreate);

		}

		logger.info("Driver num:{}, proxy num:{}.", DriverCount_ProxyAliyun, ProxyManager.getInstance().getValidProxyNum());
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
	 * 不负责关初始账户登陆
	 *
	 * @param container
	 * @param proxy
	 * @return
	 * @throws Exception
	 */
	public ChromeDriverAgent addAgent(ChromeDriverDockerContainer container, Proxy proxy) throws Exception {

		final URL remoteAddress = container.getRemoteAddress();

		// 创建 Agent
		ChromeDriverAgent agent = new ChromeDriverAgent(remoteAddress, container, proxy);

		/**
		 * A 账号异常回调
 		 */
		agent.addAccountFailedCallback((agent_, account_)->{

			String domain = account_.getDomain();

			try {

				// 设置账户状态
				account_.status = Account.Status.Broken;

				// 更新账户状态
				account_.update();

				// 判断是否还有可用账号
				Account account_new = AccountManager.getInstance().getAccountByDomain(domain);

				// A1 关联domain 有可用account
				if (account_new != null) {

					logger.info("Get new account {}:{} --> {}.", domain, account_new, agent_.getInfo());

					((Distributor)ChromeDriverDistributor.getInstance()).submitLoginTask(
							agent_,
							getLoginTask(account_new) // 获取账号对应登陆任务
					);
				}
				// A2 关联domain 无可用account
				else {
					throw new NoAvailableAccountException();
				}

			} catch (Exception e) {
				logger.error("Error execute account callback, {}.", account_, e);
			}

		})
		/**
		 * B 代理异常回调
 		 */
		.addProxyFailedCallback((agent_, p, t)->{

			logger.info("Proxy {}:{} failed.", p.host, p.port);

			// B1 增加封禁记录 domain
			ProxyManager.getInstance().addProxyBannedRecord(p, t.getDomain());

			try {

				// B2 代理无效
				if(!p.validate()) {

					logger.info("{} invalid.", p.getInfo());

					// 给agent 换 proxy
					// 如果 agent 加载的 account 需要重新登陆 会继续报账户异常
					// 不在当前分支处理
					p.failed();
					Proxy proxy_new = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);
					agent_.changeProxy(proxy_new);

					logger.info("Agent: {} --> proxy : {}", agent.getInfo(), proxy_new.getInfo());

				}
				// B3 代理被所有domain封禁
				// agent 此时只能访问 t.domain
				else if(ProxyManager.getInstance().isProxyBannedByAllDomain(p)) {

					logger.info("{} banned by all domain.", p.getInfo());

					p.failed();
					Proxy proxy_new = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);
					agent_.changeProxy(proxy_new);

					logger.info("Agent: {} --> proxy : {}", agent.getInfo(), proxy_new.getInfo());

				}
				// B4 proxy 未被所有 domain 封禁
				else {

					// 1. 获取被封禁网站的已登陆账号
					Account account = agent_.accounts.get(t.getDomain());
					logger.info("Proxy: {} banned by {}, related account: {}", proxy.getInfo(), t.getDomain(), account);

					// 2.1 有关联账号
					if(account != null) {

						// 2.1.1 清除 accounts 中的 domain
						agent_.accounts.remove(t.getDomain());

						// 2.1.2 清除 domain_agent_map 中的 agent_
						((Distributor) ChromeDriverDistributor.getInstance()).domain_agent_map.get(t.getDomain()).remove(agent_);

						// 2.1.3 判断是否有可加载该网站的agent
						ChromeDriverAgent agent_available =
								((Distributor) ChromeDriverDistributor.getInstance()).findAgentCouldAccessDomain(t.getDomain());

						// 2.1.3.1 含有可加载该网站的agent, 提交登陆任务
						if (agent_available != null) {

							logger.info("Find Agent: {} for account: {}, submit login task.", agent_available, account);

							((Distributor)ChromeDriverDistributor.getInstance()).submitLoginTask(agent_available, getLoginTask(account));

						}
						// 2.1.3.2 不含有可加载该网站的 agent 则创建新的agent
						else {

							DockerHostManager.getInstance().createDockerContainers(1);

							ChromeDriverDockerContainer container_new = DockerHostManager.getInstance().getFreeContainer();

							ProxyImpl proxy_new = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);

							ChromeDriverAgent agent_new = addAgent(container_new, proxy_new);

							logger.info("Create new Agent: {}", agent_new.getInfo());

							((Distributor)ChromeDriverDistributor.getInstance()).submitLoginTask(
									agent_new,
									getLoginTask(account)  // 获取账号对应登陆任务
							);
						}
					}
					// 2.2 无关联账号
					else {

						// 2.2.1 获取可执行该网站任务的 agent,
						ChromeDriverAgent agent_available =
								((Distributor)ChromeDriverDistributor.getInstance()).findAgentCouldAccessDomain(t.getDomain());


						if(agent_available != null) {

							logger.info("Find Agent: {} can access: {}", agent_available.getInfo(), t.getDomain());

						}
						// 2.2.1.1 无可用于执行该网站的agent
						if(agent_available == null) {

							logger.info("No can access: {}", agent_available.getInfo(), t.getDomain());

							DockerHostManager.getInstance().createDockerContainers(1);

							ChromeDriverDockerContainer container_new = DockerHostManager.getInstance().getFreeContainer();

							ProxyImpl proxy_new = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);

							// 添加新的agent
							ChromeDriverAgent agent_new = addAgent(container_new, proxy_new);

							logger.info("Add new Agent: {}.", agent_new.getInfo());
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


		((Distributor) ChromeDriverDistributor.getInstance()).addAgent(agent);

		return agent;
	}

	/**
	 * 为agent添加登陆任务
	 * @param agent
	 */
	public void addPresetLoginTasksToAgent(ChromeDriverAgent agent) {

		// 1. 获取需要登陆的domain，
		Domain.getAll().stream().map(d -> d.domain).forEach(d -> {

			logger.info(d);
			// 2. 判断 agent 是否可加载该 domain 的 account
			if( !ProxyManager.getInstance().isProxyBannedByDomain(agent.proxy, d) ){

				// 3. 获取 account
				try {
					// 3.1 获取有特定功能的account
					Account account = AccountManager.getInstance().getAccountsByDomain(d, "selected");

					// 3.2 无特殊功能account 获取一般account
					if( account == null ){
						account = AccountManager.getInstance().getAccountByDomain(d);
					}

					logger.info(account.toJSON());
					// 3.3 为agent添加登陆任务
					if( account != null ){

						addLoginTaskToAgent(agent, account);
					}

				} catch (Exception e) {
					logger.error("error for get account");
				}
			}
		});
	}

	/**
	 * 为 agent 添加 一个账号的登陆任务
	 * @param agent
	 * @param account
	 */
	public void addLoginTaskToAgent(ChromeDriverAgent agent, Account account) {

		try {
			// 1. 从数据库反序列化登陆任务
			LoginTask loginTask = LoginTaskWrapper.getLoginTaskByDomain(account.getDomain());
			logger.info(loginTask.toJSON());

			// 2. 为登陆任务设置账户
			((LoginAction)loginTask.getActions().get(loginTask.getActions().size()-1)).setAccount(account);

			// 3. 提交登陆任务
			((Distributor)ChromeDriverDistributor.getInstance()).submitLoginTask(agent, loginTask);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 初始化方法
	 * @throws Exception
	 */
	public void init() throws Exception {

		CountDownLatch downLatch = new CountDownLatch(DefaultDriverCount);

		for( String s : DefaultDriverCount_ProxyGroup.keySet() ){

			for(int i=0; i<DefaultDriverCount_ProxyGroup.get(s); i++) {

				new Thread(()->{

					try {

						// A. Proxy
						ProxyImpl proxy = ProxyManager.getInstance().getValidProxy(s);
						if(proxy == null) throw new Exception("No valid proxy.");

						// 设置 agent_proxy封禁状态
						List<String> ProxyBannedList = BLACK_DOMAIN_PROXYGROUP.get(s);
						for( String ProxyBanneDomain : ProxyBannedList ){

							ProxyManager.getInstance().addProxyBannedRecord(proxy,ProxyBanneDomain);
						}

						// 设置代理的失败回调方法
						proxy.setFailedCallback(()->{

							if(proxy.source == ProxyImpl.Source.ALIYUN_HOST) {

								AliyunHost aliyunHost = null;

								try {

									// 删除AliyunHost主机
									logger.info("stopAndDeleteAliyunHost: {}", proxy.host);
									aliyunHost = AliyunHost.getByHost(proxy.host);
									aliyunHost.stopAndDelete();

									// 删除数据库
									logger.info("delete From Mysql.proxys: {}", proxy.host);
									ProxyManager.getInstance().deleteProxyById(proxy.id);

								} catch (Exception e) {
									logger.error("AliyunHost:{} Error, ", proxy.host, e);
								}

							}
						});

						// B. Container
						ChromeDriverDockerContainer container = DockerHostManager.getInstance().getFreeContainer();

						// C. Agent
						ChromeDriverAgent agent = addAgent(container, proxy);

						// D. 设置 agent 初始登陆操作
						if( Flags.size() > 0 ){
							addPresetLoginTasksToAgent(agent);
						}

						downLatch.countDown();

					} catch (Exception e) {
						e.printStackTrace();
					}

				}).start();
			}
		}

		downLatch.await();

		logger.info("ChromeDriverAgents initialized.");
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

		LoginTask loginTask = null;
		try {
			loginTask = LoginTaskWrapper.getLoginTaskByDomain(account.getDomain());
		} catch (Exception e) {
			logger.error("error for creat logintask : {}", domain, e);
		}

		((LoginAction)loginTask.getActions().get(loginTask.getActions().size()-1)).setAccount(account);

		return loginTask;
	}

	/**
	 * 主方法
	 */
	public static void main(String[] args) throws Exception {

		// Scheduler 初始化
		Scheduler.getInstance();

		Thread.sleep(60000);

		TaskInitializer.getAll().stream().filter(t -> {
			return t.enable == true;
		}).forEach( t ->{

			try {

				if( t.cron == null ){

					// 历史任务
					HttpTaskSubmitter.getInstance().submit(t.class_name, t.init_map_json);
				}
				else {

					// 定时任务
					Msg msg = HttpTaskSubmitter.getInstance().submit(t.class_name, null, t.init_map_json, 0, t.cron);

					Gson gson = new Gson();
					Map<String, String> map = gson.fromJson(msg.data.toString(), Map.class);

					t.scheduled_task_id = map.get("id");

					t.start_time = new Date();

					t.update();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		// 每 10 分钟 执行一次百度任务
		//((Distributor)ChromeDriverDistributor.getInstance()).keepAlive();
	}
}
