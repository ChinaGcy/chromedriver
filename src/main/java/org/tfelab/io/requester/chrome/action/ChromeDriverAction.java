package org.tfelab.io.requester.chrome.action;

import org.openqa.selenium.chrome.ChromeDriver;
import org.tfelab.json.JSON;
import org.tfelab.json.JSONable;

/**
 * Created by karajan on 2017/6/3.
 */
public class ChromeDriverAction implements JSONable {
	public ChromeDriverAction() {};
	public boolean run(ChromeDriver driver) throws Exception {return true;};
	@Override
	public String toJSON() {
		return JSON.toJson(this);
	}
}