package com.sdyk.ai.crawler.specific.action;

import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.Action;
import one.rewind.io.requester.chrome.action.ChromeAction;
import one.rewind.json.JSON;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class MouseSuspensionAction extends Action {

	public ChromeDriverAgent agent;

	public String elementCssPath;

	public String js;

	public MouseSuspensionAction(){}

	public MouseSuspensionAction(String elementCssPath){

		this.elementCssPath = elementCssPath;

	}

	public boolean run( ChromeDriverAgent agent ) {

		this.agent = agent;

		//获取 WebDriver
		WebDriver driver = this.agent.getDriver();

		//获取悬浮元素
		WebElement el = driver.findElement(By.cssSelector(this.elementCssPath));

		//悬浮操作
		Actions builder1=new Actions(driver);
		builder1.moveToElement(el).build().perform();

		//随机延时
		try {
			int randomNumber1 = (int)(Math.random() * 2 + 2);
			Thread.sleep(randomNumber1 * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return true;

	}

	public String toJSON() {
		return JSON.toJson(this);
	}

}
