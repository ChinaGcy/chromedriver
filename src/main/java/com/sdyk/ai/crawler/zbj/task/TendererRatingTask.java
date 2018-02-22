package com.sdyk.ai.crawler.zbj.task;

import com.sdyk.ai.crawler.zbj.model.Tenderer;
import com.sdyk.ai.crawler.zbj.model.TendererRating;
import db.Refacter;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.tfelab.txt.DateFormatUtil;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 雇主评价
 */
public class TendererRatingTask extends Task {

	public static TendererRatingTask generateTask(String url, int page, String webId) {

		TendererRatingTask t = null;
		String url_= url+ "/?ep=" + page;
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

	public List<Task> postProc(WebDriver driver) throws Exception {

		List<Task> tasks = new ArrayList<>();
		if (!driver.findElement(By.cssSelector("#evaluation > div > div")).getText().equals("该雇主还未收到过评价")) {

			String src = getResponse().getText();
			int page = this.getParamInt("page");

			for (int i=1; i <= driver.findElement(By.cssSelector("#evaluation > div > div.panel-content > ul")).findElements(By.tagName("li")).size(); i++) {

				TendererRating tendererRating = new TendererRating();


				tendererRating.tenderer_url = getUrl().split("\\?")[0];

				tendererRating.facilitator_name = driver.findElement(By.cssSelector("#evaluation > div > div.panel-content > ul > li:nth-child(" + i + ") > div.evaluation-item-row.evaluation-item-from > span.who > a"))
						.getText();
				tendererRating.facilitator_url = driver.findElement(By.cssSelector("#evaluation > div > div.panel-content > ul > li:nth-child(" + i + ") > div.evaluation-item-row.evaluation-item-from > span.who > a"))
						.getAttribute("href");
				tendererRating.maluation = driver.findElement(By.cssSelector("#evaluation > div > div.panel-content > ul > li:nth-child(" + i + ") > div.evaluation-item-row.evaluation-item-text"))
						.getText();
				tendererRating.maluation_tag = driver.findElement(By.cssSelector("#evaluation > div > div.panel-content > ul > li:nth-child(" + i + ") > div.evaluation-item-row.evaluation-item-tags.clearfix"))
						.getText();
				tendererRating.maluation_time = DateFormatUtil.parseTime(driver.findElement(By.cssSelector("#evaluation > div > div.panel-content > ul > li:nth-child(" + i + ") > div.evaluation-item-row.evaluation-item-from > span.when"))
						.getText());
				try {
					tendererRating.pay_timeliness_num = driver.findElement(By.cssSelector("#evaluation > div > div.panel-content > ul > li:nth-child(" + i + ") > div.evaluation-item-row.evaluation-item-scores.clearfix > div.scores-item.scores-intime > span.stars-group"))
							.findElements(By.tagName("i")).size();
					tendererRating.work_happy_num = driver.findElement(By.cssSelector("#evaluation > div > div.panel-content > ul > li:nth-child(" + i + ") > div.evaluation-item-row.evaluation-item-scores.clearfix > div.scores-item.scores-delight > span.stars-group"))
							.findElements(By.tagName("i")).size();

				} catch (NoSuchElementException e) {
					tendererRating.pay_timeliness_num = 0;
					tendererRating.work_happy_num = 0;
				}
				tendererRating.insert();
			}

				if (driver.findElement(By.cssSelector("#evaluation > div > div.panel-content > ul")).
					findElements(By.tagName("li")).size() == 5) {
				Task t = new TendererRatingTask("http://home.zbj.com/"
						+ this.getParamString("webId"), ++page, this.getParamString("webId"));
				if (t != null) {
					t.setPrior();
					tasks.add(t);
				}
			}


		}

		return tasks;
	}

	public static void main(String[] args) throws Exception {
		Refacter.dropTable(TendererRating.class);
		Refacter.createTable(TendererRating.class);
	}

}
