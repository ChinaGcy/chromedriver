package com.sdyk.ai.crawler.specific.itijuzi.action;

import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.Action;
import one.rewind.json.JSON;
import org.openqa.selenium.WebElement;

public class ItijuziLoginAction extends Action {

	public String url = "https://www.itjuzi.com/user/login";
	public String usernameCssPath = "#create_account_email";
	public String passwordCssPath = "#create_account_password";
	public String loginButtonCssPath = "#login_btn";

	public String errorMsgReg = "账号或密码错误";
	transient boolean success = false;

	transient Account account;

	public ItijuziLoginAction(){ }

	public ItijuziLoginAction(Account account){

		this.account = account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Account getAccount() {
		return this.account;
	}


	boolean fillUsernameAndPassword( ChromeDriverAgent agent ) {

		try {
			agent.getUrl(this.url);
			agent.waitPageLoad(this.url);

			System.out.println(account.username);

			WebElement usernameInput = agent.getElementWait(this.usernameCssPath);
			usernameInput.clear();
			usernameInput.sendKeys(new CharSequence[]{this.account.getUsername()});

			WebElement passwordInput = agent.getElementWait(this.passwordCssPath);
			passwordInput.clear();
			passwordInput.sendKeys(new CharSequence[]{this.account.getPassword()});

			Thread.sleep(1000L);

			return true;

		} catch (Exception var3) {

			logger.error(var3);

			return false;

		}
	}

	boolean clickSubmitButton( ChromeDriverAgent agent ) {
		try {

			agent.getElementWait(this.loginButtonCssPath).click();

			Thread.sleep(5000L);

		} catch (Exception var2) {

			logger.error(var2);

			return false;

		}

		return !agent.getDriver().getPageSource().matches(this.errorMsgReg);
	}

	public boolean run(ChromeDriverAgent agent) {

		if (this.fillUsernameAndPassword( agent )) {

			if (this.clickSubmitButton( agent )) {

				this.success = true;

			}

		}
		return this.success;
	}

	public String toJSON() {
		return JSON.toJson(this);
	}


}
