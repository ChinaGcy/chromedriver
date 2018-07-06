package com.sdyk.ai.crawler.requester.test;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.ServiceWrapper;
import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.model.Project;
import com.sdyk.ai.crawler.model.ServiceProviderRating;
import com.sdyk.ai.crawler.model.TendererRating;
import com.sdyk.ai.crawler.proxy.AliyunHost;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.*;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.CaseScanTask;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.WorkScanTask;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.account.AccountImpl;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * Created by karajan on 2017/6/3.
 */
public class ChromeDriverAgentTest {

	@Test
	public void test() throws Exception {

		/*Task t = new Task("https://www.google.com/");

		Proxy proxy = new ProxyImpl("scisaga.net", 60103, null, null);

		ChromeDriverAgent agent = new ChromeDriverAgent(proxy, ChromeDriverAgent.Flag.MITM);

		agent.start();

*//*		agent.setIdleCallback(()->{
			System.err.println("IDLE");
		});*//*

		agent.addTerminatedCallback(()->{
			System.err.println("TERMINATED");
		});

		agent.submit(t);

		agent.stop();*/

	}

	@Test
	public void testBuildProxy() {

		/*BrowserMobProxyServer ps = ChromeDriverRequester.buildBMProxy(null);

		System.err.println(ps.getPort());*/

	}

	@Test
	public void loginTest() throws MalformedURLException, URISyntaxException, ChromeDriverException.IllegalStatusException {

		Account account = new AccountImpl("zbj.com", "15284812411", "123456");

			for(int i=0; i<1; i++) {

				/*ChromeDriverAgent agent = new ChromeDriverAgent();
				agent.start();

				Task task = new Task("http://www.zbj.com");
				ChromeAction action = new LoginWithGeetestAction(account);
				task.addAction(action);
				agent.submit(task);

			agent.stop();*/
		}

	}

	@Test
	public void testIP() throws ChromeDriverException.IllegalStatusException, MalformedURLException, URISyntaxException {

		/*ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();

		Task task = new Task("http://www.zbj.com");

		for(int a = 0; a< 2000 ; a++) {
			agent.submit(task);
		}*/

	}


	@Test
	public void test1() throws Exception {
		/*ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();
		Task task = new Task("http://testwww.315free.com/customer/demands-add");
		agent.submit(task);
		agent.getDriver().findElement(By.cssSelector("#addDemand > div.login > div > div > button")).click();

		Thread.sleep(5000);

		agent.getDriver().findElement(By.cssSelector("body > div.el-message-box__wrapper > div > div.el-message-box__btns > button.el-button.el-button--default.el-button--small.el-button--primary")).click();

		Thread.sleep(10000000);*/
	}

	@Test
	public void testDistributor() throws Exception {

		DockerHostManager.getInstance().delAllDockerContainers();

		// 创建 container
		DockerHostManager.getInstance().createDockerContainers(6);

		//
		ServiceWrapper.getInstance();

		for (int i = 0; i < 6 ; i++) {

			Thread thread = new Thread(() ->{
				com.sdyk.ai.crawler.proxy.model.ProxyImpl proxy = null;
				try {
					proxy = ProxyManager.getInstance().getValidProxy(AliyunHost.Proxy_Group_Name);
				} catch (Exception e) {
					e.printStackTrace();
				}
				ChromeDriverDockerContainer container = null;
				try {
					container = DockerHostManager.getInstance().getFreeContainer();
				} catch (Exception e) {
					e.printStackTrace();
				}

				ChromeDriverAgent agent = null;
				try {
					agent = new ChromeDriverAgent(container.getRemoteAddress(), container, proxy);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

				try {
					ChromeDriverDistributor.getInstance().addAgent(agent);
				} catch (ChromeDriverException.IllegalStatusException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			});

			thread.start();
		}

		Thread.sleep(1000000);
	}

	@Test
	public void testProject() throws ClassNotFoundException, URISyntaxException, UnsupportedEncodingException, MalformedURLException {

		HttpTaskPoster.getInstance().submit(ProjectScanTask.class,
				ImmutableMap.of("channel", "t-paperwork")
		);
	}
}