package com.sdyk.ai.crawler.specific.zbj.task.action;

import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.action.Action;
import one.rewind.io.requester.chrome.action.GeetestAction;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class GetProjectContactAction extends Action {

	// 投标按钮
	// #taskTabs > div > div:nth-child(1) > div > div.task-wantbid-launch > a
	public String bidButtonCssPath = "div.task-wantbid-launch > a";

	// 投标后获取电话联系方式按钮 #taskTabs > div > div:nth-child(1) > ul > li.oc-node-item.oc-node-now > div > div > p:nth-child(1) > a.info-item-btns-linker.anonymous-btn
	public String cellphontContactButtonAfterBidCssPath = ".info-item-btns-linker.anonymous-btn";

	// 获取花费信息
	public String spendMsgCssPath = ".ui-dialog-message > p > span";

	// 确认投标按钮
	public String confirmBidCssPath = ".ui-dialog-confirm > a";

	public GetProjectContactAction() {

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
	public boolean run(ChromeDriverAgent agent) {

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			logger.error(e);
		}

		//  #taskTabs > div > div:nth-child(1) > div > div.task-wantbid-launch > a
		// TODO 现在只能获取一种类型的需求联系方式，其他类型的需要用到该类型的账户进行投标。此账号无法投标其他类型，但是现在没有进行设置
		// A 判断该需求是否可以投标

		// A1 找投标按钮，获取投标状态
		WebElement bidButton;

		try {

			bidButton = agent.getDriver().findElement(By.cssSelector(bidButtonCssPath));

			// 投标已满
			String status = bidButton.getAttribute("title");

			if (status.contains("标数已满") || status.contains("暂停投标")) {
				logger.info("Project:{} bidder is full.");
				// 更新项目状态
				return false;
			}

		}
		// A2 已经投过标，或找不到投标按钮（不是投标类型的情况，如：比稿，众包，计件），或未登录
		catch (NoSuchElementException e) {

			// 如果已经投过标，直接点击获取电话联系方式
			try {
				clickCellphoneContactButton(agent);
			}
			catch (Exception ex) {
				logger.error("Can't click cellphone contact button. ", e);
			}

			return false;
		}

		// A.1 点击投标
		bidButton.click();

		// A2 将滚动条拖的顶层
		agent.getDriver().executeScript("scrollTo(0,1)");


		// 加延时等待加载
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			logger.error(e);
		}

		// B 判断是否拖拽验证 当日但用户/IP投标三次后 会出现投标验证码

		// B1 通过判断是否抛异常来确定是否需要验证
		try {

			agent.getDriver().findElement(By.cssSelector(".geetest_panel_next"));

			logger.info("Geetest validator presents. ");

			// body > div.geetest_panel.geetest_wind > div.geetest_panel_box.geetest_panelshowslide > div.geetest_panel_next

			//geetest_panel_error_content
			// 执行拖拽操作
			GeetestAction action = new GeetestAction();

			action.geetestResetTipCssPath = ".geetest_panel_error_content";

			action.geetestRefreshTooManyErrorCssPath = ".geetest_panel_error_code";

			action.geetestSuccessMsgCssPath = ".ui-dialog.newbid-bid-dialog";

			action.run(agent);

		}
		// 没出现极验测试
		catch (NoSuchElementException e) {
			logger.info("No geetest validator, continue.");
		}
		// 等待 投标提示框
		try {
			Thread.sleep(5000);
		} catch (Exception e) {
			logger.error(e);
		}

		// C 取得投标花费（猪币）
		// body > div:nth-child(21) > div.ui-dialog.newbid-bid-dialog > div > div.ui-dialog-container > div.ui-dialog-message > p > span
		double cost = Double.parseDouble(agent.getDriver()
				.findElement(By.cssSelector(spendMsgCssPath))
				.getText());

		logger.info("cost : {}", cost);

		// D 确认投标
		agent.getElementWait(confirmBidCssPath).click();

		// E 点击 电话联系
		try {
			Thread.sleep(15000);
			clickCellphoneContactButton(agent);
		} catch (Exception e) {
			logger.error(e);
		}

		// G 点击关闭 body > div.pupzhenshi-phones > div > div.pup-close
		/*agent.getDriver().findElement(By.cssSelector("body > div.pupzhenshi-phones > div > div.pup-close")).click();*/

		// 继续跟进
		agent.getDriver().findElement(By.cssSelector("#taskTabs > div > div > ul > li.oc-node-item.oc-node-now > div > div > div > button.oc-btn-dark.j-follow-up-countdown.already-contact-btn")).click();

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// 下拉栏选择
		Select select = new Select(agent.getDriver()
				.findElement(By.cssSelector("#aide-form-type1 > div > dl > dt > select")));
		select.selectByIndex(1);

		// 单选框
		agent.getDriver().findElement(By.cssSelector("#aide-form-type1 > div > dl > dd:nth-child(5) > span:nth-child(2) > label")).click();
		agent.getDriver().findElement(By.cssSelector("#aide-form-type1 > div > dl > dd:nth-child(7) > span:nth-child(2) > label")).click();

		// 确定
		agent.getDriver().findElement(By.cssSelector("body > div.arale-dialog-1_3_0 > div.ui-dialog.rd-dialog-ui > div > div.ui-dialog-container > div.ui-dialog-operation > div.ui-dialog-confirm > a")).click();
		return true;
	}

	private void clickCellphoneContactButton(ChromeDriverAgent agent) throws Exception {

		agent.getDriver()
				.findElement(By.cssSelector(cellphontContactButtonAfterBidCssPath))
				.click();

		// 等待 手机号对话框 加载
		Thread.sleep(5000);
	}

}
