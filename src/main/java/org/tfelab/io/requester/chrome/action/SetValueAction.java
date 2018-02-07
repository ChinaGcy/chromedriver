package org.tfelab.io.requester.chrome.action;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import static org.tfelab.io.requester.chrome.ChromeDriverAgent.getElementWait;
import static org.tfelab.io.requester.chrome.ChromeDriverAgent.logger;

/**
* 输入框填值
* @author karajan@tfelab.org
* 2017年3月21日 下午8:47:31
*/
public class SetValueAction extends ChromeDriverAction {

   public String inputPath;
   public String value;

   public SetValueAction() {};

   public SetValueAction(String inputPath, String value) {
	   this.inputPath = inputPath;
	   this.value = value;
   }

   public boolean run(ChromeDriver driver) {

	   //WebElement el = driver.findElement(By.cssSelector(inputPath));
	   try {
		   WebElement el = getElementWait(driver, inputPath);

		   if(el == null) {
			   logger.info("{} not found.", inputPath);
			   return false;
		   }
		   el.clear();
		   el = null;
		   el = driver.findElement(By.cssSelector(inputPath));
		   el.sendKeys(value);
		   return true;
	   } catch (org.openqa.selenium.TimeoutException e) {

		   return true;
	   }
   }
}