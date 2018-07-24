package com.sdyk.ai.crawler.binary.test;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.task.LoginTask;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.action.LoginAction;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import static one.rewind.util.FileUtil.readFileByLines;

public class TestBinary {

	@Test
	public void test() throws Exception {

		// 设置agent
		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();

		// 生成登陆任务
		LoginTask t = LoginTask.buildFromJson(readFileByLines("login_tasks/clouderwork.com.json"));

		// 添加账号
		Account a = AccountManager.getInstance().getAccountByDomain("clouderwork.com");
		((LoginAction)t.getActions().get(t.getActions().size() - 1)).setAccount(a);

		// 提交登陆方法
		agent.submit(t);

		// 下载任务
		BinaryTask b = new BinaryTask("https://www.clouderwork.com/jobs/2745074f2f67dacd");
		agent.submit(b);

		Thread.sleep(10000000);
	}

}
