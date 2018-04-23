package com.sdyk.ai.crawler.zbj.task.test;

import org.junit.Test;
import one.rewind.io.requester.Task;
import one.rewind.io.requester.chrome.ChromeDriverAgent;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class ProxyRequestTest {

	@Test
	public void testRequestByProxy() throws MalformedURLException, URISyntaxException, InterruptedException {

		/*Task t = new Task("https://www.baidu.com/s?wd=ip");

		Proxy pw = new Proxy();
		pw.sshHost = "scisaga.net";
		pw.port = 60103;

		Proxy pw_ = new Proxy();
		pw_.sshHost = "114.215.70.14";
		pw_.port = 59998;
		pw_.username = "tfelab";
		pw_.password = "TfeLAB2@15";

		t.setProxyWrapper(pw);
		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.fetch(t);

		ChromeDriverAgent agent_ = new ChromeDriverAgent();
		t.setProxyWrapper(pw_);
		agent_.fetch(t);

		Thread.sleep(3000);

		Proxy pw1 = new Proxy();
		pw1.sshHost = "118.190.133.34";
		pw1.port = 59998;
		pw1.username = "tfelab";
		pw1.password = "TfeLAB2@15";

		t.setProxyWrapper(pw);
		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.fetch(t);

		t.setProxyWrapper(pw1);
		agent_.fetch(t);

		Thread.sleep(3000);
		agent_.fetch(t);

		Thread.sleep(3000);
		agent_.fetch(t);
		Thread.sleep(3000);
		agent_.fetch(t);

		Thread.sleep(3000);
*/	}

	/**
	 * 开启服务器代理测试
	 *
	 * @throws Exception
	 */
	@Test
	public void replaceProxy() throws Exception {

		/*Task t = new Task("https://www.baidu.com/s?wd=ip");
		ChromeDriverAgent agent_ = new ChromeDriverAgent();
		agent_.fetch(t);

		ZbjProxyWrapper proxyWapper = new ZbjProxyWrapper();

		proxyWapper.getProxy(proxyWapper);

		t.setProxyWrapper(Proxy.getValidProxy("aliyun"));

		agent_.fetch(t);

		agent_.fetch(t);*/
	}




}
