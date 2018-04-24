package com.sdyk.ai.crawler.zbj.task.test;

import com.sdyk.ai.crawler.zbj.proxy.AliyunHost;
import one.rewind.io.SshManager;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

public class AliyunHostTest {

	@Test
	public void AliyunHostT() throws Exception {

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
}
