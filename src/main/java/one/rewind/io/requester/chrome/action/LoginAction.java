//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package one.rewind.io.requester.chrome.action;

import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import org.openqa.selenium.WebElement;

public class LoginAction extends Action {
	public ChromeDriverAgent agent;
	public String url = "https://login.zbj.com/login";
	public String usernameCssPath = "#username";
	public String passwordCssPath = "#password";
	public String loginButtonCssPath = "#login > div.j-login-by.login-by-username.login-by-active > div.zbj-form-item.login-form-button > button";
	public String errorMsgReg = "账号或密码错误";
	public transient boolean success = false;
	public transient Account account;

	public LoginAction() {
	}

	public LoginAction(Account account) {
		this.account = account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Account getAccount() {
		return this.account;
	}

	boolean fillUsernameAndPassword() {
		try {
			/*this.agent.getUrl(this.url);
			this.agent.waitPageLoad(this.url);*/
			WebElement usernameInput = this.agent.getElementWait(this.usernameCssPath);
			usernameInput.clear();
			usernameInput.sendKeys(new CharSequence[]{this.account.getUsername()});

			Thread.sleep(2000);

			WebElement passwordInput = this.agent.getElementWait(this.passwordCssPath);
			passwordInput.sendKeys(new CharSequence[]{this.account.getPassword()});
			Thread.sleep(1000L);
			return true;
		} catch (Exception var3) {
			logger.error(var3);
			return false;
		}
	}

	boolean clickSubmitButton() {
		try {
			this.agent.getElementWait(this.loginButtonCssPath).click();
			Thread.sleep(5000L);
		} catch (Exception var2) {
			logger.error(var2);
			return false;
		}

		return !this.agent.getDriver().getPageSource().matches(this.errorMsgReg);
	}

	public boolean run(ChromeDriverAgent agent) {
		this.agent = agent;
		if (!this.fillUsernameAndPassword()) {
			return false;
		} else {
			return this.clickSubmitButton();
		}
	}
}
