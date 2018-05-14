package com.sdyk.ai.crawler.zbj;

import com.sdyk.ai.crawler.zbj.docker.DockerHostManager;
import com.sdyk.ai.crawler.zbj.util.StatManager;
import com.sdyk.ai.crawler.zbj.util.StringUtil;
import one.rewind.db.RedissonAdapter;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import org.redisson.api.RMap;

import java.util.Date;

public class Requester extends ChromeDriverRequester {

	public static RMap<String, Date> urlVisits = RedissonAdapter.redisson.getMap("Zbj-URL-Visits");

	/*static {
		logger.info("Replace ChromeDriverRequester with {}.", Requester.class.getName());
		ChromeDriverRequester.instance = new Requester();
		requester_executor.submit(ChromeDriverRequester.instance);
	}*/

	public Requester() {}

	public void submit(Task task) {

		String hash = hash(task.getUrl());

		if(! urlVisits.containsKey(hash)) {

			task.addDoneCallback(() -> {

				urlVisits.put(hash, new Date());
				StatManager.getInstance().count();
			});

			queue.offer(task);
		}
	}

	public String hash(String url) {
		return one.rewind.txt.StringUtil.MD5(url);
	}

	private ChromeDriverDockerContainer getChromeDriverDockerContainer() {
		try {
			return DockerHostManager.getInstance().getFreeContainer();
		} catch (Exception e) {
			logger.error("Error get free container, ", e);
			return null;
		}
	}
}
