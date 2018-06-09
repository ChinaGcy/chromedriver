package com.sdyk.ai.crawler.specific.zbj.task.test;

import com.sdyk.ai.crawler.specific.zbj.task.modelTask.ServiceProviderRatingTask;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.ServiceProviderTask;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
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

		ChromeDriverAgent agent = new ChromeDriverAgent();
		ChromeDriverRequester.getInstance().addAgent(agent);
		agent.start();
		ServiceProviderTask serviceSupplierTask = new ServiceProviderTask("http://shop.zbj.com/18751471/");
		serviceSupplierTask.setBuildDom();
		ChromeDriverRequester.getInstance().submit(serviceSupplierTask);
		try {
			Thread.sleep(1000000);
		} catch (InterruptedException e) {

		}

	}

	/**
	 * 测试workTask
	 */
	@Test
	public void ServiceRatingTaskTest() throws ChromeDriverException.IllegalStatusException, MalformedURLException, URISyntaxException {

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();
		ServiceProviderRatingTask serviceProviderRatingTask = new ServiceProviderRatingTask("http://shop.zbj.com/evaluation/evallist-uid-7394304-category-1-isLazyload-0-page-10.html", "7394304",10);
		serviceProviderRatingTask.setBuildDom();
		agent.submit(serviceProviderRatingTask);
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
