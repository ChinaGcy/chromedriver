package com.sdyk.ai.crawler.zbj.task;

import com.sdyk.ai.crawler.zbj.util.StringUtil;
import com.sdyk.ai.crawler.zbj.model.Case;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 乙方项目详情
 */
public class CaseTask extends Task {

	Case ca;

	public CaseTask(String url) throws MalformedURLException, URISyntaxException {
		super(url);
	}

	public List<Task> postProc(WebDriver driver) {

		String src = getResponse().getText();

		ca = new Case(getUrl());

		if (!getUrl().contains("http://shop.tianpeng.com")) {
			// 猪八戒页面：http://shop.zbj.com/7523816/sid-696012.html
			try {
				pageOne(src, driver);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		else {
			// 天蓬网页面2：http://shop.tianpeng.com/17773550/sid-1126164.html
			pageTwo(src, driver);
		}
		// 以下是两个页面共有的信息
		// 二进制文件下载
		String description_src = driver.findElement(By.cssSelector("#J-description")).getAttribute("innerHTML");
		try {
			ca.description = download(description_src);
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			ca.insert();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new ArrayList<Task>();
	}

	/**
	 * 获取猪八戒服务价格预算
	 * @param driver
	 */
	public void budgetZBJ(WebDriver driver) {

		try {
			double[] budget = StringUtil.budget_all(driver,
					"body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-price-warp.yahei.clearfix.qrcode-version > div.price-with-qrcode > dl.price-panel.app-price-panel.hot-price > dd > span.price",
					"");
			if (budget[0] == 0.00 && budget[1] == 0.00) {
				budget = StringUtil.budget_all(driver,
						"body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-price-warp.yahei.clearfix.qrcode-version > div.price-with-qrcode.no-app-price > dl:nth-child(1) > dd > span.price",
						"");
				ca.budget_lb = budget[0];
				ca.budget_up = budget[1];
			}
			else {
				ca.budget_lb = budget[0];
				ca.budget_up = budget[1];
			}
		}
		catch (Exception e) {
			logger.error("budget error {}", e);
		}
	}

	/**
	 * 获取天蓬网服务价格预算
	 * @param driver
	 */
	public void budgetTPW(WebDriver driver) {

		try {
			double[] budget = StringUtil.budget_all(driver,
					"body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-price-warp.yahei.clearfix.qrcode-version > div > dl:nth-child(1) > dd > span.price",
					"");
			ca.budget_lb = budget[0];
			ca.budget_up = budget[1];
		}
		catch (Exception e) {
			ca.budget_lb = 0.00;
			ca.budget_up = 0.00;
		}
	}

	/**
	 * 猪八戒服务页面
	 * @param src 页面源
	 * @param driver
	 */
	public void pageOne (String src, WebDriver driver) {

		ca.user_id = getUrl().split("/")[3];

		ca.title = getString(driver,
				"body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > h2",
				"");

		ca.cycle = getString(driver,
				"body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-other-number.clearfix > div.service-complate-time > strong",
				"");

		// 价格预算
		budgetZBJ(driver);

		ca.response_time = getString(driver,
				"body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-other-number.clearfix > div.service-respond-time > div > strong",
				"");

		ca.service_attitude = getDouble(driver,
				"body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-star-warp.clearfix > ul > li.first > strong",
				"");

		ca.work_speed = getDouble(driver,
				"body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-star-warp.clearfix > ul > li:nth-child(2) > strong",
				"");

		ca.complete_quality = getDouble(driver,
				"body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-star-warp.clearfix > ul > li:nth-child(3) > strong",
				"");

		ca.rating = getFloat(driver,
				"body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-star-warp.clearfix > div.service-star-box > div.service-star-score",
				"");

		ca.rate_num = getInt(driver,
				"body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-star-warp.clearfix > div.service-star-box > div.service-comment-count > em",
				"");

		// 获取服务描述
		String caseTask_des = "";
		try {
			caseTask_des = getString(driver,
					"#j-service-tab > div.service-tab-content.ui-switchable-content > div.service-tab-item.service-detail.ui-switchable-panel > ul.service-property",
					"");
		} catch (NoSuchElementException e) { }

		Pattern pattern_tags = Pattern.compile(".*行业.*：(?<T>.+?)\\s+");
		Matcher matcher_tags = pattern_tags.matcher(caseTask_des);

		ca.type = caseTask_des;

		if (matcher_tags.find()) {
			ca.tags = matcher_tags.group("T");
		}
		else {
			ca.tags = "";
		}
	}

	/**
	 * 天棚网服务页面
	 * @param src
	 * @param driver
	 */
	public void pageTwo(String src, WebDriver driver) {

		ca.user_id = getUrl().split("/")[3];
		ca.title = driver.findElement(By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > h2"))
				.getText();

		budgetTPW(driver);

		String caseTask_des = driver.findElement(By.cssSelector("#j-service-tab > div.service-tab-content.ui-switchable-content > div.service-tab-item.service-detail.ui-switchable-panel > ul.service-property"))
				.getText();

		Pattern pattern_tags = Pattern.compile(".*行业.*：(?<T>.+?)\\s+");
		Matcher matcher_tags = pattern_tags.matcher(caseTask_des);

		ca.type = caseTask_des;

		if (matcher_tags.find()) {
			ca.tags = matcher_tags.group("T");
		}
	}
}
