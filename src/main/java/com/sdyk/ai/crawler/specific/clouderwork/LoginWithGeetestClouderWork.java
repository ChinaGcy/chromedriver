package com.sdyk.ai.crawler.specific.clouderwork;

import one.rewind.io.requester.account.Account;
import one.rewind.io.requester.chrome.action.LoginAction;
import one.rewind.json.JSON;
import org.openqa.selenium.WebElement;

public class LoginWithGeetestClouderWork extends LoginAction {

    public String url;
    public String usernameCssPath;
    public String passwordCssPath;
    public String loginButtonCssPath;
    public String errorMsgReg = "账号或密码错误";
    transient boolean success = false;
    transient Account account;

    public LoginWithGeetestClouderWork(){

    }

    public LoginWithGeetestClouderWork(Account account, String url, String usernameCssPath, String passwordCssPath, String loginButtonCssPath){
        this.account = account;
        this.url = url;
        this.usernameCssPath = usernameCssPath;
        this.passwordCssPath = passwordCssPath;
        this.loginButtonCssPath = loginButtonCssPath;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    boolean fillUsernameAndPassword() {

        try {
            this.agent.getUrl(this.url);
            this.agent.waitPageLoad(this.url);

            //米画师登陆操作时
            if( this.url.contains("mihuashi")) {

                //选择电话号登陆
                this.agent.getElementWait("#login-app > main > section > section.session__form-wrapper > ul > li:nth-child(2)").click();
            }

            WebElement usernameInput = this.agent.getElementWait(this.usernameCssPath);
            usernameInput.sendKeys(new CharSequence[]{this.account.getUsername()});
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
