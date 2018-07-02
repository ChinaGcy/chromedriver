package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.Tenderer;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 雇主详情
 */
public class TendererTask extends Task {

	static {
		// init_map_class
		init_map_class = ImmutableMap.of("tenderer_id", String.class);
		// init_map_defaults
		init_map_defaults = ImmutableMap.of("tenderer_id", "0");
		// url_template
		url_template = "https://home.zbj.com/{{tenderer_id}}/";

		need_login = false;
	}

	public TendererTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);
		this.setBuildDom();

		this.addDoneCallback((t)->{

			String userId = null;
			Pattern pattern = Pattern.compile("https://home.zbj.com/(?<userId>.+?)\\/$");
			Matcher matcher = pattern.matcher(url);
			if (matcher.find()) {
				userId = matcher.group("userId");
			}

			try {

				Document doc = getResponse().getDoc();

				List<com.sdyk.ai.crawler.task.Task> tasks = new ArrayList<>();

				Tenderer tenderer = new Tenderer(getUrl());

				tenderer.origin_id = getUrl().split("com/")[1];
				tenderer.name = getString(
						"#utopia_widget_1 > div > div.topinfo-top > div > h2",
						"");

				tenderer.location = getString(
						"#utopia_widget_1 > div > div.topinfo-top > div > div > span.location",
						"");

				if (doc.select("#utopia_widget_1 > div > div.topinfo-top > div > div > span.last-login")
						.text().contains("个月前")) {

				}
				try {
					tenderer.login_time =
							DateFormatUtil.parseTime(doc.select("#utopia_widget_1 > div > div.topinfo-top > div > div > span.last-login")
									.text());
				} catch (ParseException e) {
					e.printStackTrace();
				}

				tenderer.trade_num =
						Integer.parseInt(doc.select("#utopia_widget_1 > div > div.topinfo-bottom > div > div > div.statistics-item.statistics-trade > div.statistics-item-val > strong")
								.text().replaceAll("-", "0"));

				tenderer.category =
						doc.select("#utopia_widget_1 > div > div.topinfo-bottom > div > div > div:nth-child(3) > div.statistics-item-val")
								.text();

				tenderer.tender_type =
						doc.select("#utopia_widget_1 > div > div.topinfo-bottom > div > div > div.statistics-item.statistics-time > div.statistics-item-val")
								.text();

				tenderer.company_scale =
						doc.select("#utopia_widget_1 > div > div.topinfo-bottom > div > div > div.statistics-item.statistics-scale > div.statistics-item-val")
								.text();

				tenderer.content =
						doc.select("#utopia_widget_4 > div > div > p").text();

				tenderer.req_forecast =
						doc.select("#utopia_widget_5 > div > div > h5").text();

				tenderer.total_spending =
						Double.parseDouble(doc.select("#utopia_widget_1 > div > div.topinfo-bottom > div > div > div.statistics-item.statistics-pay > div.statistics-item-val > strong")
								.text()
								.replaceAll(",", ""));

				// 获取头像
				String head = doc.select("#utopia_widget_1 > div > div.avatar")
						.html();
				Set<String> head_img = new HashSet<>();
				String head1 = StringUtil.cleanContent(head, head_img,null, null);
				this.download(head);
				tenderer.head_portrait = one.rewind.txt.StringUtil.byteArrayToHex(
						one.rewind.txt.StringUtil.uuid("https:" + head1));

				tenderer.insert();

				// 添加projectTask
				/*tasks.add(TendererOrderTask.generateTask(getUrl(), 1, tenderer.origin_id));*/
				HttpTaskPoster.getInstance().submit(TendererOrderTask.class,
						ImmutableMap.of("user_id", userId, "page", "1"));

				// 评价任务
				/*tasks.add(TendererRatingTask.generateTask(getUrl(), 1, tenderer.origin_id));*/
				HttpTaskPoster.getInstance().submit(TendererRatingTask.class,
						ImmutableMap.of("user_id", userId, "page", "1"));

				/*for (com.sdyk.ai.crawler.task.Task t : tasks) {
					t.setBuildDom();
					ChromeDriverRequester.getInstance().submit(t);
				}*/


			}catch (Exception e) {
				logger.error(e);
			}
		});
	}
}
