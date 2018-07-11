package com.sdyk.ai.crawler;

import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.ScanTask;
import com.sdyk.ai.crawler.util.StatManager;
import one.rewind.db.RedissonAdapter;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;

import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.exception.TaskException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import org.redisson.api.RMap;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Distributor extends ChromeDriverDistributor {

	public static RMap<String, Long> URL_VISITS = RedissonAdapter.redisson.getMap("URL-Visits");

	static {
		logger.info("Replace {} with {}.", ChromeDriverDistributor.class.getName(), Distributor.class.getName());
	}

	/**
	 * 定义白名单
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
	 * 当程序异常退出，需要重构 URL_VISITS
	 */
	public Map<String, Object> submit(ChromeTaskHolder holder) throws Exception {

		Class<? extends ChromeTask> clazz = (Class<? extends ChromeTask>) Class.forName(holder.class_name);

		ChromeTask.Builder builder = ChromeTask.Builders.get(clazz);

		String url = ChromeTask.generateURL(builder, holder.init_map);

		String hash = one.rewind.txt.StringUtil.MD5(url);

		long min_interval = clazz.getField("MIN_INTERVAL").getLong(clazz);

		long last_visit = URL_VISITS.get(hash);

		// 上次采集时间过滤
		if(last_visit < min_interval) {
			logger.info("{} {} fetch interval is less than MIN_INTERVAL {}, discard.", clazz.getName(), url, min_interval);
			throw new TaskException.LessThanMinIntervalException();
		}

		URL_VISITS.put(hash, new Date().getTime());

		// 更新统计信息
		if(!taskQueueStat.contains(holder.class_name)) {
			taskQueueStat.put(holder.class_name, 1);
		} else {
			taskQueueStat.put(holder.class_name, taskQueueStat.get(holder.class_name) + 1);
		}

		return submit(holder);
	}

	/**
	 * 从阻塞队列中 获取任务
	 * @param agent
	 * @return
	 * @throws InterruptedException
	 */
	public ChromeTask distribute(ChromeDriverAgent agent) throws InterruptedException {

		ChromeTaskHolder holder = queues.get(agent).take();

		ChromeTask task = null;

		try {

			task = holder.build();

			task.addDoneCallback((t) -> {
				StatManager.getInstance().count();
				taskQueueStat.put(holder.class_name, taskQueueStat.get(holder.class_name) - 1);
			});

			// 对于ScanTask 记录TaskTrace
			if(task instanceof com.sdyk.ai.crawler.task.ScanTask) {
				task.addDoneCallback((t) -> {

					TaskTrace tt = ((ScanTask) t).getTaskTrace();
					if (tt != null) {
						tt.insert();
					}
				});
			}

			// 任务失败重试逻辑
			task.addDoneCallback((t) -> {

				if(t.needRetry()) {

					// 重试逻辑
					if( t.getRetryCount() < 3 ) {

						t.addRetryCount();
						submit(holder);

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
			logger.error("Task build failed. {} ", holder, e);
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
}
