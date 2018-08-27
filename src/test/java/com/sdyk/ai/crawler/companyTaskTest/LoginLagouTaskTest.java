package com.sdyk.ai.crawler.companyTaskTest;

import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.account.AccountManager;
import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.task.LoginTask;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.action.LoginAction;
import org.junit.Test;

import static one.rewind.util.FileUtil.readFileByLines;

public class LoginLagouTaskTest {

	@Test
	public void test() throws Exception{

		// 创建新容器
		DockerHostManager.getInstance().delAllDockerContainers();
		DockerHostManager.getInstance().createDockerContainers(1);

		// 获取新容器
		ChromeDriverDockerContainer container = DockerHostManager.getInstance().getFreeContainer();

		AccountManager.getInstance().setAllAccountFree();

		ChromeDriverDistributor.instance = new Distributor();

		ChromeDriverAgent agent = new ChromeDriverAgent(container.getRemoteAddress(), container);
		((Distributor)ChromeDriverDistributor.getInstance()).addAgent(agent);

		LoginTask loginTask1 = LoginTask.buildFromJson(readFileByLines("login_tasks/lagou.com.json"));
		((LoginAction)loginTask1.getActions().get(loginTask1.getActions().size() - 1)).setAccount(
				AccountManager.getInstance().getAccountByDomain("lagou.com")
		);

		((Distributor)ChromeDriverDistributor.getInstance()).submitLoginTask(agent, loginTask1);

		Thread.sleep(6000);

		((Distributor)ChromeDriverDistributor.getInstance()).submitLoginTask(agent, loginTask1);

		Thread.sleep(1000000);
	}
}
