package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.model.TendererRating;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.ScanTask;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.NoSuchElementException;
import one.rewind.txt.DateFormatUtil;

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

	public static TendererRatingTask generateTask(String url, int page, String userId) {

		TendererRatingTask t = null;
		String url_ = url+ "/?ep=" + page;
		try {
			t = new TendererRatingTask(url_, page, userId);
			return t;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return t;
	}

	public TendererRatingTask(String url, int page, String userId) throws MalformedURLException, URISyntaxException {
		super(url);
		this.setParam("page", page);
		this.setParam("userId", userId);

		this.addDoneCallback(() -> {

			try {

				List<com.sdyk.ai.crawler.task.Task> tasks = new ArrayList<>();
				Document doc = getResponse().getDoc();

				// 防止数据为空
				if (!doc.select("#evaluation > div > div").text().contains("该雇主还未收到过评价")) {


					// 每页中的评价数
					for (int i = 1; i <= doc.select("#evaluation > div > div.panel-content > ul >li").size(); i++) {

						tendererRating = new TendererRating(getUrl() + "&" + i);

						tendererRating.user_id = getUrl().split("\\?")[0];

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
					com.sdyk.ai.crawler.task.Task t = pageTurn(page);

					if (t != null) {
						t.setPriority(Priority.LOW);
						tasks.add(t);
					}
				}

				for (com.sdyk.ai.crawler.task.Task t : tasks) {
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

		/*tendererRating.facilitator_name =
				shareData(doc,"#evaluation > div > div.panel-content > ul > li:nth-child("+ i +") > div.evaluation-item-row.evaluation-item-from > span.who > a")
						.text();*/

		/*tendererRating.facilitator_url =
				shareData(doc,"#evaluation > div > div.panel-content > ul > li:nth-child("+ i +") > div.evaluation-item-row.evaluation-item-from > span.who > a")
						.attr("href");*/

		tendererRating.content =
				shareData(doc, "#evaluation > div > div.panel-content > ul > li:nth-child("+ i +") > div.evaluation-item-row.evaluation-item-text > p")
						.text();

		tendererRating.tags =
				shareData(doc,"#evaluation > div > div.panel-content > ul > li:nth-child("+ i +") > div.evaluation-item-row.evaluation-item-tags.clearfix")
						.text();
		try {
			tendererRating.pubdate =
					DateFormatUtil.parseTime(
							shareData(doc,"#evaluation > div > div.panel-content > ul > li:nth-child("+ i +") > div.evaluation-item-row.evaluation-item-from > span.when")
									.text());

		} catch (ParseException e) {
			logger.error("Error: {}, ", e);
		}
		try {
			tendererRating.payment_rating =
					shareData(doc,"#evaluation > div > div.panel-content > ul > li:nth-child("+ i +") > div.evaluation-item-row.evaluation-item-scores.clearfix > div.scores-item.scores-intime > span.stars-group > i")
							.size();
			tendererRating.coop_rating =
					shareData(doc,"#evaluation > div > div.panel-content > ul > li:nth-child("+ i +") > div.evaluation-item-row.evaluation-item-scores.clearfix > div.scores-item.scores-delight > span.stars-group > i")
							.size();

		} catch (NoSuchElementException e) {
			tendererRating.payment_rating = 0;
			tendererRating.coop_rating = 0;
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
	public com.sdyk.ai.crawler.task.Task pageTurn(int page) {

		// 判断是否翻页
		if (pageTurning("#evaluation > div > div.pagination-wrapper > div > ul > li", page)) {

			com.sdyk.ai.crawler.task.Task t = null;
			try {
				t = new TendererRatingTask("https://home.zbj.com/"
						+ this.getParamString("userId"), ++page, this.getParamString("userId"));
				return t;
			} catch (MalformedURLException | URISyntaxException e) {
				logger.error("Error extract channel: {}, ", getUrl(), e);
			}
		}
		return null;
	}

	@Override
	public TaskTrace getTaskTrace() {
		return new TaskTrace(this.getClass(), this.getParamString("userId"), this.getParamString("page"));
	}

	@Override
	public one.rewind.io.requester.Task validate() throws ProxyException.Failed, AccountException.Failed, AccountException.Frozen {
		return null;
	}
}
