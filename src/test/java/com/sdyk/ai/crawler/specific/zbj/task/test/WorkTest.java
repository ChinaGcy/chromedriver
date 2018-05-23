package com.sdyk.ai.crawler.specific.zbj.task.test;

import com.sdyk.ai.crawler.specific.zbj.task.Task;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.WorkTask;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.WorkScanTask;
import one.rewind.io.requester.exception.ChromeDriverException;
import org.junit.Test;
import one.rewind.io.requester.BasicRequester;
import one.rewind.io.requester.chrome.ChromeDriverAgent;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class WorkTest {

	/**
	 * 测试workTask
	 */
	@Test
	public void workTaskTest() throws ChromeDriverException.IllegalStatusException, MalformedURLException, URISyntaxException {

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();
		WorkTask workTask = new WorkTask("http://shop.zbj.com/works/detail-wid-329637.html","329637");
		workTask.setBuildDom();
		agent.submit(workTask);
	}


	@Test
	public void testBasicRequester() {
	/*	one.rewind.io.requester.Task t = null;
		try {
		t = new one.rewind.io.requester.Task("http://www.baidu.com");
		BasicRequester.getInstance().fetch(t);

	} catch (MalformedURLException e) {
		e.printStackTrace();
	} catch (URISyntaxException e) {
		e.printStackTrace();
	}*/

}
 }
