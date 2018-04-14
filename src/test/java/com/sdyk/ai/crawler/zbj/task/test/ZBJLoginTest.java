package com.sdyk.ai.crawler.zbj.task.test;

import one.rewind.simulator.mouse.MouseEventTracker;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import one.rewind.io.requester.chrome.ChromeDriverAgent;

public class ZBJLoginTest {

	@Test
	public void zbjLoginTest() throws Exception {

		ChromeDriverAgent agent = new ChromeDriverAgent();
		agent.getDriver().get("https://login.zbj.com/login");

		agent.getElementWait(".geetest_radar_tip_content").click();
		MouseEventTracker tracker = new MouseEventTracker();
		tracker.start();
		WebDriverWait wait = new WebDriverWait(agent.getDriver(), 60);
		wait.until(ExpectedConditions.or(
				ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".geetest_success_radar_tip_content"))
		));
		tracker.stop();
		tracker.serializeMovements();
		agent.getElementWait("#login > div.j-login-by.login-by-username.login-by-active > div.zbj-form-item.login-form-button > button").click();

		agent.close();
	}
}
