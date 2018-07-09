package com.sdyk.ai.crawler.specific.zbj.task.action;

import com.sdyk.ai.crawler.model.witkey.ServiceProvider;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.Action;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;

public class GetServiceProviderContactAction extends Action {

	ServiceProvider serviceProvider;

	public GetServiceProviderContactAction(ServiceProvider serviceProvider) {
		this.serviceProvider = serviceProvider;
	}

	// 点击电话联系
	public String cellPhoneButton = "body > div.shop-fixed-im.sidebar-show > div.shop-fixed-im-hover.shop-customer.j-shop-fixed-im > div.shop-fix-im-time.shop-fix-im-mobile > a";

	// 确认获取电话
	public String getCellPhone = "body > div.pup-consult-my-phone > div > div.get-privacy-phone-btn";
	// 获取电话
	public String cellPhone = "body > div.pup-consult-privacy-phone > div > div.pup-top-privacy-phone-box > span";

	// 关闭按钮
	public String colse = "body > div.pup-consult-privacy-phone > div > div.pup-close";

	public boolean run(ChromeDriverAgent agent) {

		// 点击获取电话号码
		agent.getDriver().findElement(By.cssSelector(cellPhoneButton)).click();

		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// 确认获取
		try {
			agent.getDriver().findElement(By.cssSelector(getCellPhone)).click();
		} catch (NoSuchElementException e) {}

		// 得到电话号码
		String phone = agent.getDriver().findElement(By.cssSelector(cellPhone)).getText();

		if (phone.contains("-")) {
			serviceProvider.telephone = phone;
		} else {
			serviceProvider.cellphone = phone;
		}

		// 关闭
		agent.getDriver().findElement(By.cssSelector(colse)).click();

		return true;
	}



}
