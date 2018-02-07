package org.tfelab.io.requester.chrome.action;

import org.openqa.selenium.chrome.ChromeDriver;
import org.tfelab.json.JSON;


/**
 * 执行脚本
 * @author karajan@tfelab.org
 * 2017年3月21日 下午8:48:13
 */
public class ExecAction extends ChromeDriverAction {

	public String script;

	public ExecAction() {}

	public ExecAction(String script) {
		this.script = script;
	}

	public boolean run(ChromeDriver driver) {
		try {
			if(script != null & script.length() > 0)
				driver.executeScript(script);
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