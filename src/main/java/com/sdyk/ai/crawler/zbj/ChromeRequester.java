package com.sdyk.ai.crawler.zbj;

import com.sdyk.ai.crawler.zbj.model.Account;
import com.sdyk.ai.crawler.zbj.task.ProjectScanTask;
import com.sdyk.ai.crawler.zbj.task.Task;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.tfelab.common.Configs;
import org.tfelab.io.requester.Requester;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;
import org.tfelab.io.requester.chrome.ChromeDriverRequester;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class ChromeRequester {

	protected static ChromeRequester instance;

	private static final Logger logger = LogManager.getLogger(ChromeRequester.class.getName());

	private int agentCount = 1;

	public String domain = "zbj.com";

	private List<ChromeDriverWithLogin> agents = new LinkedList<>();

	/**
	 * 单例模式
	 *
	 * @return
	 */
	public static ChromeRequester getInstance() {

		if (instance == null) {

			synchronized (ChromeRequester.class) {
				if (instance == null) {
					instance = new ChromeRequester();
				}
			}
		}

		return instance;
	}

	/**
	 *
	 */
	private ChromeRequester() {

		for(int i=0; i<agentCount; i++) {

			ChromeDriverWithLogin agent = new ChromeDriverWithLogin(domain);

			try {

				agent.login();
				agents.add(agent);
				agent.start();

			} catch (Exception e) {

				logger.error("Error while chrome login. ", e);
			}
		}
	}

	/**
	 *
	 * @return
	 */
	private ChromeDriverWithLogin getDriverWithShortestQueue() {
		int size = Integer.MAX_VALUE;
		ChromeDriverWithLogin agent_ = null;
		for(ChromeDriverWithLogin agent : agents) {
			if(agent.taskQueue.size() < size) {
				size = agent.taskQueue.size();
				agent_ = agent;
			}
		}

		return agent_;
	}

	public void distribute(Task task) {

		ChromeDriverWithLogin agent = this.getDriverWithShortestQueue();
		agent.taskQueue.add(task);

	}

	/**
	 *
	 * @param args
	 */
	public static void main(String[] args) {

		ChromeRequester chromeRequester = ChromeRequester.getInstance();

		chromeRequester.distribute(ProjectScanTask.generateTask("t-pxfw",1,null));
		/*chromeRequester.distribute(ProjectScanTask.generateTask("t-gongyesj",1,null));
		chromeRequester.distribute(ProjectScanTask.generateTask("t-game",1,null));
		chromeRequester.distribute(ProjectScanTask.generateTask("t-yxtg",1,null));*/

	}
}
