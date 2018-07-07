package com.sdyk.ai.crawler.requester.test;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.docker.model.DockerHostImpl;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import net.lightbody.bmp.BrowserMobProxyServer;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.BasicRequester;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.proxy.Proxy;
import one.rewind.io.requester.proxy.ProxyImpl;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.ssh.SshManager;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;


public class RemoteDriverTest {


	/**
	 * 创建容器
	 * @throws Exception
	 */
	@Test
	public void createDockerContainers() throws Exception {

		// DockerHostImpl host = DockerHostManager.getInstance().getHostByIp("10.0.0.62");
		DockerHostManager.getInstance().createDockerContainers(10);
	}

	/**
	 * 删除容器
	 * @throws Exception
	 */
	@Test
	public void delAllDockerContainers() throws Exception {

		SshManager.Host host = new SshManager.Host("10.0.0.62", 22, "root", "sdyk315pr");
		host.connect();

		String cmd = "docker stop $(docker ps -a -q) && docker rm $(docker ps -a -q)\n";

		String output = host.exec(cmd);

		System.err.println(output);
	}

	@Test
	public void simpleTest() throws Exception {

		//final Proxy proxy = new ProxyImpl("114.215.70.14", 59998, "tfelab", "TfeLAB2@15");
		final URL remoteAddress = new URL("http://10.0.0.56:4444/wd/hub");
		ChromeDriverAgent agent = new ChromeDriverAgent(remoteAddress,null);
		agent.start();

		System.err.println(ChromeDriverDistributor.LOCAL_IP + ":" + agent.bmProxy_port);

		Task task = new Task("http://ddns.oray.com/checkip");
		agent.submit(task);

		System.err.println(task.getResponse().getText());

		Thread.sleep(1000000);
	}

	@Test
	public void RestartContainer() throws Exception {

		DockerHostImpl host = new DockerHostImpl("10.0.0.62", 22, "root");
		host.delAllDockerContainers();

		ChromeDriverDockerContainer container = host.createChromeDriverDockerContainer();

		ChromeDriverDistributor distributor = ChromeDriverDistributor.getInstance();

		final Proxy proxy = new ProxyImpl("114.215.45.48", 59998, "tfelab", "TfeLAB2@15");
		final URL remoteAddress = container.getRemoteAddress();

		ChromeDriverAgent agent = new ChromeDriverAgent(remoteAddress, container, proxy);

		distributor.addAgent(agent);

		agent.start();

		for(int i=0; i<100; i++) {

			//TODO 创建Holder与提交任务应在不同行，报错信息会有多个，利于调优
			ChromeDriverDistributor.getInstance().submit(
					ChromeTask.buildHolder(TestFailedChromeTask.class, ImmutableMap.of("q", String.class))
			);
		}

		Thread.sleep(100000000);

	}

	@Test
	public void postTest() throws MalformedURLException, URISyntaxException {

		String project_id = "8bc2056a0f60e46b732cdfc8ad126fcb";
		String url = "http://localhost/zbj/get_contact/" + project_id;

		one.rewind.io.requester.task.Task t = new one.rewind.io.requester.task.Task(url);
		t.setPost();

		BasicRequester.getInstance().submit(t);

		System.err.println(t.getResponse().getText());
	}
}
