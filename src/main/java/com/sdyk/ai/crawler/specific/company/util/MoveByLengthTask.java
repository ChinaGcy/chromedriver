package com.sdyk.ai.crawler.specific.company.util;

import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.Action;
import one.rewind.io.requester.chrome.action.ChromeAction;
import one.rewind.json.JSON;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class MoveByLengthTask extends Action {

	ChromeDriverAgent agent;

	public String lengthValue;

	public String containerClassNmae;

	public MoveByLengthTask(){}

	public MoveByLengthTask( String lengthValue ){

		this.lengthValue = lengthValue;
		this.containerClassNmae = null;
	}

	public MoveByLengthTask( String lengthValue, String containerClassNmae){

		this.lengthValue = lengthValue;
		this.containerClassNmae = containerClassNmae;
	}

	public void run() {
		try {

			WebDriver driver = this.agent.getDriver();
			String js = null;

			if( containerClassNmae == null ){
				js = "scrollTo(0,"+lengthValue+")";
			}
			else {
				js = "document.getElementsByClassName(\"" + containerClassNmae + "\")[0].scrollTop=" + lengthValue;
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
