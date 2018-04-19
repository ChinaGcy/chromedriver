package com.sdyk.ai.crawler.zbj.task.test;

import com.sdyk.ai.crawler.zbj.proxy.AliyunHost;
import org.junit.Test;

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
