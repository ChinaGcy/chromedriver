package com.sdyk.ai.crawler.specific.zbj.task.test;

import com.sdyk.ai.crawler.specific.zbj.task.modelTask.ServiceProviderRatingTask;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.ServiceProviderTask;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.account.AccountImpl;
import one.rewind.io.requester.chrome.action.ChromeAction;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.junit.Test;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class ServiceSupplierTest {

	/**
	 * 测试ServiceSupplierTask
	 */
	@Test
	public void ServiceSupplierTaskTest() throws ChromeDriverException.IllegalStatusException, MalformedURLException, URISyntaxException {



	}

	/**
	 * 测试workTask
	 */
	@Test
	public void ServiceRatingTaskTest() throws ChromeDriverException.IllegalStatusException, MalformedURLException, URISyntaxException {


	}

	/**
	 *
	 * @throws Exception
	 */
	@Test
	public void supplierTest() throws Exception {

		ChromeDriverAgent agent = new ChromeDriverAgent();

		Queue<Task> taskQueue = new LinkedBlockingQueue<>();

		taskQueue.add(new ServiceProviderTask("http://shop.zbj.com/11622281/"));

		/*while(!taskQueue.isEmpty()) {
			Task t = taskQueue.poll();
			Proxy pw_ = new Proxy();
			pw_.sshHost = "118.190.83.89";
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
		}*/
	}
}
