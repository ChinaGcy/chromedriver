package com.sdyk.ai.crawler.specific.zbj.task.test;

import com.sdyk.ai.crawler.specific.zbj.task.scanTask.CaseScanTask;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.CaseTask;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import org.junit.Test;
import one.rewind.io.requester.chrome.ChromeDriverAgent;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
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

		ChromeDriverAgent agent = new ChromeDriverAgent();

		Task t = CaseScanTask.generateTask("http://shop.zbj.com/19308846/",1);

		Queue<Task> queue = new LinkedBlockingQueue<>();

		queue.add(t);

		/*while(!queue.isEmpty()) {

			Task tt = queue.poll();

			agent.fetch(tt);

			for (Task t1 : tt.postProc(agent.getDriver())) {
				queue.add(t1);
			}

		}*/
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
