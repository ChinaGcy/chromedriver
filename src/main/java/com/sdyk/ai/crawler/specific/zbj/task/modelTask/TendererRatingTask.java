package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.model.TendererRating;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.ScanTask;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.NoSuchElementException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 雇主评价
 */
public class TendererRatingTask extends ScanTask {

	static {
		// init_map_class
		init_map_class = ImmutableMap.of("user_id", String.class, "page", String.class);
		// init_map_defaults
		init_map_defaults = ImmutableMap.of("user_id", "0", "page", "0");
		// url_template
		url_template = "https://home.zbj.com/{{user_id}}/?ep={{page}}";
	}

	TendererRating tendererRating;

	public TendererRatingTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {
		super(url);

		this.setBuildDom();

		this.addDoneCallback((t) -> {

			// 获取url参数
			String userId = null;
			int page = 0;

			Pattern pattern = Pattern.compile("https://home.zbj.com/(?<userId>\\d+)/\\?ep=(?<page>\\d+)");

			Matcher matcher = pattern.matcher(getUrl());

			if (matcher.find()) {
				userId = matcher.group("userId");
				page = Integer.parseInt(matcher.group("page"));
			}

			try {

				Document doc = getResponse().getDoc();

				// 防止数据为空
				if (!doc.select("#evaluation > div > div").text().contains("该雇主还未收到过评价")) {


					// 每页中的评价数
					for (int i = 1; i <= doc.select("#evaluation > div > div.panel-content > ul >li").size(); i++) {

						tendererRating = new TendererRating(getUrl() + "&" + i);

						tendererRating.user_id = one.rewind.txt.StringUtil.byteArrayToHex(
								one.rewind.txt.StringUtil.uuid(
										getUrl().split("\\?")[0]));

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
					pageTurn(page, userId);
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

		tendererRating.service_provider_name =
				shareData(doc,"#evaluation > div > div.panel-content > ul > li:nth-child("+ i +") > div.evaluation-item-row.evaluation-item-from > span.who > a")
						.text();

		tendererRating.service_provider_id = one.rewind.txt.StringUtil.byteArrayToHex(
				one.rewind.txt.StringUtil.uuid(
				shareData(doc,"#evaluation > div > div.panel-content > ul > li:nth-child("+ i +") > div.evaluation-item-row.evaluation-item-from > span.who > a")
						.attr("href").replace("http", "https") + "/"));

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
	public void pageTurn(int page, String userId) {

		// 判断是否翻页
		if (pageTurning("#evaluation > div > div.pagination-wrapper > div > ul > li", page)) {

			try {
				HttpTaskPoster.getInstance().submit(this.getClass(),
						ImmutableMap.of("user_id", userId, "page", String.valueOf(++page)));
			} catch (ClassNotFoundException | UnsupportedEncodingException | URISyntaxException | MalformedURLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public TaskTrace getTaskTrace() {
		return new TaskTrace(this.getClass(), this.getParamString("userId"), this.getParamString("page"));
	}
}
