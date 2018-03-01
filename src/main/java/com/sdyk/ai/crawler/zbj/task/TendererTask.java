package com.sdyk.ai.crawler.zbj.task;

import com.sdyk.ai.crawler.zbj.ChromeDriverWithLogin;
import com.sdyk.ai.crawler.zbj.model.Tenderer;
import com.sdyk.ai.crawler.zbj.model.TendererRating;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.tfelab.db.Refacter;
import org.tfelab.io.requester.Requester;
import org.tfelab.io.requester.account.AccountWrapper;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;
import org.tfelab.io.requester.chrome.ChromeDriverRequester;
import org.tfelab.txt.DateFormatUtil;
import org.tfelab.txt.StringUtil;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 雇主详情
 */
public class TendererTask extends Task {

	public TendererTask(String url) throws MalformedURLException, URISyntaxException {
		super(url);
		this.priority = Priority.high;
	}

	public List<Task> postProc(WebDriver driver) throws ParseException, MalformedURLException, URISyntaxException {

		List<Task> tasks = new ArrayList<>();

		Tenderer tenderer = new Tenderer(getUrl());

		tenderer.website_id = getUrl().split("com/")[1];
		tenderer.name = getString(driver,
				"#utopia_widget_1 > div > div.topinfo-top > div > h2",
				"");
		try {
			tenderer.area = getString(driver,
					"#utopia_widget_1 > div > div.topinfo-top > div > div > span.location",
					"");
		}
		catch (NoSuchElementException e) {
			tenderer.area = "";
		}
		tenderer.login_time =
				DateFormatUtil.parseTime(driver.findElement(By.cssSelector("#utopia_widget_1 > div > div.topinfo-top > div > div > span.last-login"))
				.getText());

		tenderer.trade_num =
				Integer.parseInt(driver.findElement(By.cssSelector("#utopia_widget_1 > div > div.topinfo-bottom > div > div > div.statistics-item.statistics-trade > div.statistics-item-val > strong"))
				.getText().replaceAll("-", "0"));

		tenderer.industry =
				driver.findElement(By.cssSelector("#utopia_widget_1 > div > div.topinfo-bottom > div > div > div:nth-child(3) > div.statistics-item-val"))
				.getText();

		tenderer.tender_type =
				driver.findElement(By.cssSelector("#utopia_widget_1 > div > div.topinfo-bottom > div > div > div.statistics-item.statistics-time > div.statistics-item-val"))
				.getText();

		tenderer.enterprise_size =
				driver.findElement(By.cssSelector("#utopia_widget_1 > div > div.topinfo-bottom > div > div > div.statistics-item.statistics-scale > div.statistics-item-val"))
				.getText();

		tenderer.description =
				driver.findElement(By.cssSelector("#utopia_widget_4 > div > div > p")).getText();

		tenderer.demand_forecast =
				driver.findElement(By.cssSelector("#utopia_widget_5 > div > div > h5")).getText();

		tenderer.total_spending =
				Double.parseDouble(driver.findElement(By.cssSelector("#utopia_widget_1 > div > div.topinfo-bottom > div > div > div.statistics-item.statistics-pay > div.statistics-item-val > strong"))
				.getText().replaceAll(",", ""));


		// 添加projectTask
		tasks.add(TendererOrderTask.generateTask(getUrl(), 1 ,tenderer.website_id));

		// 评价任务
		tasks.add(TendererRatingTask.generateTask(getUrl(), 1, tenderer.website_id));

		try {

			tenderer.insert();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return tasks;
	}

}
