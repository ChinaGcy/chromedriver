package com.sdyk.ai.crawler.zbj.task.test;

import com.sdyk.ai.crawler.zbj.proxy.AliyunHost;
import org.junit.Test;

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

			// 查找所有的host
			List<AliyunHost> all = AliyunHost.getAll();
			Thread.sleep(10000);

			for (AliyunHost a : all) {
				a.stop();
			}

			Thread.sleep(50000);
			for (AliyunHost a : all) {
				a.delete();
			}


		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
