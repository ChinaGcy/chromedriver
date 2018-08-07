package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.google.common.collect.ImmutableMap;

import com.sdyk.ai.crawler.model.witkey.Tenderer;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import com.sdyk.ai.crawler.util.DateFormatUtil;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 雇主详情
 */
public class TendererTask extends Task {

	static {
		registerBuilder(
				TendererTask.class,
				"https://home.zbj.com/{{tenderer_id}}",
				ImmutableMap.of("tenderer_id", String.class),
				ImmutableMap.of("tenderer_id", "0"),
				false,
				Priority.MEDIUM
		);
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

				tenderer.login_time =
						DateFormatUtil.parseTime(doc.select("#utopia_widget_1 > div > div.topinfo-top > div > div > span.last-login")
								.text());


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
				String head1 = StringUtil.cleanContent(head, head_img);
				this.download(head);
				tenderer.head_portrait = one.rewind.txt.StringUtil.byteArrayToHex(
						one.rewind.txt.StringUtil.uuid("https:" + head1));

				tenderer.insert();

				// 添加projectTask
				try {

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					ImmutableMap.of("user_id", userId, "page", "1");

					Class<? extends ChromeTask> clazz = (Class<? extends ChromeTask>) TendererOrderTask.class;

					//生成holder
					ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

					//提交任务
					ChromeDriverDistributor.getInstance().submit(holder);

				} catch (Exception e) {

					logger.error("error for submit TendererOrderTask.class", e);
				}

				// 评价任务
				try {

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					ImmutableMap.of("user_id", userId, "page", "1");

					Class<? extends ChromeTask> clazz = (Class<? extends ChromeTask>) TendererRatingTask.class;

					//生成holder
					ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

					//提交任务
					ChromeDriverDistributor.getInstance().submit(holder);

				} catch (Exception e) {

					logger.error("error for submit TendererRatingTask.class", e);
				}

			}catch (Exception e) {
				logger.error(e);
			}
		});
	}
}
