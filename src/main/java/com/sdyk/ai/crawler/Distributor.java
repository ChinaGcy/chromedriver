package com.sdyk.ai.crawler;

import com.google.common.collect.Maps;
import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import com.sdyk.ai.crawler.task.ScanTask;
import com.sdyk.ai.crawler.util.StatManager;
import one.rewind.db.RedissonAdapter;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.exception.TaskException;
import one.rewind.io.requester.proxy.Proxy;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import one.rewind.json.JSON;
import one.rewind.txt.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.json.Json;
import org.redisson.api.RMap;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Distributor extends ChromeDriverDistributor {

	public static final Logger logger = LogManager.getLogger(ChromeDriverDistributor.class.getName());

	public static RMap<String, Long> URL_VISITS = RedissonAdapter.redisson.getMap("URL-Visits");

	public static Set<String> URL_VISITS_SET = new HashSet<>();

	public static Map<String, List<String>> AGENT_TASK_MAP = new HashMap<>();

	static {
		logger.info("Replace {} with {}.", ChromeDriverDistributor.class.getName(), Distributor.class.getName());
	}

	public ConcurrentHashMap<ChromeDriverAgent, Queue<ChromeTask>> loginTaskQueues = new ConcurrentHashMap();

	/**
	 * 定义白名单
	 * TODO 为什么不需要定义？
	 */
	/*public static List<String> WHITE_URLS = Arrays.asList(
			"http://www.zbj.com",
			"https://passport.clouderwork.com/signin",
			"https://www.mihuashi.com/login",
			"https://passport.lagou.com/pro/login.html",
			"http://www.shichangbu.com/member.php?mod=logging&action=login"
	);*/

	/**
	 * 任务队列中，每种类型任务的实时数量统计
	 */
	public static ConcurrentHashMap<String, Integer> taskQueueStat = new ConcurrentHashMap<>();

	/**
	 *
	 */
	public Distributor() {
		super();
	}

	/**
	 * 提交登陆任务
	 * @param loginTask
	 */
	public void submitLoginTask(ChromeDriverAgent agent, ChromeTask loginTask) {
		if(!loginTaskQueues.containsKey(agent)) {
			loginTaskQueues.put(agent, new LinkedList<>());
		}

		loginTaskQueues.get(agent).add(loginTask);

		// 更新统计信息
		if(!taskQueueStat.keySet().contains(loginTask.getClass().getName())) {
			taskQueueStat.put(loginTask.getClass().getName(), 1);
		} else {
			taskQueueStat.put(loginTask.getClass().getName(), taskQueueStat.get(loginTask.getClass().getName()) + 1);
		}

	}

	/**
	 * 向队列中添加任务
	 * 当程序异常退出，需要重构 URL_VISITS
	 */
	public Map<String, Object> submit(ChromeTaskHolder holder) throws Exception {

		Class<? extends ChromeTask> clazz = (Class<? extends ChromeTask>) Class.forName(holder.class_name);

		ChromeTask.Builder builder = ChromeTask.Builders.get(clazz);

		String url = ChromeTask.generateURL(builder, holder.init_map);

		String hash = StringUtil.MD5(url);

		long min_interval = Long.valueOf(clazz.getField("MIN_INTERVAL").getLong(clazz));

		long last_visit = 0;

		// 初次执行任务时，URL_VISITS.get(hash) 为 null
		if( URL_VISITS.get(hash) != null ){
			last_visit = URL_VISITS.get(hash);
		}

		// 上次采集时间过滤
		if(last_visit != 0 && (new Date().getTime() - last_visit) < min_interval) {
			System.err.println("Collection interval is too short :" + url);
			logger.error("{} {} fetch interval is less than MIN_INTERVAL {}, discard.", clazz.getName(), url, min_interval);
			throw new TaskException.LessThanMinIntervalException();
		}

		URL_VISITS.put(hash, new Date().getTime());

		String domain = holder.domain;
		String username = holder.username;

		ChromeDriverAgent agent = null;

		// 特定用户的采集任务
		if(holder.username != null && holder.username.length() > 0) {

			String account_key = domain + "-" + username;

			agent = domain_account_agent_map.get(account_key);

			if(agent == null) {

				logger.warn("No agent hold account {}.", account_key);
				throw new AccountException.NotFound();
			}

		}
		// 需要登录采集的任务 或 没有找到加载指定账户的Agent
		else if(holder.login_task){

			if(!domain_agent_map.keySet().contains(domain)) {
				logger.warn("No agent hold {} accounts.", domain);
				throw new AccountException.NotFound();
			}

			agent = domain_agent_map.get(domain).stream().map(a -> {
				int queue_size = queues.get(a).size();
				return Maps.immutableEntry(a, queue_size);
			})
			.sorted(Map.Entry.<ChromeDriverAgent, Integer>comparingByValue())
			.limit(1)
			.map(Map.Entry::getKey)
			.collect(Collectors.toList())
			.get(0);
		}
		// 一般任务
		else {

			// todo Collectors.toList() 返回值为0
			agent = queues.keySet().stream()
				.filter( a -> {
					return !ProxyManager.getInstance().isProxyBannedByDomain(a.proxy, holder.domain) &&
							!a.accounts.keySet().contains("tianyancha.com") &&
							!a.accounts.keySet().contains("lagou.com");
				})
				.map(a -> {
					int queue_size = queues.get(a).size();
					return Maps.immutableEntry(a, queue_size);
				})
				.sorted(Map.Entry.<ChromeDriverAgent, Integer>comparingByValue())
				.limit(1)
				.map(Map.Entry::getKey)
				.collect(Collectors.toList())
				.get(0);
		}

		// 生成指派信息
		if(agent != null) {

			logger.info("Assign {} {} {} {} to agent:{}.", holder.class_name, domain, username!=null?username:"", holder.init_map, agent.name);

			if( !URL_VISITS_SET.contains(hash) ){

				// 更新统计信息
				if(!taskQueueStat.keySet().contains(holder.class_name)) {
					taskQueueStat.put(holder.class_name, 1);
				} else {
					taskQueueStat.put(holder.class_name, taskQueueStat.get(holder.class_name) + 1);
				}

				// 添加task信息
				if(!AGENT_TASK_MAP.keySet().contains(agent.name)){
					List<String> taskUrl = new ArrayList<>();
					taskUrl.add(url);
					AGENT_TASK_MAP.put(agent.name, taskUrl);
				}
				else {
					List<String> taskUrl = AGENT_TASK_MAP.get(agent.name);
					taskUrl.add(url);
					AGENT_TASK_MAP.put(agent.name, taskUrl);
				}

				// 添加任务
				queues.get(agent).put(holder);
				URL_VISITS_SET.add(hash);

				Map<String, Object> info = new HashMap<>();
				info.put("localIp", LOCAL_IP);
				info.put("agent", agent.getInfo());
				info.put("domain", domain);
				info.put("account", username);

				return info;
			}
			logger.warn("warn Agent-queue hive this task");
		}

		logger.warn("Agent not found for task:{}-{}.", domain, username);

		throw new ChromeDriverException.NotFoundException();


		//return super.submit(holder);
	}

	/**
	 * 从阻塞队列中 获取任务
	 * @param agent
	 * @return
	 * @throws InterruptedException
	 */
	public ChromeTask distribute(ChromeDriverAgent agent) throws InterruptedException {

		ChromeTask task = null;

		if( loginTaskQueues.get(agent) != null && !loginTaskQueues.get(agent).isEmpty() ){
			task = loginTaskQueues.get(agent).poll();
		}

		ChromeTaskHolder holder = null;

		try {

			if(task == null) {

				holder = queues.get(agent).poll(10, TimeUnit.SECONDS);

				// todo 现在为循环直至有任务出现，跳出循环，是否有更简单方法。
				while(task == null && holder == null){

					holder = queues.get(agent).poll(10, TimeUnit.SECONDS);

					if( loginTaskQueues.get(agent) != null && !loginTaskQueues.get(agent).isEmpty() ){
						task = loginTaskQueues.get(agent).poll();
					}

				}
			}

			if( task != null ){

				// 记录访问量，任务数，队列任务信息
				task.addDoneCallback((t) -> {

					StatManager.getInstance().count();
					taskQueueStat.put(t.getClass().getName(), taskQueueStat.get(t.getClass().getName()) - 1);
					String hash = StringUtil.MD5(t.getUrl());
					URL_VISITS_SET.remove(hash);
				});

				// 记录task信息
				AGENT_TASK_MAP.get(agent.name).remove(task.getUrl());
				return task;
			}

			task = holder.build();

			ChromeTaskHolder holder_ = holder;

			String className = task != null ? task.getClass().getName() : holder.class_name;

			// 记录访问量，任务数，队列任务信息
			task.addDoneCallback((t) -> {

				StatManager.getInstance().count();
				taskQueueStat.put(className, taskQueueStat.get(className) - 1);
				String hash = StringUtil.MD5(t.getUrl());
				URL_VISITS_SET.remove(hash);
			});

			// 对于ScanTask 记录TaskTrace
			if(task instanceof com.sdyk.ai.crawler.task.ScanTask) {
				task.addDoneCallback((t) -> {

					try {
						TaskTrace tt = ((ScanTask) t).getTaskTrace();
						if (tt != null) {
							tt.insert();
						}
					}
					catch (Exception e) {
						logger.error("error for get TaskTrace", e);
					}

				});
			}

			// 任务失败重试逻辑
			task.addExceptionCallback((t) -> {

				if(t.needRetry()) {

					// 重试逻辑
					if( t.getRetryCount() < 3 ) {

						t.addRetryCount();
						if(holder_ != null) {
							submit(holder_);
						}
						// 登陆任务也可以重试
						else {
							submitLoginTask(agent, (ChromeTask) t);
						}

					}
					// 失败任务保存到数据库
					else {

						try {
							t.insert();
						} catch (Exception e) {
							logger.error(e);
						}
					}
				}
			});

			logger.info("Task:{} Assign {}.", task.getUrl(), agent.name);

			taskCount++;

			return task;

		}
		catch (Exception e) {

			// Recursive call to get task
			logger.error("Task submit failed. {} ", task != null? task : holder, e);
			return distribute(agent);
		}
	}

	/**
	 * 创建并返回一个空容器
	 * @return
	 */
	public ChromeDriverDockerContainer getChromeDriverDockerContainer() {
		try {
			DockerHostManager.getInstance().createDockerContainers(1);
			return DockerHostManager.getInstance().getFreeContainer();
		} catch (Exception e) {
			logger.error("Error get free container, ", e);
			return null;
		}
	}

	/**
	 * 判断是否有可用于特定domain的agent
	 * @param domain
	 * @return
	 */
	public ChromeDriverAgent findAgentWithoutDomain(String domain) throws Exception {

		for( ChromeDriverAgent agent : queues.keySet() ){

			// agent 没有执行过 domain 的 task
			if( ! agent.accounts.keySet().contains( domain ) ){

				// 获取将agent_proxy封禁的列表
				if( !ProxyManager.getInstance().proxyDomainBannedMap.get(agent.proxy.getInfo()).contains(domain) ){
					return agent;
				}
			}
		}

		return null;
	}
}
