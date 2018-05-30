package com.sdyk.ai.crawler.specific.zbj.task.action;

import com.sdyk.ai.crawler.model.Project;
import io.netty.handler.codec.http.HttpResponse;
import net.lightbody.bmp.filters.ResponseFilter;
import net.lightbody.bmp.util.HttpMessageContents;
import net.lightbody.bmp.util.HttpMessageInfo;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.ChromeAction;
import one.rewind.io.requester.chrome.action.LoginWithGeetestAction;
import org.openqa.selenium.By;

import java.util.HashMap;
import java.util.Map;

public class GetProjectContactAction extends ChromeAction {

	Project project;

	public GetProjectContactAction(Project project) {
		this.project = project;
	}

	/**
	 *  继续跟进需要等待150s后才可以继续下面的步骤
	 *  放弃订单无法投标
	 *  30分钟内选择，否则无法投标
	 *  连续投标三次有拖拽验证，6次拖拽失败重试
	 *
	 *  拖拽按钮  body > div.geetest_panel.geetest_wind > div.geetest_panel_box.geetest_panelshowslide > div.geetest_panel_next > div > div.geetest_wrap > div.geetest_slider.geetest_ready > div.geetest_slider_button
	 *  图片 body > div.geetest_panel.geetest_wind > div.geetest_panel_box.geetest_panelshowslide > div.geetest_panel_next > div > div.geetest_wrap > div.geetest_widget > div > a > div.geetest_canvas_img.geetest_absolute > canvas
	 *  刷新 body > div.geetest_panel.geetest_wind > div.geetest_panel_box.geetest_panelshowslide > div.geetest_panel_next > div > div.geetest_panel > div > a.geetest_refresh_1
	 */
	public void run() {

		ChromeDriverAgent agent = this.agent;

		// A 点击投标
		agent.getElementWait("#taskTabs > div > div:nth-child(1) > div > div.task-wantbid-launch > a").click();

		// 加延时等待加载
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			logger.error(e);
		}

		// B 判断是否拖拽验证 当日但用户/IP投标三次后 会出现投标验证码

		// body > div.geetest_panel.geetest_wind > div.geetest_panel_box.geetest_panelshowslide > div.geetest_panel_next
		// 执行拖拽操作
		LoginWithGeetestAction action = new LoginWithGeetestAction();
		action.geetestWindowCssPath = "body > div.geetest_panel.geetest_wind > div.geetest_panel_box.geetest_panelshowslide > div.geetest_panel_next > div > div.geetest_wrap > div.geetest_widget > div > a > div.geetest_canvas_img.geetest_absolute > canvas";
		action.geetestSliderButtonCssPath = "body > div.geetest_panel.geetest_wind > div.geetest_panel_box.geetest_panelshowslide > div.geetest_panel_next > div > div.geetest_wrap > div.geetest_slider.geetest_ready > div.geetest_slider_button";
		action.geetestResetTipCssPath = "body > div.geetest_panel.geetest_wind > div.geetest_panel_box > div.geetest_panel_error > div.geetest_panel_error_content";
		action.geetestRefreshTooManyErrorCssPath = "body > div.geetest_panel.geetest_wind > div.geetest_panel_box > div.geetest_panel_error > div.geetest_panel_error_code";
		action.geetestContentCssPath = "body > div.geetest_panel.geetest_wind > div.geetest_panel_box.geetest_panelshowslide > div.geetest_panel_next";
		action.run();

		// C 取得投标花费（猪币）
		// body > div:nth-child(21) > div.ui-dialog.newbid-bid-dialog > div > div.ui-dialog-container > div.ui-dialog-message > p > span
		project.spend = Double.parseDouble(this.agent.getDriver()
				.findElement(By.cssSelector("body > div > div.ui-dialog.newbid-bid-dialog > div > div.ui-dialog-container > div.ui-dialog-message > p > span"))
				.getText());

		// D 确认投标
		this.agent.getElementWait("body > div > div.ui-dialog.newbid-bid-dialog > div > div.ui-dialog-container > div.ui-dialog-operation > div.ui-dialog-confirm > a")
				.click();

		// 加延时更新
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			logger.error(e);
		}

		// E 点击 电话联系
		agent.getDriver()
				.findElement(By.cssSelector("#taskTabs > div > div > ul > li.oc-node-item.oc-node-now > div > div > p > a.info-item-btns-linker.anonymous-btn > span > span.button-text"))
				.click();

		// 等待 手机号对话框 加载
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			logger.error(e);
		}

		// F 点击获取手机号按钮
		agent.getDriver()
				.findElement(By.cssSelector("body > div.pupzhenshi-phones > div > a"))
				.click();

		// 等待获取手机号
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			logger.error(e);
		}

		// 获取手机号
		project.cellphone = agent.getDriver().findElement(By.cssSelector("body > div.pup-servicedry-phone > div > div.min-phone > span")).getText();

		String url = "";

		// G 点击关闭
		agent.getDriver().findElement(By.cssSelector("body > div.pup-servicedry-phone > div > div.pup-close")).click();

		project.update();

		/*// 继续跟进
		chromeDriverAgent.getDriver().findElement(By.cssSelector("#taskTabs > div > div > ul > li.oc-node-item.oc-node-now > div > div > div > button.oc-btn-dark.j-follow-up-countdown.already-contact-btn")).click();

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// 下拉栏选择
		Select select = new Select(chromeDriverAgent.getDriver()
				.findElement(By.cssSelector("#aide-form-type1 > div > dl > dt > select")));
		select.selectByIndex(1);

		// 单选框
		chromeDriverAgent.getDriver().findElement(By.cssSelector("#aide-form-type1 > div > dl > dd:nth-child(5) > span:nth-child(2) > label")).click();
		chromeDriverAgent.getDriver().findElement(By.cssSelector("#aide-form-type1 > div > dl > dd:nth-child(7) > span:nth-child(2) > label")).click();

		// 确定
		chromeDriverAgent.getDriver().findElement(By.cssSelector("body > div.arale-dialog-1_3_0 > div.ui-dialog.rd-dialog-ui > div > div.ui-dialog-container > div.ui-dialog-operation > div.ui-dialog-confirm > a")).click();*/
	}

}
