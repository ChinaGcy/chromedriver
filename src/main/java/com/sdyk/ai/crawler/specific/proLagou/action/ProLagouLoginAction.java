package com.sdyk.ai.crawler.specific.proLagou.action;

import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.Action;
import one.rewind.json.JSON;
import org.openqa.selenium.WebElement;

public class ProLagouLoginAction extends Action {

	ChromeDriverAgent agent;

	public String url = "https://passport.lagou.com/pro/login.html";
	public String usernameCssPath = "#user_name";
	public String passwordCssPath = "#main > form > div:nth-child(2) > input[category=\"password\"]";
	public String loginButtonCssPath = "#main > form > div.clearfix.btn_login > input[category=\"submit\"]";

	public String errorMsgReg = "账号或密码错误";
	transient boolean success = false;

	transient Account account;

	public ProLagouLoginAction(){ }

	public ProLagouLoginAction(Account account){

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
			this.agent.getUrl(this.url);
			this.agent.waitPageLoad(this.url);

			WebElement usernameInput = this.agent.getElementWait(this.usernameCssPath);
			usernameInput.clear();
			usernameInput.sendKeys(new CharSequence[]{this.account.getUsername()});

			WebElement passwordInput = this.agent.getElementWait(this.passwordCssPath);
			passwordInput.clear();
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

	public void run() {

		if (this.fillUsernameAndPassword()) {

			if (this.clickSubmitButton()) {

				this.success = true;

			}

		}
	}

	public String toJSON() {
		return JSON.toJson(this);
	}


}
