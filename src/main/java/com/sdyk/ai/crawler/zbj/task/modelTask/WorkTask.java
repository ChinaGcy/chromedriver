package com.sdyk.ai.crawler.zbj.task.modelTask;

import com.sdyk.ai.crawler.zbj.exception.IpException;
import com.sdyk.ai.crawler.zbj.model.Work;
import com.sdyk.ai.crawler.zbj.proxypool.ProxyReplace;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sdyk.ai.crawler.zbj.task.Task;

/**
 * 案例详情
 */
public class WorkTask extends Task {

	Work work;

	public WorkTask(String url, String user_id) throws MalformedURLException, URISyntaxException {
		super(url);
		this.setParam("user_id",user_id);
	}

	public List<Task> postProc(WebDriver driver) {

		String src = getResponse().getText();
		List<Task> tasks = new ArrayList<>();

		// 判断是否被禁
		try {
			ProxyReplace.proxyWork(src, this);
		} catch (IpException e) {
			ProxyReplace.replace(this);
			return tasks;
		}

		work = new Work(getUrl());

		// 判断页面格式
		if (getUrl().contains("zbj")) {
			// 猪八戒
			pageOne(driver);
		}
		else {
			// 天棚网
			pageTwo(driver);
		}

		try {
			work.insert();
		} catch (Exception e) {
			logger.error("insert error for Work", e);
		}
		return tasks;
	}

	/**
	 * 猪八戒案例格式
	 * @param driver
	 */
	public void pageOne(WebDriver driver) {

		work.name = driver.findElement(By.cssSelector("body > div.det-bg.yahei > div.det-content.clearfix > div.det-head.fl > div"))
				.getText();
		work.user_id = this.getParamString("user_id");

		String src_ = getString(driver,
				"body > div.det-bg.yahei > div.det-content.clearfix > div.det-middle.clearfix > div.det-right.fr > div.det-middle-content > ul",
				"") + " ";
		Pattern pattern = Pattern.compile(".*客户名称：(?<T>.+?)\\s+");
		Pattern pattern_type = Pattern.compile(".*类型.*： ?(?<T>.+?)\\s+");
		Pattern pattern_field = Pattern.compile(".*行业.*： ?(?<T>.+?)\\s+");
		Matcher matcher = pattern.matcher(src_);
		Matcher matcher_type = pattern_type.matcher(src_);
		Matcher matcher_field = pattern_field.matcher(src_);

		if (matcher.find()) {
			work.tenderer_name = matcher.group("T");
		}
		if (matcher_type.find()) {
			work.type = matcher_type.group("T");
		}
		if (matcher_field.find()) {
			work.field = matcher_field.group("T");
		}

		// 下载二进制文件
		String description_src = driver.findElement(By.cssSelector("body > div.det-bg.yahei > div.det-content.clearfix > div.det-middle.clearfix > div.det-left.fl"))
				.getAttribute("innerHTML");

		work.description = download(description_src)
				.replace("<img src=\"https://t5.zbjimg.com/t5s/common/img/space.gif\">", "");

		// 价格
		if(driver.findElement(By.cssSelector("body > div.det-bg.yahei > div.det-content.clearfix > div.det-middle.clearfix > div.det-right.fr > div.det-middle-head > p.right-content"))
				.getText().contains("面议")) {
			work.pricee = 0.00;
		} else {
			work.pricee = Double.parseDouble(driver.findElement(By.cssSelector("body > div.det-bg.yahei > div.det-content.clearfix > div.det-middle.clearfix > div.det-right.fr > div.det-middle-head > p.right-content"))
					.getText().split("￥")[1]);
		}
	}

	/**
	 * 天棚网案例格式
	 * @param driver
	 */
	public void pageTwo(WebDriver driver) {

		try {

			work.user_id = this.getParamString("user_id");
			work.name = driver.findElement(By.cssSelector("body > div.tp-works-hd > div > div.tp-works-hd-left > div.works-title > h2"))
					.getText();
			work.tenderer_name = driver.findElement(By.cssSelector("body > div.tp-works-hd > div > div.tp-works-hd-left > div.works-info > p.works-info-customer > em"))
					.getText();
			work.pricee = Double.parseDouble(driver.findElement(By.cssSelector("body > div.tp-works-hd > div > div.tp-works-hd-left > div.works-info > p.works-info-amount > em"))
					.getText().replaceAll("¥", "").replaceAll(",", ""));
			work.tags = driver.findElement(By.cssSelector("body > div.tp-works-hd > div > div.tp-works-hd-left > ul")).getText();

			String description_src = driver.findElement(By.cssSelector("body > div.tp-works-bd > div > div.works-bd-content > div"))
					.getAttribute("innerHTML");
			// 二进制文件下载
			work.description = download(description_src)
					.replace("<img src=\"https://t5.zbjimg.com/t5s/common/img/space.gif\">", "");

		}catch (Exception e) {
			e.printStackTrace();
		}
	}


}
