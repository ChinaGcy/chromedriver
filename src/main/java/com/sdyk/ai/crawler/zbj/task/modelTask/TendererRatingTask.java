package com.sdyk.ai.crawler.zbj.task.modelTask;

import com.sdyk.ai.crawler.zbj.model.TendererRating;
import com.sdyk.ai.crawler.zbj.task.Task;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.tfelab.txt.DateFormatUtil;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 雇主评价
 */
public class TendererRatingTask extends Task {

	TendererRating tendererRating;

	public static TendererRatingTask generateTask(String url, int page, String webId) {

		TendererRatingTask t = null;
		String url_ = url+ "/?ep=" + page;
		try {
			t = new TendererRatingTask(url_, page, webId);
			return t;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return t;
	}

	public TendererRatingTask(String url, int page, String webId) throws MalformedURLException, URISyntaxException {
		super(url);
		this.setParam("page", page);
		this.setParam("webId", webId);
	}

	public List<Task> postProc(WebDriver driver) {

		List<Task> tasks = new ArrayList<>();

		// 防止数据为空
		if (!driver.findElement(By.cssSelector("#evaluation > div > div")).getText().contains("该雇主还未收到过评价")) {

			String src = getResponse().getText();

			int page = this.getParamInt("page");

			// 每页中的评价数
			for (int i = 0; i < driver.findElement(By.cssSelector("#evaluation > div > div.panel-content > ul")).findElements(By.tagName("li")).size(); i++) {

				tendererRating = new TendererRating(getUrl());

				tendererRating.tenderer_url = getUrl().split("\\?")[0];

				// 每个评价
				ratingData(driver, i);

				// 入库
				try {
					tendererRating.insert();
				} catch (Exception e) {
					logger.error("Error insert: {}, ", e);
				}
			}

			// 翻页
			Task t = pageTurn(driver, page);

			if (t != null) {
				t.setPrior();
				tasks.add(t);
			}
		}
		return tasks;
	}

	/**
	 *
	 * @param driver
	 * @param i
	 */
	public void ratingData(WebDriver driver, int i) {

		tendererRating.facilitator_name =
				shareData(driver,"#evaluation > div > div.panel-content > ul",i)
						.findElement(By.className("evaluation-item-from"))
						.findElement(By.tagName("a"))
						.getText();

		tendererRating.facilitator_url =
				shareData(driver,"#evaluation > div > div.panel-content > ul",i)
						.findElement(By.className("evaluation-item-from"))
						.findElement(By.tagName("a"))
						.getAttribute("href");

		tendererRating.maluation =
				shareData(driver,"#evaluation > div > div.panel-content > ul",i)
						.findElement(By.className("evaluation-item-text"))
						.getText();

		tendererRating.maluation_tag =
				shareData(driver,"#evaluation > div > div.panel-content > ul",i)
						.findElement(By.className("evaluation-item-tags"))
						.getText();
		try {
			tendererRating.maluation_time =
					DateFormatUtil.parseTime(
							shareData(driver,"#evaluation > div > div.panel-content > ul",i)
									.findElement(By.className("evaluation-item-from"))
									.findElement(By.className("when"))
									.getText());

		} catch (ParseException e) {
			logger.error("Error: {}, ", e);
		}
		try {
			tendererRating.pay_timeliness_num =
					shareData(driver,"#evaluation > div > div.panel-content > ul",i)
							.findElement(By.className("evaluation-item-scores"))
							.findElement(By.className("scores-intime"))
							.findElements(By.tagName("i"))
							.size();
			tendererRating.work_happy_num =
					shareData(driver,"#evaluation > div > div.panel-content > ul",i)
							.findElement(By.className("evaluation-item-scores"))
							.findElement(By.className("scores-delight"))
							.findElements(By.tagName("i"))
							.size();

		} catch (NoSuchElementException e) {
			tendererRating.pay_timeliness_num = 0;
			tendererRating.work_happy_num = 0;
		}
	}

	/**
	 * 为ratingData方法提供WeElement
	 *
	 * @param driver
	 * @param path
	 * @param i
	 * @return
	 */
	public WebElement shareData(WebDriver driver, String path, int i) {

		return driver.findElement(By.cssSelector(path))
				.findElements(By.tagName("li"))
				.get(i);
	}

	/**
	 * 翻页
	 * @param driver
	 * @param page
	 * @return
	 */
	public Task pageTurn(WebDriver driver, int page) {

		// 判断是否翻页
		if (pageTurning(driver,"#evaluation > div > div.pagination-wrapper > div > ul", page)) {

			Task t = null;
			try {
				t = new TendererRatingTask("http://home.zbj.com/"
						+ this.getParamString("webId"), ++page, this.getParamString("webId"));
				return t;
			} catch (MalformedURLException | URISyntaxException e) {
				logger.error("Error extract url: {}, ", getUrl(), e);
			}
		}
		return null;
	}
}
