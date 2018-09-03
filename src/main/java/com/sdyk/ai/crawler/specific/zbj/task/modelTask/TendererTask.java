package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.witkey.Tenderer;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import com.sdyk.ai.crawler.util.DateFormatUtil;
import com.sdyk.ai.crawler.util.LocationParser;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskFactory;
import one.rewind.io.requester.task.ScheduledChromeTask;
import one.rewind.io.requester.task.TaskHolder;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;

/**
 * 雇主详情
 */
public class TendererTask extends Task {

	public static long MIN_INTERVAL = 24 * 60 * 60 * 1000;

	public static List<String> crons = Arrays.asList("* * */1 * *", "* * */2 * *", "* * */4 * *", "* * */8 * *");

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

		// 判断是否发生异常
		this.setValidator((a, t) -> {

			String src = getResponse().getText();
			if (src.contains("请登录") && src.contains("活跃度")) {

				throw new AccountException.Failed(a.accounts.get(t.getDomain()));
			}

		});

		this.addDoneCallback((t)->{

			String userId = t.getStringFromVars("tenderer_id");

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

				// 定时任务
				tenderer.domain_id = 1;

				LocationParser parser = LocationParser.getInstance();
				if (tenderer.location != null && tenderer.location.length() > 0) {
					tenderer.location = parser
							.matchLocation(tenderer.location)
							.get(0).toString();
				}
				boolean status = tenderer.insert();
				ScheduledChromeTask st = t.getScheduledChromeTask();

				// 第一次抓取生成定时任务
				if(st == null) {

					try {
						st = new ScheduledChromeTask(t.getHolder(), crons);
						st.start();
					} catch (Exception e) {
						logger.error("error for creat ScheduledChromeTask", e);
					}

				}
				else {
					if( !status ){
						st.degenerate();
					}
				}

				// 添加projectTask
				try {

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					ImmutableMap.of("user_id", userId, "page", "1");


					//生成holder
					TaskHolder holder = ChromeTaskFactory.getInstance().newHolder(ProjectTask.class, init_map);

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
					TaskHolder holder = ChromeTaskFactory.getInstance().newHolder(clazz, init_map);
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
