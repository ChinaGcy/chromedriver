package com.sdyk.ai.crawler.chrometask.test;

import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import com.sdyk.ai.crawler.specific.shichangbu.task.modelTask.CaseTask;
import com.sdyk.ai.crawler.task.LoginTask;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.LoginAction;
import org.junit.Test;

import static one.rewind.util.FileUtil.readFileByLines;

public class TestSetNoFetchImages {

	@Test
	public void test() throws Exception{

		AccountManager.getInstance().setAllAccountFree();

		// 设置agent
		ChromeDriverAgent agent = new ChromeDriverAgent(ProxyManager.getInstance().getProxyById("53"));

		agent.start();

		// 生成登陆任务
		LoginTask t = LoginTask.buildFromJson(readFileByLines("login_tasks/shichangbu.com.json"));

		// 添加账号
		Account a = AccountManager.getInstance().getAccountByDomain("shichangbu.com");
		((LoginAction)t.getActions().get(t.getActions().size() - 1)).setAccount(a);

		// 提交登陆方法
		agent.submit(t);

		// 生成描述任务
		CaseTask caseTask = new CaseTask("http://www.shichangbu.com/portal.php?mod=product&op=view&id=2244");
		caseTask.setNoFetchImages();

		CaseTask caseTask1 = new CaseTask("http://www.shichangbu.com/portal.php?mod=product&op=view&id=2297");
		caseTask1.setNoFetchImages();

		CaseTask caseTask2 = new CaseTask("http://www.shichangbu.com/portal.php?mod=case&op=view&id=3");
		caseTask2.setNoFetchImages();

		// 提交描述任务
		agent.submit(caseTask2);
		agent.submit(caseTask);

		Thread.sleep(60000);
		System.out.println(11111111);

		agent.submit(caseTask2);
		agent.submit(caseTask);

		//agent.submit(caseTask1);
		//agent.submit(caseTask2);

		Thread.sleep(10000000);

	}

}
