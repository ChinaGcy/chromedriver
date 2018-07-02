package com.sdyk.ai.crawler.specific.mihuashi.action;

import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.Action;
import one.rewind.json.JSON;
import org.openqa.selenium.WebElement;

public class MihuashiLoginAction extends Action {

	ChromeDriverAgent agent;

	public String url = "https://www.mihuashi.com/login";

	// 用户名输入 path
	public String usernamePath = "#login-app > main > section > section.session__form-wrapper > section > div:nth-child(1) > input";

	// 密码输入 path
	public String passwordPath = "#login-app > main > section > section.session__form-wrapper > section > div:nth-child(2) > input";

	// 登陆按钮 path
	public String ButtonCssPath = "#login-app > main > section > section.session__form-wrapper > section > div:nth-child(3) > button";

	public String errorMsgReg = "账号或密码错误";
	transient boolean success = false;

	transient Account account;

	public MihuashiLoginAction(){ }

	public MihuashiLoginAction(Account account){

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

			//选择电话号登陆
			this.agent.getElementWait("#login-app > main > section > section.session__form-wrapper > ul > li:nth-child(2)").click();

			WebElement usernameInput = this.agent.getElementWait(this.usernamePath);
			usernameInput.clear();
			usernameInput.sendKeys(new CharSequence[]{this.account.getUsername()});

			WebElement passwordInput = this.agent.getElementWait(this.passwordPath);
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

			this.agent.getElementWait(this.ButtonCssPath).click();

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
