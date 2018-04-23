package com.sdyk.ai.crawler;

import com.sdyk.ai.crawler.zbj.Crawler;
import com.sdyk.ai.crawler.zbj.docker.DockerHostManager;
import com.sdyk.ai.crawler.zbj.model.DockerHost;
import com.sdyk.ai.crawler.zbj.proxy.AliyunHost;
import com.sdyk.ai.crawler.zbj.proxy.ProxyManager;
import org.junit.Test;

public class CrawlerTest {

	@Test
	public void test() throws Exception {

		DockerHost host = DockerHostManager.getInstance().getHostByIp("10.0.0.62");
		DockerHostManager.getInstance().delAllDockerContainers(host);

		// ProxyManager.getInstance().deleteProxyByGroup(AliyunHost.Proxy_Group_Name);

		// AliyunHost.stopAndDeleteAll();

		Crawler crawler = new Crawler();

		Thread.sleep(6000000);

	}
}
