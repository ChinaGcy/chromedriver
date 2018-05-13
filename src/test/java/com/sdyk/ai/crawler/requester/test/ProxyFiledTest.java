package com.sdyk.ai.crawler.requester.test;

import com.sdyk.ai.crawler.zbj.proxy.AliyunHost;
import com.sdyk.ai.crawler.zbj.proxy.ProxyManager;
import com.sdyk.ai.crawler.zbj.proxy.exception.NoAvailableProxyException;
import com.sdyk.ai.crawler.zbj.proxy.model.ProxyImpl;
import com.sdyk.ai.crawler.zbj.task.Task;
import com.sdyk.ai.crawler.zbj.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.zbj.task.scanTask.ProjectScanTask;

import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.proxy.Proxy;
import org.junit.Test;

public class ProxyFiledTest {
	/**
	 * 测试代理更换
	 */
	@Test
	public void test() throws Exception {

		ProxyImpl p = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);

		ChromeDriverAgent agent = new ChromeDriverAgent(p);
		Task task = new Task("https://www.baidu.com/s?wd=ip");

		agent.addProxyFailedCallback(() -> {

			try {
				agent.proxy.status = Proxy.Status.INVALID;
				agent.proxy.update();

				ProxyImpl p1 = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);
				agent.changeProxy(p1);
			}
			catch (NoAvailableProxyException e) {
				System.err.println("No Available Proxy, exit.");
				System.exit(0);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		});

		ChromeDriverRequester requester = ChromeDriverRequester.getInstance();
		requester.addAgent(agent);
		agent.start();

		for (int i = 0; i<10; i++) {
			requester.submit(task);
		}

		Thread.sleep(1000000);
	}

	/**
	 * 当代理不可用,更换代理，代理不够，从阿里云申请新的代理服务器放入数据库中使用
	 */
	@Test
	public void getAliyunTest() throws Exception {

		ProxyImpl p = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);

		ChromeDriverAgent agent = new ChromeDriverAgent(p);
		Task task = new Task("https://www.baidu.com/s?wd=ip");

		agent.addProxyFailedCallback(() -> {

			try {
				agent.proxy.status = Proxy.Status.INVALID;
				agent.proxy.update();

				int i = ProxyManager.getInstance().getValidProxyNum();

				if (i < 2) {
					AliyunHost.batchBuild(1);
				}

				ProxyImpl p1 = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);
				agent.changeProxy(p1);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		});

		ChromeDriverRequester requester = ChromeDriverRequester.getInstance();
		requester.addAgent(agent);
		agent.start();

		for (int i = 0; i<6; i++) {
			requester.submit(task);
		}

		Thread.sleep(10000000);
	}
}
