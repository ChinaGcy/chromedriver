package org.tfelab.io.requester.chrome.action;

import org.openqa.selenium.chrome.ChromeDriver;
import org.tfelab.json.JSON;

/**
* 刷新页面
* @author karajan@tfelab.org
* 2017年3月21日 下午8:48:30
*/
public class RefreshAction extends ChromeDriverAction {

   public RefreshAction() {}

   public boolean run(ChromeDriver driver) {
	   driver.navigate().refresh();
	   return true;
   }

   @Override
   public String toJSON() {
	   return JSON.toJson(this);
   }
}