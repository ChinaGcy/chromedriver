package com.sdyk.ai.crawler.mihuashi.task.test;


import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ServiceProviderTask;
import com.sdyk.ai.crawler.task.LoginTask;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.LoginAction;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static one.rewind.util.FileUtil.readFileByLines;

public class ServiceSupplierTaskTest {

	@Test
	public void testServiceSupplierTask() throws Exception {

		ChromeDriverAgent agent = new ChromeDriverAgent();

		agent.start();

		/*LoginTask loginTask = LoginTask.buildFromJson(readFileByLines("login_tasks/mihuashi.com.json"));

		// 添加账号
		Account a = AccountManager.getInstance().getAccountByDomain("mihuashi.com");
		((LoginAction)loginTask.getActions().get(loginTask.getActions().size() - 1)).setAccount(a);

		agent.submit(loginTask);*/

		ServiceProviderTask serviceProviderTask = new ServiceProviderTask("https://www.mihuashi.com/users/sroin66?role=painter");

		agent.submit(serviceProviderTask);

		Thread.sleep(100000000);


	}

}
