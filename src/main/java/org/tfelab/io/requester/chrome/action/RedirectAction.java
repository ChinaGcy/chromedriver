package org.tfelab.io.requester.chrome.action;

import org.openqa.selenium.chrome.ChromeDriver;
import org.tfelab.json.JSON;

/**
* 浏览页面
* @author karajan@tfelab.org
* 2017年3月21日 下午8:48:52
*/
public class RedirectAction extends ChromeDriverAction {

   public String url;

   public RedirectAction() {}

   public RedirectAction(String url) {
	   this.url = url;
   }

   public boolean run(ChromeDriver driver) throws InterruptedException {

	   try {
		   driver.get(url);
		   Thread.sleep(5000);
	   } catch (org.openqa.selenium.TimeoutException e) {
		   //driver.navigate().refresh();
	   }

	   return true;
   }

   @Override
   public String toJSON() {
	   return JSON.toJson(this);
   }
}