package com.sdyk.ai.crawler;

import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.account.model.AccountImpl;
import com.sdyk.ai.crawler.model.Domain;
import com.sdyk.ai.crawler.model.WebDirverCount;
import com.sdyk.ai.crawler.proxy.exception.NoAvailableProxyException;
import com.sdyk.ai.crawler.proxy.model.ProxyImpl;
import com.sdyk.ai.crawler.proxy.AliyunHost;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import com.sdyk.ai.crawler.specific.clouderwork.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.task.LogTask;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import one.rewind.db.DaoManager;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.proxy.Proxy;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import one.rewind.io.requester.task.Task;
import one.rewind.util.Configs;
import org.apache.logging.log4j.LogManager;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import java.util.*;

/**
 * 任务生成器
 */
public class Scheduler {

	public static final org.apache.logging.log4j.Logger logger = LogManager.getLogger(Scheduler.class.getName());

	int driverCount = 1;

	public String domain;

	public Scheduler() {}

	public Scheduler(String domain, int driverCount) {

		this.driverCount = driverCount;

		this.domain = domain;

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
	public void init(String domain, int driverCount) {

		List<ChromeDriverAgent> agents = new ArrayList<>();

		int needCreatedriverCount = 0;

		//已有初始化的 agent
		if( ChromeDriverDistributor.getInstance().queues != null &&
				ChromeDriverDistributor.getInstance().queues.keySet().size() > 0 ){

			//获取未加载该网站的所有agent
			agents = getAllAgentWithoutDomain(domain);

			needCreatedriverCount = driverCount - agents.size();


		}
		else {

			needCreatedriverCount = driverCount;
			// 删除所有docker container
			try {
				DockerHostManager.getInstance().delAllDockerContainers();
			} catch (Exception e) {
				e.printStackTrace();
			}

			Distributor.URL_VISITS.clear();

			// TODO 根据情况使用
			try {
				resetAccountAndProxy();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		try {

			if( needCreatedriverCount > 0 ){

				// 创建阿里云host
				//AliyunHost.batchBuild(driverCount);

				// 创建 container
				DockerHostManager.getInstance().createDockerContainers(needCreatedriverCount);
			}

			// 读取有效账户 driverCount 个
			List<AccountImpl> accounts = AccountManager.getAccountByDomain(domain, driverCount);

			List<AccountImpl> accounts_use = accounts.subList(0, agents.size());
			List<AccountImpl> accounts_new = accounts.subList(agents.size(), accounts.size());

			// 为已有 agent 添加登陆任务
			if( agents.size() > 0 ){
				for( int i = 0 ; i< agents.size() ; i++ ){

					List<ChromeDriverAgent> finalAgents = agents;

					int finalI = i;

					Thread thread = new Thread(() -> {

						try {
							// 为正在运行的Agent添加登陆任务
							busyAgentAddLoginTask(null, accounts_use.get(finalI), finalAgents.get(finalI));

						} catch (Exception e) {
							e.printStackTrace();
						}


					});
					thread.start();
				}
			}

			// 创建新的 agent 并进行登陆
			for(AccountImpl account : accounts_new) {

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

										System.err.println("代理失败回调");

										//删除代理
										//aliyunHost = AliyunHost.getByHost(proxy.host);
										//aliyunHost.stopAndDelete();

										//删除数据库
										//ProxyManager.getInstance().deleteProxyById(proxy.id);

									} catch (Exception e) {
										logger.error("AliyunHost:{} Error, ", proxy.host, e);
									}

								}

							});

							// 生成登录任务
							ChromeDriverDockerContainer container = DockerHostManager.getInstance().getFreeContainer();

							//ChromeDriverAgent agent = new ChromeDriverAgent(container.getRemoteAddress());
							ChromeDriverAgent agent = new ChromeDriverAgent(/*container.getRemoteAddress(), container,*/ proxy);

							// agent 账号异常回调
							agent.addAccountFailedCallback((agent_ ,account_, task_)->{

								// 设置账户状态
								account_.status = Account.Status.Broken;

								// 更新账户状态
								if(account_ instanceof AccountImpl) {
									((AccountImpl) account_).update();
								}

								// 1. 判断是否还有可用账号
								Account account_new = AccountManager.getAccountByDomain(task_.getDomain());
								if( account_new != null ){

									// 2. 判断是否有 没有加载domain 的Agent
									ChromeDriverAgent chromeDriverAgent = findAgentWithoutDomain(task_.getDomain());

									// 3. 如果2有，添加登陆任务
									if( chromeDriverAgent != null ){

										busyAgentAddLoginTask(null, account_, chromeDriverAgent);

									}
									// 4. 如果2没有，新建一个ChromeDriverAgent
									else {

										DockerHostManager.getInstance().createDockerContainers(1);

										ChromeDriverDockerContainer container_new = DockerHostManager.getInstance().getFreeContainer();

										ProxyImpl proxy_new = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);

										ChromeDriverAgent agent_new = new ChromeDriverAgent(container_new.getRemoteAddress(),
												container_new, proxy_new);

										agent_new.submit(getLoginTaskByDomain(task_.getDomain(), account_new));

										ChromeDriverDistributor.getInstance().addAgent(agent_new);

									}

								}

								logger.info("Account {}:{} failed.", account.domain, account.username);

							}).addProxyFailedCallback((agent1, proxy1, task1)->{

								System.err.println(" 回调 Agent Proxy 代理异常 ");

								// 代理异常回掉
								logger.info("Proxy {}:{} failed.", proxy1.host, proxy1.port);

								// 增加封禁的 domain
								ProxyManager.getInstance().addProxyBannedRecord(proxy1, task1.getDomain());

								try {

									// 对代理异常进行处理
									dealWithProxyFailed(agent1, proxy1, task1);

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

								logger.info("Container {} {}:{} Terminated.", container.name, container.ip,
										container.vncPort);

							}).addNewCallback((agent1)->{

								try {
									// 添加登陆任务
									agent.submit(getLoginTaskByDomain(domain, account));
								} catch (Exception e) {
									logger.error(e);
								}
							});

							ChromeDriverDistributor.getInstance().addAgent(agent);

							/*latch.countDown();*/
							/*logger.info("ChromeDriverAgent remote address {}, local proxy {}:{}",
									agent.remoteAddress,
									agent.bmProxy.getClientBindAddress(), agent.bmProxy_port);*/
						}

					}
					catch (Exception ex) {
						logger.error("", ex);
					}
				});

				thread.start();
			}

			logger.info("ChromeDriverAgents are ready.");

		} catch (Exception e) {
			logger.error(e);
		}

	}

	/**
	 * 处理代理异常
	 * @param agent1
	 * @param proxy1
	 * @param task1
	 * @throws Exception
	 */
	public void dealWithProxyFailed(ChromeDriverAgent agent1, Proxy proxy1, ChromeTask task1) throws Exception {


		// 测试代理是否可用
		if( proxy1.validate() ){

			proxyCanUse(agent1, proxy1, task1);

		}
		//代理不可用
		else {

			//更换代理
			changeProxy(agent1, task1);

		}

	}

	public void changeProxy(ChromeDriverAgent agent, ChromeTask task) throws Exception {

		// 释放代理
		agent.proxy.failed();

		// 新建代理
		ProxyImpl spareProxy = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);

		// 更换代理
		agent.changeProxy(spareProxy);

		// 创建失败任务的holder
		ChromeTaskHolder holder = ChromeTask.buildHolder(task.getClass(), task.init_map);

		// 添加失败任务
		ChromeDriverDistributor.getInstance().submit(holder, agent);

	}

	/**
	 * 代理被网站封禁时
	 * @param agent1
	 * @param proxy1
	 * @param task1
	 * @throws Exception
	 */
	public void proxyCanUse(ChromeDriverAgent agent1, Proxy proxy1, ChromeTask task1) throws Exception {

		//获取所有domain
		Dao<Domain, String> dao = DaoManager.getDao(Domain.class);
		List<Domain> domainList = dao.queryForAll();

		boolean allDomainBanned = true;

		// 判断代理是否被所有domain封禁
		for( Domain domain : domainList ){

			if( !proxy1.bannedDomains.contains(domain.domain) ){
				allDomainBanned = false;
				break;
			}

		}

		// 代理被所有domain封禁
		if( allDomainBanned ){

			//更换代理
			changeProxy(agent1, task1);

		}
		// 未被所有domain禁用
		else {

			ChromeDriverAgent haveAgent = findAgentWithoutDomain(task1.getDomain());

			// agent-proxy 绑定账号
			if(agent1.accounts != null && agent1.accounts.keySet().size() > 0 &&
					!agent1.accounts.keySet().add(task1.getDomain()) ){

				// 有可进行该网站登陆任务的agent
				if( haveAgent != null ){

					busyAgentAddLoginTask(task1, agent1.accounts.get(task1.getDomain()), haveAgent);

				}
				// 无可进行该网站登陆任务的agent
				else {

					DockerHostManager.getInstance().createDockerContainers(1);

					ChromeDriverDockerContainer container_new = DockerHostManager.getInstance().getFreeContainer();

					ProxyImpl proxy_new = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);

					ChromeDriverAgent agent_new = new ChromeDriverAgent(container_new.getRemoteAddress(),
							container_new, proxy_new);

					agent_new.submit(getLoginTaskByDomain(task1.getDomain(), agent1.accounts.get(task1.getDomain())));

					ChromeDriverDistributor.getInstance().addAgent(agent_new);

				}


			}
			// agent-proxy 未绑定账号
			else {

				// 不含有可执行该网站任务的agent
				if( haveAgent  == null ){

					DockerHostManager.getInstance().createDockerContainers(1);

					ChromeDriverDockerContainer container_new = DockerHostManager.getInstance().getFreeContainer();

					ProxyImpl proxy_new = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);

					ChromeDriverAgent agent_new = new ChromeDriverAgent(container_new.getRemoteAddress(),
							container_new, proxy_new);

					agent_new.accounts.put(task1.getDomain(),null);

					ChromeDriverDistributor.getInstance().addAgent(agent_new);

				}

			}

		}

	}

	/**
	 * 判断是否有可用于特定domain的agent
	 * @param domain
	 * @return
	 */
	public ChromeDriverAgent findAgentWithoutDomain(String domain ) throws Exception {

		for( ChromeDriverAgent chromeDriverAgent :
				ChromeDriverDistributor.getInstance().queues.keySet() ){

			// 含有可执行该网站任务的agent
			if( ! chromeDriverAgent.accounts.keySet().contains( domain ) ){

				Proxy agent_proxy = ProxyManager.getInstance().getProxyById(String.valueOf(chromeDriverAgent.proxy.id));

				// 获取将agent_proxy封禁的列表
				if( !ProxyManager.getInstance().proxyDomainBannedMap.get(agent_proxy.host).contains(domain) ){
					return chromeDriverAgent;
				}

			}
		}
		return null;
	}

	/**
	 * 获取 Distributor 中所有未加载特定domain 但可以加载特定 domain 的 agent
	 * @param domain
	 * @return
	 */
	public List<ChromeDriverAgent> getAllAgentWithoutDomain(String domain ){

		List<ChromeDriverAgent> agentList = new ArrayList<>();

		for( ChromeDriverAgent userfulAgent : ChromeDriverDistributor.getInstance().queues.keySet() ){

			Set<String> accountSet = new HashSet<>(userfulAgent.accounts.keySet());

			if( accountSet.add( domain ) ){
				accountSet.remove( domain );
				agentList.add(userfulAgent);
			}
		}

		return agentList;
	}

	/**
	 * 为正在执行任务的agent增加登陆任务
	 * @param task
	 * @param account
	 * @param agent
	 * @throws ChromeDriverException.IllegalStatusException
	 * @throws InterruptedException
	 */
	public void busyAgentAddLoginTask(ChromeTask task, Account account, ChromeDriverAgent agent) throws Exception {


		try {

			Config base = ConfigFactory.load();
			InputStream stream = Configs.class.getClassLoader().getResourceAsStream("conf/LogPath.conf");
			Config config = ConfigFactory.parseReader(new InputStreamReader(stream)).withFallback(base);

			// TODO account.domain 应该是 根域名形式 aka 后面要带 .com .net ...
			String loginClassName = config.getConfig(account.domain).getString("loginClassName");

			// 设置参数
			Map<String, Object> init_map = new HashMap<>();
			init_map.put("accountId", account.id);

			// 反射初始化类
			Class<? extends ChromeTask> clazz = (Class<? extends ChromeTask>) Class.forName(loginClassName);

			// 生成holder
			ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

			// 提交登陆任务任务
			ChromeDriverDistributor.getInstance().submit(holder, agent);

			if( task != null ){
				// 提交运行失败的任务
				ChromeDriverDistributor.getInstance().submit( task.getHolder(task.getClass(), task.init_map, task.getPriority()) );
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * 通过 domain 生成登陆任务
	 * @param domain
	 * @param account
	 * @return
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public ChromeTask getLoginTaskByDomain(String domain, Account account) throws MalformedURLException, URISyntaxException {

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
	public static void main(String[] args){

		ChromeDriverDistributor.instance = new Distributor();

		//new Thread(()->{ServiceWrapper.getInstance();}).start();

		Scheduler scheduler = new Scheduler();

		WebDirverCount webDirverCount = new WebDirverCount();

		List<WebDirverCount> webDirverCounts = webDirverCount.getAll();

		for( WebDirverCount w : webDirverCounts ){

			scheduler.init(w.domain, w.dirver_count);

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}

	}
}
