package com.sdyk.ai.crawler.zbj.task.modelTask;

import com.sdyk.ai.crawler.zbj.model.Tenderer;
import com.sdyk.ai.crawler.zbj.task.Task;
import org.jsoup.nodes.Document;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import one.rewind.txt.DateFormatUtil;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 雇主详情
 */
public class TendererTask extends Task {

	public TendererTask(String url) throws MalformedURLException, URISyntaxException {
		super(url);
		this.setPriority(Priority.MEDIUM);
	}

	public List<Task> postProc() throws ParseException {

		Document doc = getResponse().getDoc();
		String src = getResponse().getText();
		List<Task> tasks = new ArrayList<>();

		Tenderer tenderer = new Tenderer(getUrl());

		tenderer.website_id = getUrl().split("com/")[1];
		tenderer.name = getString(
				"#utopia_widget_1 > div > div.topinfo-top > div > h2",
				"");
		try {
			tenderer.area = getString(
					"#utopia_widget_1 > div > div.topinfo-top > div > div > span.location",
					"");
		}
		catch (NoSuchElementException e) {
			tenderer.area = "";
		}
		try {
			tenderer.login_time =
					DateFormatUtil.parseTime(doc.select("#utopia_widget_1 > div > div.topinfo-top > div > div > span.last-login")
							.text());
		} catch (NoSuchElementException e) {
			tenderer.login_time = null;
		}


		tenderer.trade_num =
				Integer.parseInt(doc.select("#utopia_widget_1 > div > div.topinfo-bottom > div > div > div.statistics-item.statistics-trade > div.statistics-item-val > strong")
				.text().replaceAll("-", "0"));

		tenderer.industry =
				doc.select("#utopia_widget_1 > div > div.topinfo-bottom > div > div > div:nth-child(3) > div.statistics-item-val")
				.text();

		tenderer.tender_type =
				doc.select("#utopia_widget_1 > div > div.topinfo-bottom > div > div > div.statistics-item.statistics-time > div.statistics-item-val")
				.text();

		tenderer.enterprise_size =
				doc.select("#utopia_widget_1 > div > div.topinfo-bottom > div > div > div.statistics-item.statistics-scale > div.statistics-item-val")
				.text();

		tenderer.description =
				doc.select("#utopia_widget_4 > div > div > p").text();

		tenderer.demand_forecast =
				doc.select("#utopia_widget_5 > div > div > h5").text();

		tenderer.total_spending =
				Double.parseDouble(doc.select("#utopia_widget_1 > div > div.topinfo-bottom > div > div > div.statistics-item.statistics-pay > div.statistics-item-val > strong")
				.text().replaceAll(",", ""));

		try {

			tenderer.insert();
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 添加projectTask
		tasks.add(TendererOrderTask.generateTask(getUrl(), 1 ,tenderer.website_id));

		// 评价任务
		tasks.add(TendererRatingTask.generateTask(getUrl(), 1, tenderer.website_id));

		return tasks;
	}

}
