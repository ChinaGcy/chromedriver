package com.sdyk.ai.crawler.zbj;

import com.sdyk.ai.crawler.zbj.util.StatManager;
import one.rewind.io.requester.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;

public class Requester extends ChromeDriverRequester {

	/*static {
		logger.info("Replace ChromeDriverRequester with {}.", Requester.class.getName());
		ChromeDriverRequester.instance = new Requester();
		requester_executor.submit(ChromeDriverRequester.instance);
	}*/

	public Requester() {}




	public void submit(Task task) {



		task.addDoneCallback(() -> {
			StatManager.getInstance().count();
		});

		queue.offer(task);
	}
}
