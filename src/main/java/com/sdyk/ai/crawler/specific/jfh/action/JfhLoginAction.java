package com.sdyk.ai.crawler.specific.jfh.action;

import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.Action;
import one.rewind.json.JSON;
import org.openqa.selenium.WebElement;

public class JfhLoginAction extends Action {

	ChromeDriverAgent agent;

	// 定义登陆url
	public String url = "https://www.jfh.com/security/index";
	// 用户名输入 path
	public String usernameCssPath = "#j_loginid";
	// 密码输入 path
	public String passwordCssPath = "#j_password";
	// 登陆按钮 path
	public String loginButtonCssPath = "#submit_button";

	public String errorMsgReg = "账号或密码错误";
	transient boolean success = false;

	transient Account account;

	public JfhLoginAction(){ }

	public JfhLoginAction(Account account){

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

	public boolean run( ChromeDriverAgent agent ) {

		this.agent = agent;

		if (this.fillUsernameAndPassword()) {

			if (this.clickSubmitButton()) {

				this.success = true;

			}

		}
		return true;

	}

	public String toJSON() {
		return JSON.toJson(this);
	}

}
