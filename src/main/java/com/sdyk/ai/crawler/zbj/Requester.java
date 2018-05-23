package com.sdyk.ai.crawler.zbj;

import com.sdyk.ai.crawler.zbj.docker.DockerHostManager;
import com.sdyk.ai.crawler.zbj.model.TaskTrace;
import com.sdyk.ai.crawler.zbj.task.scanTask.ScanTask;
import com.sdyk.ai.crawler.zbj.util.StatManager;
import one.rewind.db.RedissonAdapter;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import org.redisson.api.RMap;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Requester extends ChromeDriverRequester {

	public static RMap<String, Date> URL_VISITS = RedissonAdapter.redisson.getMap("Zbj-URL-Visits");

	/*static {
		logger.info("Replace ChromeDriverRequester with {}.", Requester.class.getName());
		ChromeDriverRequester.instance = new Requester();
		requester_executor.submit(ChromeDriverRequester.instance);
	}*/

	private static List<String> WHITE_URLS = Arrays.asList(
			"http://www.zbj.com"
	);

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
		if(task instanceof ScanTask) {

			// TODO 任务队列中包含相同URL的Task，该Task不需要提交
			task.addDoneCallback(() -> {

				StatManager.getInstance().count();
			});

			task.addDoneCallback(() -> {

				TaskTrace tt = ((ScanTask) task).getTaskTrace();
				try {
					tt.insert();
				} catch (Exception e) {
					e.printStackTrace();
				}
			});

			queue.offer(task);

		}
		// Model采集任务
		else if(! URL_VISITS.containsKey(hash) || WHITE_URLS.contains(task.getUrl())) {

			URL_VISITS.put(hash, new Date());

			task.addDoneCallback(() -> {

				StatManager.getInstance().count();
			});

			queue.offer(task);
		}
	}

	public String hash(String url) {
		return one.rewind.txt.StringUtil.MD5(url);
	}

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
