package com.sdyk.ai.crawler.specific.action;

import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.Action;
import one.rewind.io.requester.chrome.action.ChromeAction;
import one.rewind.json.JSON;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class WriteAction extends Action {

	public ChromeDriverAgent agent;

	public String elementPath;

	public String detail;

	public WriteAction(){}

	public WriteAction(String elementPath, String detail ){
		this.elementPath = elementPath;
		this.detail = detail;
	}

	public boolean run( ChromeDriverAgent agent ) {

		this.agent = agent;

		try {

			//随机延时
			Thread.sleep(4000);

			WebElement el = this.agent.getDriver().findElement(By.cssSelector(this.elementPath));
			if (el != null) {
				el.clear();
				el.sendKeys(new CharSequence[]{this.detail});
			} else {
				logger.warn("{} not found.", this.elementPath);
			}
		} catch (Exception var2) {
			logger.error("Exec click action error. ", var2);
		}

		return true;
	}


	public String toJSON() {
		return JSON.toJson(this);
	}

}
