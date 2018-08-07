package com.sdyk.ai.crawler.specific.zbj.task.test;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Scheduler;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.CaseScanTask;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.CaseTask;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.io.requester.callback.TaskCallback;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import org.junit.Test;
import one.rewind.io.requester.chrome.ChromeDriverAgent;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class CaseTest {

	/**
	 * 测试casetask
	 */
	@Test
	public void CaseTest() throws Exception {
		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();
		CaseTask caseTask = new CaseTask("https://shop.zbj.com/4696791/sid-983087.html");
		caseTask.setBuildDom();
		agent.submit(caseTask);
	}

	/**
	 *
	 * @throws Exception
	 */
	@Test
	public void CaseScanTest() throws Exception {

		/*Scheduler.getInstance();

		Map<String, Object> init_map = new HashMap<>();
		ImmutableMap.of("user_id", "19308846", "page", String.valueOf(1));

		Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.zbj.task.scanTask.CaseScanTask");

		//生成holder
		ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

		//提交任务
		ChromeDriverDistributor.getInstance().submit(holder);*/

		ChromeDriverAgent chromeDriverAgent = new ChromeDriverAgent();
		chromeDriverAgent.start();

		Task task = new CaseTask("https://shop.zbj.com/18115303/sid-1312573.html");

		chromeDriverAgent.submit(task);

		for (TaskCallback done : task.doneCallbacks) {
			done.run(task);
		}

	}

	/**
	 *
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	@Test
	public void caseTaskTest() throws MalformedURLException, URISyntaxException {

		/*ChromeDriverAgent agent = new ChromeDriverAgent();

		Queue<Task> taskQueue = new LinkedBlockingQueue<>();

		//taskQueue.add(new CaseTask("http://shop.zbj.com/4685446/sid-207237.html"));
		taskQueue.add(new CaseTask("http://shop.tianpeng.com/18093800/sid-1216982.html"));

		while(!taskQueue.isEmpty()) {
			Task t = taskQueue.poll();
			if(t != null) {
				try {
					agent.fetch(t);
					for (Task t_ : t.postProc(agent.getDriver())) {
						taskQueue.add(t_);
					}

				} catch (Exception e) {

					taskQueue.add(t);
				}
			}
		}*/
	}
}
