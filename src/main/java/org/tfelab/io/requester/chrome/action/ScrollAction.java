package org.tfelab.io.requester.chrome.action;

import org.openqa.selenium.chrome.ChromeDriver;
import org.tfelab.json.JSON;

/**
 * 滚轮事件
 */
public class ScrollAction extends ChromeDriverAction {

	public String value;

	public ScrollAction() {}

	public ScrollAction(String value) {
		this.value = value;
	}

	public boolean run(ChromeDriver driver) {
		try {
			String setscroll = "document.documentElement.scrollTop=" + value;
			driver.executeScript(setscroll);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public String toJSON() {
		return JSON.toJson(this);
	}
}