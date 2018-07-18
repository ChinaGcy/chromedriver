//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package one.rewind.io.requester.chrome.action;

import one.rewind.io.requester.chrome.ChromeDriverAgent;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class ClickAction extends Action {
	public String elementPath;
	public long sleepTime = 0L;

	public ClickAction() {
	}

	public ClickAction(String elementPath) {
		this.elementPath = elementPath;
	}

	public ClickAction(String elementPath, long sleepTime) {
		this.elementPath = elementPath;
		this.sleepTime = sleepTime;
	}

	public boolean run(ChromeDriverAgent agent) {
		try {
			WebElement el = agent.getDriver().findElement(By.cssSelector(this.elementPath));
			if (el != null) {
				el.click();
				if (this.sleepTime > 0L) {
					agent.getDriver().wait(this.sleepTime);
				}

				return true;
			}

			logger.warn("{} not found.", this.elementPath);
		} catch (Exception var3) {
			logger.error("Exec click action error. ", var3);
		}

		return false;
	}
}
