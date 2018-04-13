package tfelab.io.test;

import net.lightbody.bmp.BrowserMobProxyServer;
import org.junit.Test;
import org.tfelab.io.requester.Task;
import org.tfelab.io.requester.account.Account;
import org.tfelab.io.requester.account.AccountImpl;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;
import org.tfelab.io.requester.chrome.ChromeDriverRequester;
import org.tfelab.io.requester.chrome.action.ChromeAction;
import org.tfelab.io.requester.chrome.action.LoginWithGeetestAction;
import org.tfelab.io.requester.exception.ChromeDriverException;
import org.tfelab.io.requester.proxy.ProxyWrapper;
import org.tfelab.io.requester.proxy.ProxyWrapperImpl;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * Created by karajan on 2017/6/3.
 */
public class ChromeDriverAgentTest {

	@Test
	public void test() throws Exception {

		Task t = new Task("https://www.google.com/");

		ProxyWrapper proxy = new ProxyWrapperImpl("scisaga.net", 60103, null, null);

		ChromeDriverAgent agent = new ChromeDriverAgent(proxy, ChromeDriverAgent.Flag.MITM);

/*		agent.setIdleCallback(()->{
			System.err.println("IDLE");
		});*/

		agent.setTerminatedCallback(()->{
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

			ChromeDriverAgent agent = new ChromeDriverAgent(null);
			Task task = new Task("http://www.zbj.com");
			ChromeAction action = new LoginWithGeetestAction(account);
			task.addAction(action);
			agent.submit(task);

			agent.stop();
		}

	}
}