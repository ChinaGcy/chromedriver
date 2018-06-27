package com.sdyk.ai.crawler.requester.test;

import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.docker.model.DockerHostImpl;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import net.lightbody.bmp.BrowserMobProxyServer;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.BasicRequester;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.proxy.Proxy;
import one.rewind.io.requester.proxy.ProxyImpl;
import one.rewind.io.ssh.SshManager;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;

import static one.rewind.io.requester.chrome.ChromeDriverRequester.buildBMProxy;

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

		System.err.println(ChromeDriverRequester.REQUESTER_LOCAL_IP + ":" + agent.bmProxy_port);

		Task task = new Task("http://ddns.oray.com/checkip");
		agent.submit(task);

		System.err.println(task.getResponse().getText());

		Thread.sleep(1000000);
	}


	@Test
	public void remoteTest() throws Exception {

		delAllDockerContainers();

		createDockerContainers();

		ChromeDriverRequester requester = ChromeDriverRequester.getInstance();

		for(int i=0; i<10; i++) {

			final Proxy proxy = ProxyManager.getInstance().getValidProxy("aliyun-cn-shenzhen-squid");
			final URL remoteAddress = new URL("http://10.0.0.62:" + (31000 + i) + "/wd/hub");

			new Thread(() -> {
				try {

					ChromeDriverAgent agent = new ChromeDriverAgent(remoteAddress, null, proxy);
					//ChromeDriverAgent agent = new ChromeDriverAgent(remoteAddress);

					requester.addAgent(agent);

					agent.start();

				} catch (ChromeDriverException.IllegalStatusException e) {
					e.printStackTrace();
				}
			}).start();

		}

		for(int i=0; i<100; i++) {

			Task task = new Task("https://www.baidu.com/s?word=ip");
			requester.submit(task);
		}

		Thread.sleep(3000000);

		requester.close();

		delAllDockerContainers();
	}

	@Test
	public void testBuildProxyServer() throws InterruptedException, UnknownHostException {

		Proxy proxy = new ProxyImpl("scisaga.net", 60103, "tfelab", "TfeLAB2@15");
		BrowserMobProxyServer ps = buildBMProxy(proxy);
		System.err.println(ps.getClientBindAddress());
		System.err.println(ps.getPort());
		Thread.sleep(100000);
	}

	@Test
	public void RestartContainer() throws Exception {

		DockerHostImpl host = new DockerHostImpl("10.0.0.62", 22, "root");
		host.delAllDockerContainers();

		ChromeDriverDockerContainer container = host.createChromeDriverDockerContainer();

		ChromeDriverRequester requester = ChromeDriverRequester.getInstance();

		final Proxy proxy = new ProxyImpl("114.215.45.48", 59998, "tfelab", "TfeLAB2@15");
		final URL remoteAddress = container.getRemoteAddress();

		ChromeDriverAgent agent = new ChromeDriverAgent(remoteAddress, container, proxy);

		requester.addAgent(agent);

		agent.start();

		for(int i=0; i<100; i++) {
			Task task = new Task("http://www.baidu.com/s?wd=" + i);
			requester.submit(task);
		}

		Thread.sleep(100000000);

	}

	@Test
	public void postTest() throws MalformedURLException, URISyntaxException {

		String project_id = "8bc2056a0f60e46b732cdfc8ad126fcb";
		String url = "http://localhost/zbj/get_contact/" + project_id;

		one.rewind.io.requester.Task t = new one.rewind.io.requester.Task(url);
		t.setPost();

		BasicRequester.getInstance().submit(t);

		System.err.println(t.getResponse().getText());
	}
}
