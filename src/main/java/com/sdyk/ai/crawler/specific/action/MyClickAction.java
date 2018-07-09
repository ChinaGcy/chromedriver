package com.sdyk.ai.crawler.specific.action;

import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.Action;
import one.rewind.io.requester.chrome.action.ChromeAction;
import one.rewind.json.JSON;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class MyClickAction extends Action {

	public ChromeDriverAgent agent;

	public String elementPath;

	public MyClickAction() {
	}

	public MyClickAction(String elementPath) {
		this.elementPath = elementPath;
	}

	public boolean run( ChromeDriverAgent agent ) {

		this.agent = agent;

		try {

			WebElement el = this.agent.getDriver().findElement(By.cssSelector(this.elementPath));
			if (el != null) {
				el.click();

				//随机延时
				Thread.sleep(10000);

			} else {
				logger.warn("{} not found.", this.elementPath);
			}
		} catch (Exception var2) {
			logger.error("Exec click action error. ", var2);
		}

		return  true;

	}

	public String toJSON() {
		return JSON.toJson(this);
	}

}
