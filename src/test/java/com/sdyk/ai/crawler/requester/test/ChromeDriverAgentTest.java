package com.sdyk.ai.crawler.requester.test;

import com.sdyk.ai.crawler.docker.DockerHostManager;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.ServiceProviderTask;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.TendererTask;
import net.lightbody.bmp.BrowserMobProxyServer;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.account.AccountImpl;
import one.rewind.io.requester.callback.TaskCallback;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.action.ChromeAction;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.proxy.Proxy;
import one.rewind.io.requester.proxy.ProxyImpl;
import one.rewind.io.requester.task.ChromeTask;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * Created by karajan on 2017/6/3.
 */
public class  ChromeDriverAgentTest {

	@Test
	public void test() throws Exception {

		ChromeTask t = new TendererTask("https://home.zbj.com/13568685");

		//Proxy proxy = new ProxyImpl("scisaga.net", 60103, null, null);
		//Proxy proxy = new ProxyImpl("tpda.cc", 60202, "sdyk", "sdyk");

		ChromeDriverAgent agent = new ChromeDriverAgent();

		agent.start();

		agent.submit(t);

		for (TaskCallback taskCallback : t.doneCallbacks) {
			taskCallback.run(t);
		}
		Thread.sleep(10000000);
		agent.stop();

	}

	@Test
	public void testBuildProxy() {

		BrowserMobProxyServer ps = ChromeDriverDistributor.buildBMProxy(null);

		System.err.println(ps.getPort());

	}

	@Test
	public void loginTest() throws MalformedURLException, URISyntaxException, ChromeDriverException.IllegalStatusException, InterruptedException {

		Account account = new AccountImpl("zbj.com", "15284812411", "123456");

		for(int i=0; i<10; i++) {

			ChromeDriverAgent agent = new ChromeDriverAgent();
			agent.start();

			ChromeTask task = new ChromeTask("http://www.zbj.com");
			ChromeAction action = new LoginWithGeetestAction();
			task.addAction(action);
			try {
				agent.submit(task);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Thread.sleep(10000);

			agent.stop();
		}

	}

	@Test
	public void requesterFilterTest() throws MalformedURLException, URISyntaxException, ChromeDriverException.IllegalStatusException, InterruptedException {

		final ChromeTask t = new ChromeTask("https://www.baidu.com/");
		t.addDoneCallback((task)->{
			System.err.println("Done!");
			System.err.println(task.getResponse().getVar("test"));
		});

		t.setResponseFilter((response, contents, messageInfo) -> {
			if(messageInfo.getOriginalUrl().contains("tu_329aca4.js")) {
				t.getResponse().setVar("test", contents.getTextContents());
			}
		});

		// Proxy proxy = new ProxyImpl("10.0.0.51", 49999, null, null);
		ChromeDriverAgent agent = new ChromeDriverAgent(ChromeDriverAgent.Flag.MITM);
		agent.start();

		agent.addTerminatedCallback((a)->{
			System.err.println("TERMINATED");
		});

		try {
			agent.submit(t);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Thread.sleep(10000);

		agent.stop();
	}

	@Test
	public void noFetchImagesTest() throws MalformedURLException, URISyntaxException, ChromeDriverException.IllegalStatusException, InterruptedException {

		ChromeTask t = new ChromeTask("https://beijing.zbj.com/").setNoFetchImages();
		ChromeTask t2 = new ChromeTask("http://www.shichangbu.com/portal.php?mod=product&op=view&id=2244").setNoFetchImages();
		// http://www.shichangbu.com/portal.php?mod=product&op=view&id=2244

		Proxy proxy = new ProxyImpl("scisaga.net", 60103, null, null);
		ChromeDriverAgent agent = new ChromeDriverAgent(ChromeDriverAgent.Flag.MITM);
		agent.start();

		agent.addTerminatedCallback((a)->{
			System.err.println("TERMINATED");
		});

		agent.submit(t);
		agent.submit(t2);
		agent.submit(t);
		agent.submit(t2);
		//t.noFetchImages = false;
		agent.submit(t);
		agent.submit(t2);

		Thread.sleep(10000);

		agent.stop();
	}

	@Test
	public void newCommandTimeoutTest() throws Exception {

		// 获取新容器
		DockerHostManager.getInstance().delAllDockerContainers();
		DockerHostManager.getInstance().createDockerContainers(1);
		ChromeDriverDockerContainer container = DockerHostManager.getInstance().getFreeContainer();

		ChromeDriverAgent agent = new ChromeDriverAgent(container.getRemoteAddress(), container);
		agent.start();

		ChromeTask task = new ChromeTask("https://www.baidu.com");
		agent.submit(task);

		for( int i = 1 ; i > 0 ; i++){
			Thread.sleep(60 * 1000);
			System.out.println(i + " m");
		}

		Thread.sleep( 60 * 60 * 1000);

	}
}