package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.Project;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.ScanTask;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TendererOrderTask extends ScanTask {

	static {
		// init_map_class
		init_map_class = ImmutableMap.of("user_id", String.class, "page", String.class);
		// init_map_defaults
		init_map_defaults = ImmutableMap.of("user_id", "0", "page", "0");
		// url_template
		url_template = "https://home.zbj.com/{{user_id}}/?op={{page}}";
	}

	public TendererOrderTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {
		super(url);

		this.setBuildDom();

		this.addDoneCallback((t) -> {

			String userId = null;
			int page = 0;
			Pattern pattern = Pattern.compile("https://home.zbj.com/(?<userId>\\d+)/\\?op=(?<page>\\d+)");
			Matcher matcher = pattern.matcher(getUrl());
			if (matcher.find()) {
				userId = matcher.group("userId");
				page = Integer.parseInt(matcher.group("page"));
			}

			Document doc = getResponse().getDoc();

			// 获取历史数据（简略）
			try {
				getSimpleProjectTask(doc, userId);

			} catch (Exception e) {
				e.printStackTrace();
			}

			// 翻页
			if (pageTurning("#order > div > div.pagination-wrapper > div > ul > li", page)) {

				HttpTaskPoster.getInstance().submit(this.getClass(),
						 ImmutableMap.of("user_id", userId, "page", String.valueOf(++page)));
			}
		});
	}

	/**
	 * 添加简略project数据，之后更新成为具体数据
	 * @param doc
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public static void getSimpleProjectTask(Document doc, String userId) throws Exception {

		Elements elements = doc.select("#order > div > div.panel-content > ul > li");

		for (Element element : elements) {

			// 与project的url一致，更新数据库。
			String url = element.select("div > div.order-item-title > a").attr("href");

			logger.info(url);

			Project project = new Project(url);

			project.domain_id = 1;

			project.origin_id = url.split("/")[3];

			project.tenderer_name = doc.select("#utopia_widget_1 > div > div.topinfo-top > div > h2").text();

			project.tenderer_id = one.rewind.txt.StringUtil.byteArrayToHex(
					one.rewind.txt.StringUtil.uuid(
							"https://home.zbj.com/" + userId));

			project.title = element.select("div > div.order-item-title > a").text();

			project.trade_type = element.select("span.order-item-category").text();

			// TODO 正则获取数据
			//#order > div > div.panel-content > ul > li:nth-child(2) > div > div.order-item-subinfo
			// div > div.order-item-subinfo
			String text = element.select("div > div.order-item-subinfo").text();

			if (text.contains("提供服务")) {
				project.bids_num = 1;
			} else {
				Pattern pattern = Pattern.compile("(?<T>\\d+)位服务商参与");
				Matcher matcher = pattern.matcher(text);
				if (matcher.find()) {
					project.bids_num = Integer.parseInt(matcher.group("T"));
				}
			}
			Pattern pattern_time = Pattern.compile("\\d+-\\d+-\\d+ \\d+:\\d+:\\d+");
			Matcher matcher_time = pattern_time.matcher(text);
			if (matcher_time.find()) {
				project.pubdate = DateFormatUtil.parseTime(matcher_time.group());
			}

			Pattern pattern_origin = Pattern.compile("来自：(?<T>.+?)$");
			Matcher matcher_origin = pattern_origin.matcher(text);
			if (matcher_origin.find()) {
				project.origin_from = matcher_origin.group("T");
			}

			try {
				project.budget_ub = Double.parseDouble(element.select("span.order-item-budget.fr > em")
						.text()
						.replace("￥", ""));
			} catch (Exception e) {
				e.printStackTrace();
			}

			project.budget_lb = project.budget_ub;

			project.status = element.select("div > div.order-item-title > span").text();

			project.insert();

			HttpTaskPoster.getInstance().submit(ProjectTask.class,
					ImmutableMap.of("project_id", project.origin_id ));

		}


	}

	@Override
	public TaskTrace getTaskTrace() {

		return new TaskTrace(this.getClass(), this.getParamString("userId"), this.getParamString("page"));
	}
}
