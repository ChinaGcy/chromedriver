package com.sdyk.ai.crawler;

import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.ScanTask;
import com.sdyk.ai.crawler.util.StatManager;
import one.rewind.db.RedissonAdapter;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import org.redisson.api.RMap;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Requester extends ChromeDriverRequester {

	public static RMap<String, Date> URL_VISITS = RedissonAdapter.redisson.getMap("Zbj-URL-Visits");

	/*static {
		logger.info("Replace ChromeDriverRequester with {}.", Requester.class.getName());
		ChromeDriverRequester.instance = new Requester();
		requester_executor.submit(ChromeDriverRequester.instance);
	}*/

	public static List<String> WHITE_URLS = Arrays.asList(
			"http://www.zbj.com",
			"https://passport.clouderwork.com/signin",
			"https://www.mihuashi.com/login",
			"https://passport.lagou.com/pro/login.html"
	);

	//
	public static ConcurrentHashMap<String, Integer> taskStat = new ConcurrentHashMap<>();

	public Requester() {}

	public long getTaskQueueSize() {
		return queue.size();
	}

	/**
	 * 当程序异常退出，需要重构 URL_VISITS
	 * @param task
	 */
	public void submit(Task task) {

		String hash = hash(task.getUrl());

		// 列表扫描任务的处理
		if(task instanceof com.sdyk.ai.crawler.task.ScanTask || ! URL_VISITS.containsKey(hash) || WHITE_URLS.contains(task.getUrl())) {

			URL_VISITS.put(hash, new Date());

			// TODO 任务队列中包含相同URL的Task，该Task不需要提交
			task.addDoneCallback(() -> {
				StatManager.getInstance().count();
				taskStat.put(task.getClass().getSimpleName(), taskStat.get(task.getClass().getSimpleName()) - 1);
			});

			// 对于ScanTask 记录TaskTrace
			if(task instanceof com.sdyk.ai.crawler.task.ScanTask) {
				task.addDoneCallback(() -> {

					TaskTrace tt = ((ScanTask) task).getTaskTrace();
					try {
						if (tt != null) {
							tt.insert();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			}

			queue.offer(task);

			if(!taskStat.contains(task.getClass().getSimpleName())) {
				taskStat.put(task.getClass().getSimpleName(), 1);
			} else {
				taskStat.put(task.getClass().getSimpleName(), taskStat.get(task.getClass().getSimpleName()) + 1);
			}
		}
	}

	/**
	 *
	 * @param url
	 * @return
	 */
	public String hash(String url) {
		return one.rewind.txt.StringUtil.MD5(url);
	}

	/**
	 *
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
