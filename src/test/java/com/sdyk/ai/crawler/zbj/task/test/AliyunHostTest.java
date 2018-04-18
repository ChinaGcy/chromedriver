package com.sdyk.ai.crawler.zbj.task.test;

import com.sdyk.ai.crawler.zbj.model.Proxy;
import com.sdyk.ai.crawler.zbj.proxy.AliyunHost;
import one.rewind.db.Refacter;
import one.rewind.io.requester.Task;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
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
	public void aliyunHostStart() throws Exception {
		try {
			AliyunHost.batchBuild(5);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
