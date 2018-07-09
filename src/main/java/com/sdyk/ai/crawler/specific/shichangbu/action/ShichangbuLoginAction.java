package com.sdyk.ai.crawler.specific.shichangbu.action;

import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.Action;
import one.rewind.json.JSON;
import org.openqa.selenium.WebElement;

public class ShichangbuLoginAction extends Action {

	ChromeDriverAgent agent;

	public String url = "http://www.shichangbu.com/member.php?mod=logging&action=login";
	public String usernameCssPath = "div > div > form > div > div > div > input";
	public String passwordCssPath = "div > div > form > div > div.mb-bd > div:nth-child(3) > input";
	public String loginButtonCssPath = "div > div > form > div > div.mb-bd > div.fmg.fmg-submit > button";

	public String errorMsgReg = "账号或密码错误";
	transient boolean success = false;

	transient Account account;

	public ShichangbuLoginAction(){ }

	public ShichangbuLoginAction(Account account){

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
