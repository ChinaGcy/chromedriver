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
			String src = getResponse().getText();
			List<Task> tasks = new ArrayList<>();
			Document doc = getResponse().getDoc();

			int op_page = this.getParamInt("page");

			// 获取历史数据（简略）
			try {
				getSimpleProjectTask(doc, tasks);
			} catch (MalformedURLException | URISyntaxException e) {
				e.printStackTrace();
			}

			if (pageTurning("#order > div > div.pagination-wrapper > div > ul", op_page)) {
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
	 * @param tasks
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public void getSimpleProjectTask(Document doc, List<Task> tasks) throws MalformedURLException, URISyntaxException {

		Elements elements = doc.select("#order > div > div.panel-content > ul > li");

		for (Element element : elements) {

			String url = element.select("div > div.order-item-title > a").attr("href");

			System.err.println(url);

			Project project = new Project(url);

			project.title = element.select("div > div.order-item-title > a").text();
			project.trade_type = element.select("span.order-item-type").text();
			try {
				project.pubdate = DateFormatUtil.parseTime(element.select("div > div.order-item-subinfo > span:nth-child(3)")
						.text());
			} catch (ParseException e) {
				logger.error(e);
			}
			project.origin = element.select("div > div.order-item-subinfo > span:nth-child(5)").text();
			project.bidder_new_num = Integer.parseInt(element.select("div > div.order-item-subinfo > span:nth-child(1)")
					.text()
					.split("位")[0]);

			project.budget_up = Double.parseDouble(element.select("#order > div > div.panel-content > ul > li:nth-child(1) > span.order-item-budget.fr > em")
					.text()
					.replace("￥", ""));
			project.budget_lb = project.budget_up;

			project.trade_type = element.select("div > div.order-item-title > span").text();

			System.err.println(project.toJSON());

			project.insert();

			tasks.add(new ProjectTask(url));
		}
	}
}
