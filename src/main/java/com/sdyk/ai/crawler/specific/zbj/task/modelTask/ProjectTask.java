package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.witkey.Project;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import com.sdyk.ai.crawler.specific.zbj.task.action.RefreshAction;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.BasicRequester;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ScheduledChromeTask;
import one.rewind.io.requester.task.TaskHolder;
import one.rewind.txt.DateFormatUtil;
import one.rewind.util.FileUtil;
import org.elasticsearch.index.fielddata.ScriptDocValues;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

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

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	public static List<String> crons = Arrays.asList("* * */1 * *");

	static {
		registerBuilder(
				ProjectTask.class,
				"https://task.zbj.com/{{project_id}}/",
				ImmutableMap.of("project_id", String.class),
				ImmutableMap.of("project_id", "0"),
				false,
				Priority.MEDIUM
		);
	}

	public Project project;

	public enum PageType {
		OrderDetail, ReqDetail
	}

	/**
	 *
	 * @param url
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public ProjectTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setBuildDom();

		this.addAction(new RefreshAction());

		this.addDoneCallback((t) -> {

			try {

				String src = getResponse().getText();

				Document doc = getResponse().getDoc();

				String tenderer_webId = null;

				FileUtil.writeBytesToFile(src.getBytes(), "project.html");

				// 初始化 必须传入url 生成主键id
				project = new Project(url);

				project.domain_id = 1;

				project.origin_id = url.split("/")[3];

				//
				if (src.contains("操作失败请稍后重试") || src.contains("很抱歉，此页面内部错误！")) {
					this.setRetry();
					return;
				}

				// TODO 猪八戒页面更新
				// 补充示例页面 channel: http://task.zbj.com/12919315/，http://task.zbj.com/9790967/

				// A 无法请求页面内容 #headerNavWrap > div:nth-child(1) > div > div.header-nav-sub-title
				//#headerNavWrap > div:nth-child(1) > div > a
				if (pageAccessible(src)) {
					String header;
					header = doc.select("#j-zbj-header-bd-wrap > div > div.bd-logo.clearfix > a > h1").text();

					//#j-zbj-header-bd-wrap > div > div.bd-logo.clearfix > a > h1
					if (header == null || header.equals("")) {
						header = doc.select("#headerNavWrap > div:nth-child(1) > div > div.header-nav-sub-title").text();
					}

					// B1 页面格式1 ：http://task.zbj.com/12954152/
					if (pageType(header) == PageType.OrderDetail) {

						logger.trace("Model: {}, Type: {}, URL: {}", Project.class.getSimpleName(), PageType.OrderDetail.name(), getUrl());
						try {
							tenderer_webId = procTypeA(doc, src, header);
							project.insert();
						} catch (Exception e) {
							logger.error("insert error for project", e);
						}
					}
					// B2 页面格式2 ：http://task.zbj.com/12954086/
					else if (pageType(header) == PageType.ReqDetail) {
						try {
							tenderer_webId = procTypeB(doc, header);
							project.insert();
						} catch (Exception e) {
							logger.error("insert error for project", e);
						}
					}

					// TODO 调用需求评分接口
					/*try {

						String Project_url = "http://10.0.0.63:51001/project/eval/" + project.id;
						ChromeTask chromeTask = new ChromeTask(Project_url);
						t.setPost();
						BasicRequester.getInstance().submit(t);
					} catch (Exception e) {
						logger.error("Error calculate project rating. ", e);
					}*/

					try {

						if (tenderer_webId != null && tenderer_webId.length() > 0) {

							//设置参数
							Map<String, Object> init_map = ImmutableMap.of("tenderer_id", tenderer_webId);

							Class<? extends ChromeTask> clazz = (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.zbj.task.modelTask.TendererTask");

							//生成holder
							TaskHolder holder = this.getHolder(clazz, init_map);

							//提交任务
							ChromeDriverDistributor.getInstance().submit(holder);
						}

					} catch (Exception e) {

						logger.error("error for submit TendererTask.class", e);
					}

					ScheduledChromeTask st = t.getScheduledChromeTask();

					// 第一次抓取生成定时任务 快照
					if(st == null) {

						st = new ScheduledChromeTask(t.getHolder(), crons);
						st.start();
					}
					// 已完成项目停止定时任务
					if( project.status.contains("完成") || project.status.contains("成功") || project.status.contains("失败")){
						st.stop();
					}
				}

			} catch (Exception e) {
				logger.error("", e);
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

		if (type == null) {
			return null;
		}
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

		String status = doc.select(".taskmode-block.clearfix > div.modecont > ul > li.cur > p")
				.text().split("2")[0];

		Elements elements = doc.select(".taskmode-block.clearfix > div.modecont > ul > li");
		// 剩余时间
		String time = doc.select(".modecont > ul > li.cur > span.taskmode-clock.o-time").attr("data-difftime");
		// 已经完成
		if (status == null || status.equals("")) {
			project.status = elements.get(elements.size()-1).select("p").text().split("2")[0];;
			if (project.status == null) {
				project.status = "评价";
			}

			project.due_time = null;

		} else {
			project.status = status;
			project.due_time = new Date(Long.valueOf(time) * 1000 + System.currentTimeMillis());
		}
	}

	/**
	 * pagetwo
	 * 项目状态， 时间
	 */
	public void projectStateTwo(Document doc) {

		// 项目状态
		// A 当前状态
		String status = doc.select(".timeline > div > div > ul > li.current > p:nth-child(3)")
					.text();
		// B 结束状态
		String status1 = doc.select(".timeline > div > div > ul > li.current > p:nth-child(2)")
				.text();
		Elements elements = doc.select(".timeline > div > div > ul > li");

		String time = doc.select(".timeline > div > div > ul > li.current > div.clock.absolute-1").attr("data-difftime");

		// 已经完成
		if (status == null || status.equals("")) {
			project.status = elements.get(elements.size()-1).select(" p:nth-child(2)").text() + " - "
					+ getString(".main-content > div.header-banner > i", "");;
			project.due_time = null;
		// 未完成
		} else {

			// 停止/关闭 状态
			if (time.equals("") || time == null ) {
				project.status = status1 + " - " + getString(".main-content > div.header-banner > i", "");
				project.due_time = null;
				// 正常 状态
			} else {
				project.status = status + " - " + getString(".main-content > div.header-banner > i", "");
				project.due_time = new Date(Long.valueOf(time)*1000 + System.currentTimeMillis());
			}
		}


		String pubdate = doc.select(".wrapper.header-block-div > p.task-describe > span:nth-child(2) > b")
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
	 * 获取甲方id，昵称，webid
	 */
	public String getTendererIdName(Document doc, String src) {

		// #j-content > div > div.user-toltit > dl > dt > img
		// #j-content > div > div.user-toltit > dl > dt > a > img
		//#j-content > div > div.user-toltit > dl > dt > img
		String link = doc.select("#j-content > div > div.user-toltit > dl > dt")
				.select("img")
				.attr("src")
				.split("\\.")[2];

		//https://avatar.zbjimg.com/014/00/94/200x200_avatar_48.jpg!middle

		String[] links = link.split("/");

		String s1 = links[1].substring(1,links[1].length());

		String ss = link.split("_")[2];

		project.tenderer_id = one.rewind.txt.StringUtil.byteArrayToHex(
				one.rewind.txt.StringUtil.uuid("https://home.zbj.com/" + s1 + links[2] + links[3] + ss));

		project.tenderer_name = doc.select("#j-content > div > div.user-toltit > dl > dt").select("img").attr("alt");

		Pattern pattern = Pattern.compile("<div class=\"taskmode-inline\" id=\"reward-all\">\\s+赏金分配：<em class=\"gray6\">(?<rewardType>.+?)</em>");
		Matcher matcher = pattern.matcher(src);
		if (matcher.find()) {
			project.reward_type = one.rewind.txt.StringUtil.removeHTML(matcher.group("rewardType"));
		}
		return "" + s1 + links[2] + links[3] + ss;
	}

	/**
	 *
	 * @param src
	 */
	public void finishProject(String src,Document doc) {
		// 1 采集时刻投标人数
		// 1.1 项目已完成
		if (src.contains("<div class=\"banner-task-summary clearfix\">")) {

			project.bids_available = getInt(
					"#anytime-back > div.banner-task-summary.clearfix > div.summary-right > h4:nth-child(2) > em:nth-child(2)",
					"个|名");

			if (project.bids_available == 0) {
				project.bids_available = getInt(
						"body > div.main.task-details > div.main-con.user-page > div.banner-task-summary.clearfix > div.summary-right.clearfix > h4:nth-child(2) > em:nth-child(2)",
						"个|名");
			}

		}
		// 1.2 项目未完成
		else {

			if (src.contains("该需求可接受")) {
				//#taskTabs > div > div:nth-child(1) > div > div.task-wantbid-launch > p > span:nth-child(1)
				project.bidder_total_num = StringUtil.getBidderTotalNum(doc,
						"#taskTabs > div > div:nth-child(1) > div > div.task-wantbid-launch > p > span:nth-child(1)");

				//#taskTabs > div > div:nth-child(1) > div > div.task-wantbid-launch > p > span:nth-child(1)
				project.bids_available = StringUtil.getBidderNum(doc,
						"#taskTabs > div > div:nth-child(1) > div > div.task-wantbid-launch > p > span:nth-child(2)");
			}
		}
	}

	/**
	 * 页面格式1
	 * 获取信息
	 * @param src 页面源
	 * */
	public String procTypeA(Document doc, String src, String head) {

		try {

			// 项目是否可投标，以及投标数量
			finishProject(src, doc);

			project.title = getString("#ed-tit > div.tctitle.clearfix > h1", "");
			project.location = getString("#j-receiptcon > span.ads", "");

			project.origin_from = getString("#j-receiptcon > a", "");

			// body > div.main.task-details > div.grid > ul
			project.category = getString("body > div.main.task-details > div.grid > ul > li:nth-child(2) > a", "");

			Elements elements = doc.select("body > div.main.task-details > div.grid > ul > li");
			project.tags = new ArrayList<>();
			for (int i = 2; i < elements.size(); i++) {
				project.tags.add(elements.get(i).text().replace(">", "")
						.replace(" ", ""));
			}

			// TODO 需要额外处理图片, 下载
			String description_src = doc.select("#work-more")
					.html()
					.replaceAll("<a class=\"check-all-btn\".+?>查看全部</a>", "")
					.replace("</label>", "")
					.replace("<label>", "");

			project.content = download(description_src).replaceAll("<p>附件:.+?下载</p>", "");

			project.time_limit = StringUtil.getTimeSpan(StringUtil.detectTimeSpanString(project.content));

			// 预算处理  #ed-tit > div.micon > div.fl.money-operate > p > u
			double[] budget = StringUtil.budget_all(doc,
					"#ed-tit > div.micon > div.fl.money-operate > p > u",
					project.content);
			project.budget_lb = budget[0];
			project.budget_ub = budget[1];

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
				project.bids_num = Integer.parseInt(matcher_bidder.group("T"));
			}
			while (matcher_collect.find()) {
				project.fav_num = Integer.parseInt(matcher_collect.group("T"));
			}

			// 获取招标人id
			return getTendererIdName(doc, src);
			/*try {
				tasks.add(new TendererTask("https://home.zbj.com/" + project.tenderer_id));
			} catch (MalformedURLException | URISyntaxException e) {
				logger.error("Error extract channel: {}, ", "http://home.zbj.com/" + project.tenderer_id, e);
			}*/

		} catch (Exception e) {
			logger.error("Error handle page category 1, {}, ", getUrl(), e);
		}
		return null;
	}

	/**
	 * 页面格式2
	 * 数据采取
	 * @param head
	 */
	public String procTypeB(Document doc, String head) {

		try {

			project.title = getString(
					".wrapper.header-block-div > h1", "");

			//#utopia_widget_2 > li:nth-child(1)
			project.category = getString(
					"#utopia_widget_2 > li:nth-child(2) > a",
					"").replace(">", "");

			Elements elements = doc.select("#utopia_widget_2 > li");
			for (int i = 2; i < elements.size(); i++) {
				if (i == elements.size()-1 ) {
					project.tags.add(elements.get(i).text().replace(">", "")
							.replace(" ", ""));
				}else {
					project.tags.add(elements.get(i).text().replace(">", ",").replace(" ", ""));
				}
			}
			if (project.tags.size() == 0) {
				project.tags.add(doc.select("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner > div.wrapper.header-block-div > p.task-describe > span:nth-child(3) > b").text());
			}

			String description_src = doc.select(".order-header-block.new-bid.header-block-with-banner > div.task-detail.wrapper > div.task-detail-content.content")
					.toString();

			// 下载
			project.content = download(description_src)
					.replace("</label>","")
					.replace("<label>", "");

			project.time_limit = StringUtil.getTimeSpan(StringUtil.detectTimeSpanString(project.content));

			// 获取地点，来源
			if (doc.select("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block > div.task-detail.wrapper > div.task-detail-content.content > div > span").size() == 2) {
				project.location = doc.select("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block > div.task-detail.wrapper > div.task-detail-content.content > div > span:nth-child(1)")
						.text().replace("-", " ");
				project.origin_from = doc.select("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block > div.task-detail.wrapper > div.task-detail-content.content > div > span:nth-child(2)")
						.text().replace("来自：", "");
			} else {
				project.origin_from = doc.select("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block > div.task-detail.wrapper > div.task-detail-content.content > div > span:nth-child(1)")
						.text().replace("来自：", "");
			}

			// 预算处理

			// #trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner > div.wrapper.header-block-div > p.mb4.price-describe > span:nth-child(1) > b
			double[] budget = StringUtil.budget_all(doc,
					"p.mb4.price-describe > span:nth-child(1) > b",
					project.content);
			project.budget_lb = budget[0];
			project.budget_ub = budget[1];

			// 项目状态，剩余时间
			projectStateTwo(doc);

			// 投标总数与投标人数
			//#taskTabs > div > div:nth-child(1) > div > div.task-wantbid-launch > p > span:nth-child(1)
			project.bidder_total_num = StringUtil.getBidderTotalNum(doc,
					".task-wantbid-launch > p.data-task-info > span:nth-child(1)");
			//#taskTabs > div > div:nth-child(1) > div > div.task-wantbid-launch > p > span:nth-child(2)
			project.bids_available = StringUtil.getBidderNum(doc,
					".task-wantbid-launch > p.data-task-info > span:nth-child(2)");

			project.trade_type = "投标";


			// 获取甲方id 昵称 webId
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
				String webId = matcher_id.group()
						.replace("\"buyerId\":", "")
						.replace(",", "");
				project.tenderer_id = one.rewind.txt.StringUtil.byteArrayToHex(
						one.rewind.txt.StringUtil.uuid("https://home.zbj.com/" + webId));

				return webId;
			}
		}
		catch (Exception e) {
			logger.error("error is {}", e);
		}
		return null;
	}
}
