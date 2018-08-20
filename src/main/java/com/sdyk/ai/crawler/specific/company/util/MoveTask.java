package com.sdyk.ai.crawler.specific.company.util;

import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.Action;
import one.rewind.io.requester.chrome.action.ChromeAction;
import one.rewind.json.JSON;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class MoveTask extends Action {

	ChromeDriverAgent agent;

	public String elementPath;

	public String containerClassNmae;

	public MoveTask(){}

	public MoveTask( String elementPath ){

		this.elementPath = elementPath;
		this.containerClassNmae = null;
	}

	public MoveTask( String elementPath, String containerClassNmae ){

		this.elementPath = elementPath;
		this.containerClassNmae = containerClassNmae;
	}

	public void run() {
		try {

			//延时操作
			int randomNumber = (int) Math.round(Math.random()*6);
			Thread.sleep(randomNumber * 1000);

			WebDriver driver = this.agent.getDriver();
			WebElement el = driver.findElement(By.cssSelector(this.elementPath));
			int elementPosition = el.getLocation().getY();
			String js = null;

			//容器为空
			if( containerClassNmae == null ){
				js = String.format("window.scroll(0, %s)", elementPosition);
			}
			//容器不为空
			else {
				js = "document.getElementsByClassName(\"" + containerClassNmae + "\")[0].scrollTop=" + elementPosition;
				System.out.println(js);
			}

			((JavascriptExecutor)driver).executeScript(js);

		} catch (Exception var2) {
			logger.error("Exec click action error. ", var2);
		}

	}

	public String toJSON() {
		return JSON.toJson(this);
	}

}
