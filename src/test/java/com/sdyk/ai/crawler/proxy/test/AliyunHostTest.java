package com.sdyk.ai.crawler.proxy.test;

import com.sdyk.ai.crawler.zbj.proxy.AliyunHost;
import one.rewind.io.ssh.SshManager;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class AliyunHostTest {

	@Test
	public void AliyunHostTest() throws Exception {

		//Refacter.createTable(AliyunHost.class);
		AliyunHost aliyunHost = AliyunHost.buildService(AliyunHost.Region.CN_SHENZHEN);
		aliyunHost.createSquidProxy();
		aliyunHost.insert();
	}

	@Test
	public void connect() throws IOException {
		SshManager.Host ssh_host = new SshManager.Host("47.106.35.84", 22, "root", "SdYK@315Fr##");
		ssh_host.connect();
}

	@Test
	public void aliyunHostStart() throws Exception {

		AliyunHost.stopAndDeleteAll();
	}

	@Test
	public void crerateHost() throws InterruptedException {
		AliyunHost.batchBuild(1);
	}

	@Test
	public void uploadTest() throws Exception {
		SshManager.Host host = new SshManager.Host("119.23.246.39", 22,"root","SdYK@315Fr##");
		host.connect();
		host.upload("squid.sh", "");

	}
}
