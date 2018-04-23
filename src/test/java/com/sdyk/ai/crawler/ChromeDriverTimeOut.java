package com.sdyk.ai.crawler;

import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import one.rewind.io.requester.chrome.ChromeDriverAgent;

import java.util.concurrent.TimeUnit;

/**
 *
 */
public class ChromeDriverTimeOut {

	ChromeDriverAgent agent = new ChromeDriverAgent();

	@Test
	public void test () {
		WebDriver driver = agent.getDriver();

		driver.get("http://weibo.com");

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		//driver.findElement(By.tagName("body")).sendKeys("Keys.ESCAPE");

		JavascriptExecutor js = (JavascriptExecutor) driver;
		js.executeScript("return window.stop");
		System.err.println("1111111111");

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}
}
