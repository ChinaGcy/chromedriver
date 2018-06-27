package com.sdyk.ai.crawler.requester.test;

import net.lightbody.bmp.BrowserMobProxyServer;
import one.rewind.io.requester.Task;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.account.AccountImpl;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.chrome.action.ChromeAction;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.proxy.Proxy;
import one.rewind.io.requester.proxy.ProxyImpl;
import org.junit.Test;
import org.openqa.selenium.By;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * Created by karajan on 2017/6/3.
 */
public class ChromeDriverAgentTest {

	@Test
	public void test() throws Exception {

		Task t = new Task("https://www.google.com/");

		Proxy proxy = new ProxyImpl("scisaga.net", 60103, null, null);

		ChromeDriverAgent agent = new ChromeDriverAgent(proxy, ChromeDriverAgent.Flag.MITM);

		agent.start();

/*		agent.setIdleCallback(()->{
			System.err.println("IDLE");
		});*/

		agent.addTerminatedCallback(()->{
			System.err.println("TERMINATED");
		});

		agent.submit(t);

		agent.stop();

	}

	@Test
	public void testBuildProxy() {

		BrowserMobProxyServer ps = ChromeDriverRequester.buildBMProxy(null);

		System.err.println(ps.getPort());

	}

	@Test
	public void loginTest() throws MalformedURLException, URISyntaxException, ChromeDriverException.IllegalStatusException {

		Account account = new AccountImpl("zbj.com", "15284812411", "123456");

			for(int i=0; i<1; i++) {

				ChromeDriverAgent agent = new ChromeDriverAgent();
				agent.start();

				Task task = new Task("http://www.zbj.com");
				ChromeAction action = new LoginWithGeetestAction(account);
				task.addAction(action);
				agent.submit(task);

			agent.stop();
		}

	}

	@Test
	public void testIP() throws ChromeDriverException.IllegalStatusException, MalformedURLException, URISyntaxException {

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();

		Task task = new Task("http://www.zbj.com");

		for(int a = 0; a< 2000 ; a++) {
			agent.submit(task);
		}

	}


	@Test
	public void test1() throws Exception {
		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();
		Task task = new Task("http://testwww.315free.com/customer/demands-add");
		agent.submit(task);
		agent.getDriver().findElement(By.cssSelector("#addDemand > div.login > div > div > button")).click();

		Thread.sleep(5000);

		agent.getDriver().findElement(By.cssSelector("body > div.el-message-box__wrapper > div > div.el-message-box__btns > button.el-button.el-button--default.el-button--small.el-button--primary")).click();

		Thread.sleep(10000000);
	}

}