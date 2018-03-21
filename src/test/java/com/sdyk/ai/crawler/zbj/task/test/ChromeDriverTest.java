package com.sdyk.ai.crawler.zbj.task.test;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;
import org.tfelab.io.requester.BasicRequester;
import org.tfelab.io.requester.Task;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class ChromeDriverTest {

	@Test
	public void test() {
		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.getDriver()
				.get("https://rms.zhubajie.com/resource/redirect?key=homesite/task/网站开发-1_206.xlsx/origine/f76f7652-1708-47e3-8e2b-caacecf365b9");

		/*try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}*/
	}

	@Test
	public void binaryDownloader() {

		Task t_ = null;
		try {
			t_ = new Task("https://rms.zhubajie.com/resource/redirect?key=homesite/task/网站开发-1_206.xlsx/origine/f76f7652-1708-47e3-8e2b-caacecf365b9");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		BasicRequester.getInstance().fetch(t_);

		System.err.println(t_.getResponse().getSrc());
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.err.println(t_.getResponse().getSrc().length);
	}

	@Test
	public void openTwoPage() {
		ChromeDriverAgent agent = new ChromeDriverAgent();
		WebDriver webDriver = agent.getDriver();
		webDriver.get("https://www.baidu.com/s?wd=ip");
		webDriver.findElement(By.cssSelector("#\\31 > h3 > a")).sendKeys(Keys.CONTROL +"t");
		/*Actions actionOpenLinkInNewTab = new Actions(webDriver);

		actionOpenLinkInNewTab.keyDown(Keys.CONTROL).sendKeys("t").keyUp(Keys.CONTROL).perform();*/
	}

}
