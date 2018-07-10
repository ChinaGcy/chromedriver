package com.sdyk.ai.crawler.itijuziTest;

import com.aliyuncs.ecs.model.v20140526.DescribeImageSharePermissionResponse;
import com.sdyk.ai.crawler.account.model.AccountImpl;
import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.specific.itijuzi.action.ItijuziLoginAction;
import com.sdyk.ai.crawler.specific.itijuzi.task.CompanyListScanTask;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ItijuziScanTaskTest {

	@Test
	public void testItijuziScanTask() throws InterruptedException {

		try {

			ChromeDriverDistributor distributor = ChromeDriverDistributor.getInstance();

			ChromeDriverAgent agent = new ChromeDriverAgent();

			Account account = new AccountImpl("itijuzi","13941617993","zhaobin7758521");

			agent.submit(new ChromeTask("https://www.itjuzi.com/user/login").addAction(new ItijuziLoginAction(account)));

			distributor.addAgent(agent);

			CompanyListScanTask scanTask = new CompanyListScanTask("http://radar.itjuzi.com/company");

			//设置参数
			Map<String, Object> init_map = new HashMap<>();
			init_map.put("page", "1");

			//生成holder
			ChromeTaskHolder holder = ChromeTask.buildHolder(scanTask.getClass(), init_map);

			distributor.submit(holder);

		} catch (Exception e) {
			e.printStackTrace();
		}

		Thread.sleep(100000000);
	}


}
