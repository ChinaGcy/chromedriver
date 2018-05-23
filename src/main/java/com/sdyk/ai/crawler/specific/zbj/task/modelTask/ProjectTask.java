package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.sdyk.ai.crawler.specific.zbj.task.Task;
import com.sdyk.ai.crawler.util.StringUtil;
import com.sdyk.ai.crawler.model.Project;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.util.FileUtil;
import org.jsoup.nodes.Document;
import one.rewind.txt.DateFormatUtil;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 甲方需求详情
 */
public class ProjectTask extends Task {

	Project project;

	public enum PageType {
		OrderDetail, ReqDetail
	}

	public ProjectTask(String url) throws MalformedURLException, URISyntaxException {
		super(url);

		this.setBuildDom();
		// 设置优先级
		this.setPriority(Priority.HIGH);

		this.addDoneCallback(() -> {

			try {

				String src = getResponse().getText();
				Document doc = getResponse().getDoc();


				FileUtil.writeBytesToFile(src.getBytes(), "project.html");

				List<Task> tasks = new ArrayList();

				try {
					// 初始化 必须传入url 生成主键id
					project = new Project(getUrl());
				} catch (MalformedURLException | URISyntaxException e) {
					logger.error("Error extract channel: {}, ", getUrl(), e);
				}

				if (src.contains("操作失败请稍后重试")) {
					try {
						ChromeDriverRequester.getInstance().submit(new ProjectTask(getUrl()));
						return;
					} catch (MalformedURLException | URISyntaxException e) {
						logger.error(e);
					}
				}
				// TODO 补充示例页面 channel: http://task.zbj.com/12919315/，http://task.zbj.com/9790967/

				// A 无法请求页面内容
				if (pageAccessible(src)) {

					String header;

					// #headerNavWrap > div:nth-child(1) > div > div.header-nav-sub-title
					header = doc.select("#headerNavWrap > div:nth-child(1) > div > div.header-nav-sub-title").text();

					// B1 页面格式1 ：http://task.zbj.com/12954152/
					if (pageType(header) == PageType.OrderDetail) {

						logger.trace("Model: {}, Type: {}, URL: {}", Project.class.getSimpleName(), PageType.OrderDetail.name(), getUrl());

						procTypeA(doc, src, header, tasks);
						try {
							project.insert();
						} catch (Exception e) {
							logger.error("insert error for project", e);
						}
					}
					// B2 页面格式2 ：http://task.zbj.com/12954086/
					else if (pageType(header) == PageType.ReqDetail) {

						procTypeB(doc, header, tasks);
						try {
							project.insert();
						} catch (Exception e) {
							logger.error("insert error for project", e);
						}
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
	 * 处理无法访问的页面
	 * 例：	http://task.zbj.com/9790967/
	 * 		http://task.zbj.com/11810444/
	 *
	 * @param src 页面源
	 * @return
	 */
	public boolean pageAccessible(String src) {

		String reg = "(无法|无权|不能|暂不能在网页|该任务仅限雇主和参与服务商)查看" +
				"|参数校验错误";
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(src);
		if(matcher.find()) {
			return false;
		}
		return true;
	}

	/**
	 * 判断页面格式
	 * @return
	 */
	public PageType pageType(String type) {

		if (type.contains("订单详情")) {
			project.type = "订单详情";
			return PageType.OrderDetail;
		}
		else if (type.contains("需求详情")) {
			project.type = "需求详情";
			return PageType.ReqDetail;
		}

		// 找不到 header
		return null;
	}

	/**
	 * pageone
	 * 项目状态， 时间
	 */
	public void projectStateOne(Document doc) {


		project.current_status = doc.select("#j-content > div > div.taskmode-block.clearfix > div.modecont.ullen7.ullen7-curstep3 > ul > li.cur > p")
					.text().split("2")[0];
		// 已经完成
		if (project.current_status == null || project.current_status.equals("")) {
			project.current_status = "评价";
		}


		// 剩余时间
		try {
			String time = doc.select("#j-content > div > div.taskmode-block.clearfix > div.modecont.ullen7.ullen7-curstep3 > ul > li.cur > span.taskmode-clock.o-time > em")
					.text();
			if (time.equals("") || time == null) {
				// 无法找到剩余时间
				project.remaining_time = null;
			}else {
				project.remaining_time = DateFormatUtil.parseTime(time);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

	}

	/**
	 * pagetwo
	 * 项目状态， 时间
	 */
	public void projectStateTwo(Document doc) {

		// 项目状态
		project.current_status = doc.select("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner > div.timeline > div > div > ul > li.current > p:nth-child(3)")
					.text();
		// 已经完成
		if (project.current_status == null || project.current_status.equals("")) {
			project.current_status = "评价";
		}

		String time = doc.select("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner > div.timeline > div > div > ul > li.current > div.clock.absolute-1 > em")
				.text();
		// 无法找到剩余时间
		if (time == null || time.equals("")) {
			project.remaining_time = null;
		}
		else {
			// 剩余时间
			try {
				project.remaining_time = DateFormatUtil.parseTime(time);
			} catch (ParseException e) {
				logger.error("projectTask remaining is error {}", e);
			}
		}


		String pubdate = doc.select("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner > div.wrapper.header-block-div > p.task-describe > span:nth-child(2) > b")
				.text();
		if (pubdate == null || pubdate.equals("")) {
			project.pubdate = null;
		}
		else {
			try {
				project.pubdate = DateFormatUtil.parseTime(pubdate);
			} catch (ParseException e) {
				logger.error("project pubdate is error", e);
			}
		}
	}

	/**
	 *
	 */
	public void getTendererIdName(Document doc, String src) {

		String link = doc.select("#j-content > div > div.user-toltit > dl > dt > a > img").attr("src").split("\\.")[2];

		String[] links = link.split("/");

		String ss = links[4].split("_")[2];

		project.tenderer_id = Integer.parseInt(links[1] + links[2] + links[3] + ss)+"";

		project.tenderer_name = doc.select("#j-content > div > div.user-toltit > dl > dt > a > img").attr("alt");

		Pattern pattern = Pattern.compile("<div class=\"taskmode-inline\" id=\"reward-all\">\\s+赏金分配：<em class=\"gray6\">(?<rewardType>.+?)</em>");
		Matcher matcher = pattern.matcher(src);
		if (matcher.find()) {
			project.reward_type = one.rewind.txt.StringUtil.removeHTML(matcher.group("rewardType"));
		}

	}

	/**
	 *
	 * @param src
	 */
	public void finishProject(String src,Document doc) {
		// 1 采集时刻投标人数
		// 1.1 项目已完成
		if (src.contains("<div class=\"banner-task-summary clearfix\">")) {

			project.bidder_num = getInt(
					"#anytime-back > div.banner-task-summary.clearfix > div.summary-right > h4:nth-child(2) > em:nth-child(2)",
					"个|名");

			if (project.bidder_num == 0) {
				project.bidder_num = getInt(
						"body > div.main.task-details > div.main-con.user-page > div.banner-task-summary.clearfix > div.summary-right.clearfix > h4:nth-child(2) > em:nth-child(2)",
						"个|名");
			}

		}
		// 1.2 项目未完成
		else {

			if (src.contains("该需求可接受")) {
				project.bidder_total_num = StringUtil.getBidderTotalNum(doc,
						"#j-ibid-list > div > div.ibid-total.clearfix > b:nth-child(1)");

				//#j-ibid-list > div > div.ibid-total.clearfix > b:nth-child(2)
				project.bidder_num = StringUtil.getBidderNum(doc,
						"#j-ibid-list > div > div.ibid-total.clearfix > b");
			}
		}
	}

	/**
	 * 页面格式1
	 * 获取信息
	 * @param src 页面源
	 * @param head 页面格式
	 * @param tasks
	 */
	public void procTypeA(Document doc, String src, String head, List<Task> tasks) {

		try {

			// 项目是否可投标，以及投标数量
			finishProject(src, doc);

			project.title = getString("#ed-tit > div.tctitle.clearfix > h1", "");

			project.area = getString("#j-receiptcon > span.ads", "");

			project.origin = getString("#j-receiptcon > a", "");

			// body > div.main.task-details > div.grid > ul
			project.category = getString("body > div.main.task-details > div.grid > ul", "");

			// TODO 需要额外处理图片, 下载
			String description_src = doc.select("#work-more")
					.html()
					.replaceAll("<a class=\"check-all-btn\".+?>查看全部</a>", "");

			project.description = download(description_src);

			project.time_limit = StringUtil.getTimeSpan(StringUtil.detectTimeSpanString(project.description));

			// 预算处理  #ed-tit > div.micon > div.fl.money-operate > p > u
			double[] budget = StringUtil.budget_all(doc,
					"#ed-tit > div.micon > div.fl.money-operate > p > u",
					project.description);
			project.budget_lb = budget[0];
			project.budget_up = budget[1];

			// 发布时间
			String pubdate = doc.select("#j-receiptcon > span.time").text();

			if (pubdate == null || pubdate.equals("")) {
				project.pubdate = null;
			} else {
				try {
					project.pubdate = DateFormatUtil.parseTime(doc.select("#j-receiptcon > span.time").text());
				} catch (ParseException e) {
					logger.error("projectTask one pubdate is bad", e);
				}
			}
			// 获取招标人id
			getTendererIdName(doc, src);

			// 项目状态 剩余时间
			projectStateOne(doc);

			project.trade_type = doc.select("#j-content > div > div.taskmode-block.clearfix > div.header > em").text();

			String data = doc
					.select("#anytime-back > div.user-bg.clearfix > div.right.task-right > div.r-c > div > div.task-service-box-bd")
					.text();
			Pattern pattern_view = Pattern.compile("浏览：(?<T>\\d+)次");
			Pattern pattern_bidder = Pattern.compile("投标次数：(?<T>\\d+)次");
			Pattern pattern_collect = Pattern.compile("收藏：(?<T>\\d+)人");

			Matcher matcher_view = pattern_view.matcher(data);
			Matcher matcher_bidder = pattern_bidder.matcher(data);
			Matcher matcher_collect = pattern_collect.matcher(data);

			while (matcher_view.find()) {
				project.view_num = Integer.parseInt(matcher_view.group("T"));
			}
			while (matcher_bidder.find()) {
				project.bidder_new_num = Integer.parseInt(matcher_bidder.group("T"));
			}
			while (matcher_collect.find()) {
				project.collect_num = Integer.parseInt(matcher_collect.group("T"));
			}

			// 进入雇主页
			try {
				tasks.add(new TendererTask("https://home.zbj.com/" + project.tenderer_id));
			} catch (MalformedURLException | URISyntaxException e) {
				logger.error("Error extract channel: {}, ", "http://home.zbj.com/" + project.tenderer_id, e);
			}
		} catch (Exception e) {
			logger.error("Error handle page type 1, {}, ", getUrl(), e);
		}
	}

	/**
	 * 页面格式2
	 * 数据采取
	 * @param head
	 */
	public void procTypeB(Document doc, String head, List<Task> tasks) {

		try {

			String src = doc.head().select("#storage").toString();

			Pattern pattern_name = Pattern.compile("\"buyerName\":.+?\"");
			Matcher matcher_name = pattern_name.matcher(src);
			if (matcher_name.find()) {
				project.tenderer_name = matcher_name.group()
						.replace("\"buyerName\":\"", "")
						.replace("\"", "");
			}
			Pattern pattern_id = Pattern.compile("\"buyerId\":.+?,");
			Matcher matcher_id = pattern_id.matcher(src);
			if (matcher_id.find()) {
				project.tenderer_id = matcher_id.group()
						.replace("\"buyerId\":", "")
						.replace(",", "");
			}

			project.type = head;

			project.title = getString(
					"#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block > div.wrapper.header-block-div > h1", "");

			project.req_no = getString(
					"#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block > div.wrapper.header-block-div > p.task-describe > span:nth-child(1) > b", "");

			project.category = getString(
					"#utopia_widget_3",
					"");

			String description_src = doc.select("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner > div.task-detail.wrapper > div.task-detail-content.content")
					.toString();

			// 下载
			project.description = download(description_src);

			project.time_limit = StringUtil.getTimeSpan(StringUtil.detectTimeSpanString(project.description));

			// 获取地点，来源
			if (doc.select("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner > div.task-detail.wrapper > div.task-detail-content.content > div > span").size() == 2) {
				project.area = doc.select("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner > div.task-detail.wrapper > div.task-detail-content.content > div > span:nth-child(1)")
						.text().replace("-", " ");
				project.origin = doc.select("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner > div.task-detail.wrapper > div.task-detail-content.content > div > span:nth-child(2)")
						.text().replace("来自：", "");
			} else {
				project.origin = doc.select("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner > div.task-detail.wrapper > div.task-detail-content.content > div > span:nth-child(1)")
						.text().replace("来自：", "");
			}

			// 预算处理

			double[] budget = StringUtil.budget_all(doc,
					"#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner > div.wrapper.header-block-div > p.mb4.price-describe > span:nth-child(1) > b",
					project.description);
			project.budget_lb = budget[0];
			project.budget_up = budget[1];

			// 项目状态，剩余时间
			projectStateTwo(doc);

			// 投标总数与投标人数
			project.bidder_total_num = StringUtil.getBidderTotalNum(doc,
					"#taskTabs > div > div:nth-child(1) > div > div.task-wantbid-launch > p.data-task-info > span:nth-child(1)");
			project.bidder_num = StringUtil.getBidderNum(doc,
					"#taskTabs > div > div:nth-child(1) > div > div.task-wantbid-launch > p.data-task-info > span");

			project.status = getString("#trade-content > div.page-info-content.clearfix > div.main-content > div.header-banner > i", "");

			project.trade_type = "投标";

			// 进入雇主页
			try {
				tasks.add(new TendererTask("https://home.zbj.com/" + project.tenderer_id));
			} catch (MalformedURLException | URISyntaxException e) {
				logger.error("Error extract channel: {}, ", "http://home.zbj.com/" + project.tenderer_id, e);
			}
		}
		catch (Exception e) {
			logger.error("Error handle page type 2, {}, ", getUrl(), e);
		}
	}
}
