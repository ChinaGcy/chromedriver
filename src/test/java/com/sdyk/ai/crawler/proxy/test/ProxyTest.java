package com.sdyk.ai.crawler.proxy.test;

import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.proxy.model.ProxyImpl;
import one.rewind.db.DaoManager;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.proxy.Proxy;
import one.rewind.io.requester.task.ChromeTask;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;

public class ProxyTest {

	public static void main(String[] args) throws Exception {

		Dao<ProxyImpl, String> dao = DaoManager.getDao(ProxyImpl.class);

		List<ProxyImpl> proxies = dao.queryForAll();

		for (ProxyImpl proxy : proxies) {

			proxy.validate();
		}
	}

	@Test
	public void testProxy() throws Exception {

		Dao<ProxyImpl, String> dao = DaoManager.getDao(ProxyImpl.class);

		List<ProxyImpl> proxies = dao.queryForAll();

		DockerHostManager.getInstance().delAllDockerContainers();

		DockerHostManager.getInstance().createDockerContainers(3);


		for( ProxyImpl proxy : proxies ){

			proxy.validate();

			ChromeDriverDockerContainer container = DockerHostManager.getInstance().getFreeContainer();

			ChromeDriverAgent agent = new ChromeDriverAgent(container.getRemoteAddress(), container, proxy);

			System.out.println(container.getRemoteAddress());

			//ChromeDriverAgent agent = new ChromeDriverAgent();
			agent.start();

			//agent.setProxy(proxy);

			try {

				ChromeTask task = new ChromeTask("https://www.baidu.com/s?wd=ip");

				agent.submit(task);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		Thread.sleep(100000);

	}

}
