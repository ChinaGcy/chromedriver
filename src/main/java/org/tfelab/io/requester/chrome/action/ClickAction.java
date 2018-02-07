package org.tfelab.io.requester.chrome.action;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.tfelab.json.JSON;

import static org.tfelab.io.requester.chrome.ChromeDriverAgent.logger;

/**
* 点击
* @author karajan@tfelab.org
* 2017年3月21日 下午8:47:18
*/
public class ClickAction extends ChromeDriverAction {

   public String elementPath;

   public ClickAction() {}

   public ClickAction(String elementPath) {
	   this.elementPath = elementPath;
   }

   public boolean run(ChromeDriver driver) throws InterruptedException {
	   WebElement we = driver.findElement(By.cssSelector(elementPath));
	   if(we != null) {
		   try {
			   we.click();
		   } catch (org.openqa.selenium.TimeoutException e) {
			   //driver.navigate().refresh();
		   }

		   Thread.sleep(5000);
		   return true;
	   } else {
		   logger.info("{} not found.", elementPath);
		   return false;
	   }
   }

   @Override
   public String toJSON() {
	   return JSON.toJson(this);
   }
}