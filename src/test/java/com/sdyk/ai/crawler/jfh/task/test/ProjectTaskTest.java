package com.sdyk.ai.crawler.jfh.task.test;

import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.specific.jfh.task.modelTask.ProjectTask;
import com.sdyk.ai.crawler.task.LoginTask;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.LoginAction;
import org.junit.Test;

import static one.rewind.util.FileUtil.readFileByLines;

public class ProjectTaskTest {

	@Test
	public void test() throws Exception{

		AccountManager.getInstance().setAllAccountFree();

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();

		LoginTask loginTask = LoginTask.buildFromJson(readFileByLines("login_tasks/jfh.com.json"));
		((LoginAction)loginTask.getActions().get(loginTask.getActions().size() - 1)).setAccount(
				AccountManager.getInstance().getAccountByDomain("jfh.com")
		);

		agent.submit(loginTask);

		ProjectTask projectTask = new ProjectTask("https://www.jfh.com/jfportal/orders/jf34958002");

		agent.submit(projectTask);

		Thread.sleep(1000000000);

	}

}
