package org.tfelab.io.requester.chrome.action;

import org.openqa.selenium.chrome.ChromeDriver;
import org.tfelab.json.JSON;

public class ClearCacheAction extends ChromeDriverAction {

	public ClearCacheAction() {}

	public boolean run(ChromeDriver driver) {

		//WebElement el = driver.findElement(By.cssSelector(inputPath));
		try {
			driver.get("chrome://settings-frame/clearBrowserData");
			new ClickAction("#clear-browser-data-commit").run(driver);
			return true;
		} catch (InterruptedException e) {
			return false;
		}
	}

	@Override
	public String toJSON() {
		return JSON.toJson(this);
	}
}