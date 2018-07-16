package com.sdyk.ai.crawler;

import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.Action;
import one.rewind.json.JSON;
import org.openqa.selenium.WebElement;

public class LoginInAction extends Action {

	public String url ;
	public String usernameCssPath;
	public String passwordCssPath;
	public String loginButtonCssPath;
	public String typePath;

	public String errorMsgReg = "账号或密码错误";

	transient boolean success = false;

	transient Account account;

	public LoginInAction(){ }

	public LoginInAction(String url, String usernameCssPath, String passwordCssPath, String loginButtonCssPath, String typePath, Account account){

		this.url = url;
		this.usernameCssPath = usernameCssPath;
		this.passwordCssPath = passwordCssPath;
		this.loginButtonCssPath = loginButtonCssPath;
		this.typePath = typePath;
		this.account = account;

	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Account getAccount() {
		return this.account;
	}

	boolean fillUsernameAndPassword(ChromeDriverAgent agent) {

		try {

			if( this.typePath != null ){
				agent.getElementWait(this.typePath).click();
				Thread.sleep(2000);
			}

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

	boolean clickSubmitButton(ChromeDriverAgent agent) {
		try {

			agent.getElementWait(this.loginButtonCssPath).click();

			Thread.sleep(3000L);

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
