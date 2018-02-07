package org.tfelab.io.requester.chrome.action;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.chrome.ChromeDriver;
import org.tfelab.json.JSON;

public class SetWindowSizeAction extends ChromeDriverAction {

	int width;
	int height;

	public SetWindowSizeAction() {}

	public SetWindowSizeAction(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public boolean run(ChromeDriver driver) {
		try {

			Dimension d = new Dimension(width, height);
			driver.manage().window().setSize(d);
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