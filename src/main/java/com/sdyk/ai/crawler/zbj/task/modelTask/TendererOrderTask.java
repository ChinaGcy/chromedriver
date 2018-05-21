package com.sdyk.ai.crawler.zbj.task.modelTask;

import com.j256.ormlite.stmt.query.In;
import com.sdyk.ai.crawler.zbj.model.Project;
import com.sdyk.ai.crawler.zbj.task.Task;
import com.sdyk.ai.crawler.zbj.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.zbj.task.scanTask.ScanTask;
import com.sdyk.ai.crawler.zbj.util.StatManager;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.redisson.executor.TasksBatchService;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TendererOrderTask extends ScanTask {

	/**
	 * 翻页
	 * @param url
	 * @param page
	 * @param webId
	 * @return
	 */
	public static TendererOrderTask generateTask(String url, int page, String webId) {

		TendererOrderTask t = null;
		String url_ = url+ "/?op=" + page;
		try {
			t = new TendererOrderTask(url_, page, webId);
			t.setBuildDom();
			return t;
		} catch (MalformedURLException | URISyntaxException e) {
			e.printStackTrace();
		}
		return t;
	}

	public TendererOrderTask(String url, int page, String webId) throws MalformedURLException, URISyntaxException {
		super(url);
		this.setParam("page", page);
		this.setParam("webId", webId);
		this.setBuildDom();

		this.addDoneCallback(() -> {

			List<Task> tasks = new ArrayList<>();
			Document doc = getResponse().getDoc();

			int op_page = this.getParamInt("page");

			// 获取历史数据（简略）
			try {
				tasks.addAll(getSimpleProjectTask(doc, webId));
			} catch (Exception e) {
				e.printStackTrace();
			}

			if (pageTurning("#order > div > div.pagination-wrapper > div > ul >li", op_page)) {
				// 翻页
				Task t = generateTask("https://home.zbj.com/"
						+ this.getParamString("webId"), ++op_page, this.getParamString("webId"));
				if (t != null) {
					t.setPriority(Priority.MEDIUM);
					t.setBuildDom();
					tasks.add(t);
				}
			}

			for(Task t : tasks) {
				t.setBuildDom();
				ChromeDriverRequester.getInstance().submit(t);
			}
		});
	}

	/**
	 * 添加简略project数据，之后更新成为具体数据
	 * @param doc
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public static List<Task> getSimpleProjectTask(Document doc, String webId) throws Exception {

		List<Task> tasks = new ArrayList<>();

		Elements elements = doc.select("#order > div > div.panel-content > ul > li");

		for (Element element : elements) {

			String url = element.select("div > div.order-item-title > a").attr("href");

			logger.info(url);

			Project project = new Project(url);

			project.tenderer_name = doc.select("#utopia_widget_1 > div > div.topinfo-top > div > h2").text();

			project.tenderer_id = webId;

			project.title = element.select("div > div.order-item-title > a").text();

			project.trade_type = element.select("span.order-item-type").text();

			// TODO 正则获取数据
			// div > div.order-item-subinfo
			String text = doc.select("div > div.order-item-subinfo").text();

			if (text.contains("提供服务")) {
				project.bidder_new_num = 1;
			} else {
				Pattern pattern = Pattern.compile("(?<T>\\d+)位服务商参与");
				Matcher matcher = pattern.matcher(text);
				if (matcher.find()) {
					project.bidder_new_num = Integer.parseInt(matcher.group("T"));
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

				project.origin = matcher_origin.group("T");
			}

			try {
				project.budget_up = Double.parseDouble(element.select("span.order-item-budget.fr > em")
						.text()
						.replace("￥", ""));
			} catch (Exception e) {
				e.printStackTrace();
			}

			project.budget_lb = project.budget_up;

			project.status = element.select("div > div.order-item-title > span").text();

			project.insert();

			tasks.add(new ProjectTask(url));
		}

		return tasks;
	}
}
