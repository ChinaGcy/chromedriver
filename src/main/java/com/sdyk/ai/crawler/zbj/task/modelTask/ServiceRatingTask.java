package com.sdyk.ai.crawler.zbj.task.modelTask;


import com.sdyk.ai.crawler.zbj.exception.IpException;
import com.sdyk.ai.crawler.zbj.model.SupplierRating;
import com.sdyk.ai.crawler.zbj.proxypool.ProxyReplace;
import com.sdyk.ai.crawler.zbj.task.Task;
import com.sdyk.ai.crawler.zbj.task.scanTask.ScanTask;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.tfelab.txt.DateFormatUtil;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class ServiceRatingTask extends ScanTask {

	SupplierRating serviceRating;

	// http://shop.zbj.com/evaluation/evallist-uid-7791034-type-1-isLazyload-0-page-1.html
	public static ServiceRatingTask generateTask(String url, int page) {

		String url_ = url + page + ".html";

		try {
			ServiceRatingTask t = new ServiceRatingTask(url_, page);
			return t;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return null;
	}

	public ServiceRatingTask(String url, int page) throws MalformedURLException, URISyntaxException {
		super(url);
		this.setParam("page", page);
	}

	/**
	 *
	 * @param driver
	 * @return
	 */
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

		int page = this.getParamInt("page");

		// 判断当前页面有多少评论
		int size = 0;

		try {
			size = driver.findElement(By.cssSelector("#userlist > div.moly-poc.user-fols.ml20.mr20"))
					.findElements(By.className("user-information")).size();
		} catch (NoSuchElementException e) {
			// 页面为空，size = 0 ，不采集数据
		}

		for (int i = 1; i <= size; i++) {

			serviceRating = new SupplierRating(getUrl());

			// 每个评价
			ratingData(driver, i);

			try {
				serviceRating.insert();
			} catch (Exception e) {
				logger.error("Error insert: {}, ", e);
			}
		}

		// 翻页
		if (pageTurning(driver, "#userlist > div.pagination > ul", page)) {
			Task task = generateTask("http://shop.zbj.com/evaluation/evallist-uid-"+ getUrl().split("-")[2] +"-type-1-isLazyload-0-page-", ++page);
			tasks.add(task);
		}

		return tasks;
	}

	/**
	 *
	 * @param driver
	 * @param i
	 */
	public void ratingData(WebDriver driver, int i) {

		serviceRating.service_supplier_id = getUrl().split("-")[2];
		String[] ss = driver.findElement(By.cssSelector("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dt > img"))
				.getAttribute("src").split("/");
		serviceRating.tenderer_id = ss[3].substring(1)+ss[4]+ss[5]+ss[6].split("_")[2].split(".jpg")[0];

		serviceRating.tenderer_url = "http://home.zbj.com/" + serviceRating.tenderer_id;

		serviceRating.project_url = driver.findElement(By.cssSelector("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dd:nth-child(2) > p.name-tit > a"))
				.getAttribute("href");

		serviceRating.tenderer_name = driver.findElement(By.cssSelector("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dd:nth-child(2) > p.name-tit"))
				.getText().split("成交价格：")[0];
		serviceRating.spend = Double.parseDouble(driver.findElement(By.cssSelector("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dd:nth-child(2) > p.name-tit"))
				.getText().split("成交价格：")[1].replaceAll("元", ""));
		serviceRating.description = driver.findElement(By.cssSelector("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dd:nth-child(2) > p:nth-child(2) > span"))
				.getText();
		try {
			serviceRating.tags = driver.findElement(By.cssSelector("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dd:nth-child(2) > p.yingx"))
					.getText().split("印象：")[1];

		} catch (NoSuchElementException e) {
			serviceRating.tags ="";
		}

		try {
			serviceRating.rating_time = DateFormatUtil.parseTime(driver.findElement(By.cssSelector("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dd.mint > p")).getText());
		} catch (ParseException e) {
			logger.error("serviceRating  rating_time {}", e);
		}

	}
}
