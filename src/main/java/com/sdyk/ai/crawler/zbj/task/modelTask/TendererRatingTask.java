package com.sdyk.ai.crawler.zbj.task.modelTask;

import com.sdyk.ai.crawler.zbj.model.TendererRating;
import com.sdyk.ai.crawler.zbj.task.Task;
import com.sdyk.ai.crawler.zbj.task.scanTask.ScanTask;
import com.sdyk.ai.crawler.zbj.util.StatManager;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import one.rewind.txt.DateFormatUtil;

import javax.print.Doc;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 雇主评价
 */
public class TendererRatingTask extends ScanTask {

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
		this.setBuildDom();

		this.addDoneCallback(() -> {

			try {

				List<Task> tasks = new ArrayList<>();
				Document doc = getResponse().getDoc();

				// 防止数据为空
				if (!doc.select("#evaluation > div > div").text().contains("该雇主还未收到过评价")) {


					// 每页中的评价数
					for (int i = 1; i <= doc.select("#evaluation > div > div.panel-content > ul >li").size(); i++) {

						tendererRating = new TendererRating(getUrl());

						tendererRating.tenderer_url = getUrl().split("\\?")[0];

						// 每个评价
						ratingData(doc, i);

						// 入库
						try {

							tendererRating.insert();
						} catch (Exception e) {
							logger.error("Error insert: {}, ", e);
						}
					}

					// 翻页
					Task t = pageTurn(page);

					if (t != null) {
						t.setPriority(Priority.LOW);
						tasks.add(t);
					}
				}

				for (Task t : tasks) {
					t.setBuildDom();
					ChromeDriverRequester.getInstance().submit(t);
				}
			}catch (Exception e) {
				logger.error(e);
			}
		});
	}

	/**
	 *
	 * @param i
	 */
	public void ratingData(Document doc, int i) {

		tendererRating.facilitator_name =
				shareData(doc,"#evaluation > div > div.panel-content > ul > li:nth-child("+ i +") > div.evaluation-item-row.evaluation-item-from > span.who > a")
						.text();

		tendererRating.facilitator_url =
				shareData(doc,"#evaluation > div > div.panel-content > ul > li:nth-child("+ i +") > div.evaluation-item-row.evaluation-item-from > span.who > a")
						.attr("href");

		tendererRating.maluation =
				shareData(doc, "#evaluation > div > div.panel-content > ul > li:nth-child("+ i +") > div.evaluation-item-row.evaluation-item-text > p")
						.text();

		tendererRating.maluation_tag =
				shareData(doc,"#evaluation > div > div.panel-content > ul > li:nth-child("+ i +") > div.evaluation-item-row.evaluation-item-tags.clearfix")
						.text();
		try {
			tendererRating.maluation_time =
					DateFormatUtil.parseTime(
							shareData(doc,"#evaluation > div > div.panel-content > ul > li:nth-child("+ i +") > div.evaluation-item-row.evaluation-item-from > span.when")
									.text());

		} catch (ParseException e) {
			logger.error("Error: {}, ", e);
		}
		try {
			tendererRating.pay_timeliness_num =
					shareData(doc,"#evaluation > div > div.panel-content > ul > li:nth-child("+ i +") > div.evaluation-item-row.evaluation-item-scores.clearfix > div.scores-item.scores-intime > span.stars-group > i")
							.size();
			tendererRating.work_happy_num =
					shareData(doc,"#evaluation > div > div.panel-content > ul > li:nth-child("+ i +") > div.evaluation-item-row.evaluation-item-scores.clearfix > div.scores-item.scores-delight > span.stars-group > i")
							.size();

		} catch (NoSuchElementException e) {
			tendererRating.pay_timeliness_num = 0;
			tendererRating.work_happy_num = 0;
		}
	}

	/**
	 * 为ratingData方法提供WeElement
	 *
	 * @param doc
	 * @param path
	 * @return
	 */
	public Elements shareData(Document doc, String path) {
		return doc.select(path);
	}

	/**
	 * 翻页
	 * @param page
	 * @return
	 */
	public Task pageTurn(int page) {

		// 判断是否翻页
		if (pageTurning("#evaluation > div > div.pagination-wrapper > div > ul", page)) {

			Task t = null;
			try {
				t = new TendererRatingTask("https://home.zbj.com/"
						+ this.getParamString("webId"), ++page, this.getParamString("webId"));
				return t;
			} catch (MalformedURLException | URISyntaxException e) {
				logger.error("Error extract url: {}, ", getUrl(), e);
			}
		}
		return null;
	}
}
