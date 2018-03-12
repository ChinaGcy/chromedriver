package com.sdyk.ai.crawler.zbj.task.test;

import com.sdyk.ai.crawler.zbj.requester.ChromeDriverLoginWrapper;
import org.junit.Test;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;

public class ZBJLoginTest {

	@Test
	public void zbjLoginTest() throws Exception {
		ChromeDriverAgent agent = (new ChromeDriverLoginWrapper("zbj.com")).login();


	}
}
