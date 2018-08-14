package com.sdyk.ai.crawler.shichangbu.task.test;

import com.sdyk.ai.crawler.specific.shichangbu.task.modelTask.CaseTask;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

public class CaseTaskTest {

	@Test
	public void test() throws Exception{

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.start();

		CaseTask caseTask = new CaseTask("http://www.shichangbu.com/portal.php?mod=product&op=view&id=7");
		agent.submit(caseTask);

		Thread.sleep(1000000);

	}

	@Test
	public void testFileName() throws UnsupportedEncodingException {

		String src = "https://www.jfh.com/jfhrm/buinfo/download/bu__21859__buInfo__caseLogo__logo_e35788d2-44b4-4650-9947-316e94b257d3___jpg";

		System.out.println(BinaryDownloader.getFileName(src));

	}
}
