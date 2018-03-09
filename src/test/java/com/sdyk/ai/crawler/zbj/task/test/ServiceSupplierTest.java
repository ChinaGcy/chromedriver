package com.sdyk.ai.crawler.zbj.task.test;

import com.sdyk.ai.crawler.zbj.model.Proxy;
import com.sdyk.ai.crawler.zbj.task.ServiceSupplierTask;
import com.sdyk.ai.crawler.zbj.task.Task;
import org.junit.Test;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ServiceSupplierTest {

	/**
	 *
	 * @throws Exception
	 */
	@Test
	public void supplierTest() throws Exception {

		ChromeDriverAgent agent = new ChromeDriverAgent();

		Queue<Task> taskQueue = new LinkedBlockingQueue<>();

		taskQueue.add(new ServiceSupplierTask("http://shop.zbj.com/11622281/"));

		while(!taskQueue.isEmpty()) {
			Task t = taskQueue.poll();
			Proxy pw_ = new Proxy();
			pw_.host = "118.190.83.89";
			pw_.port = 59998;
			pw_.username = "tfelab";
			pw_.password = "TfeLAB2@15";
			t.setProxyWrapper(pw_);
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
		}
	}



}
