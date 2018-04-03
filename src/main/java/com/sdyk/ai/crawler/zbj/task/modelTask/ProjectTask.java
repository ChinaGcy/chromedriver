package com.sdyk.ai.crawler.zbj.task.modelTask;

import com.sdyk.ai.crawler.zbj.exception.IpException;
import com.sdyk.ai.crawler.zbj.proxy.proxyPool.ProxyReplace;
import com.sdyk.ai.crawler.zbj.task.Task;
import com.sdyk.ai.crawler.zbj.util.StringUtil;
import com.sdyk.ai.crawler.zbj.model.Project;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.tfelab.txt.DateFormatUtil;

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
		// 设置优先级
		this.priority = Priority.high;
	}

	/**
	 * @param driver
	 * @return
	 */
	public List<Task> postProc(WebDriver driver) {

		String src = getResponse().getText();
		List<Task> tasks = new ArrayList();

		// 判断是否被禁
		try {
			ProxyReplace.proxyWork(src, this);
		} catch (IpException e) {
			ProxyReplace.replace(this);
			return tasks;
		}

		try {
			// 初始化 必须传入url 生成主键id
			project = new Project(getUrl());
		} catch (MalformedURLException | URISyntaxException e) {
			logger.error("Error extract url: {}, ", getUrl(), e);
		}


		if (src.contains("操作失败请稍后重试")) {
			try {
				tasks.add(new ProjectTask(getUrl()));
				return tasks;
			} catch (MalformedURLException | URISyntaxException e) {
				e.printStackTrace();
			}
		}
		// TODO 补充示例页面 url: http://task.zbj.com/12919315/，http://task.zbj.com/9790967/
		// A 无法请求页面内容
		if (pageAccessible(src)) {

			String header;
			try {
				// #headerNavWrap > div:nth-child(1) > div > div.header-nav-sub-title
				header = driver.findElement(By.cssSelector("#headerNavWrap > div:nth-child(1) > div > div.header-nav-sub-title")).getText();

			} catch (NoSuchElementException e) {
				return tasks;
			}

			// B1 页面格式1 ：http://task.zbj.com/12954152/
			if (pageType(header) == PageType.OrderDetail) {

				logger.trace("Model: {}, Type: {}, URL: {}", Project.class.getSimpleName(), PageType.OrderDetail.name(), getUrl());

				pageOne(src, driver, header, tasks);

				try {
					project.insert();
				} catch (Exception e) {
					logger.error("insert error for project", e);
				}
			}
			// B2 页面格式2 ：http://task.zbj.com/12954086/
			else if (pageType(header) == PageType.ReqDetail) {

				pageTwo(src, driver, header, tasks);

				try {
					project.insert();
				} catch (Exception e) {
					logger.error("insert error for project", e);
				}
			}
		}
		return tasks;
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
	 * @param driver
	 */
	public void projectStateOne(WebDriver driver) {

		try {
			// 项目状态
			project.current_status = driver.findElement(By.cssSelector("#j-content > div > div.taskmode-block.clearfix"))
					.findElement(By.className("modecont")).findElement(By.className("cur"))
					.findElement(By.tagName("p")).getText().split("2")[0];
		}
		catch (Exception e) {
			// 已经完成
			project.current_status = "评价";
		}
		try {
			try {
				// 剩余时间
				project.remaining_time = DateFormatUtil.parseTime(driver.findElement(By.cssSelector("#j-content > div > div.taskmode-block.clearfix"))
						.findElement(By.className("modecont"))
						.findElement(By.className("cur"))
						.findElement(By.className("taskmode-clock"))
						.getAttribute("data-difftime"));
			}
			catch (ParseException e) {
				e.printStackTrace();
			}
		}
		catch (NoSuchElementException e) {
			// 无法找到剩余时间
			project.remaining_time = null;
		}
	}

	/**
	 * pagetwo
	 * 项目状态， 时间
	 * @param driver
	 */
	public void projectStateTwo(WebDriver driver) {

		try {
			project.current_status = driver.findElement(By.cssSelector("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-blockr > div.timeline > div > div"))
					.findElement(By.className("current")).findElement(By.tagName("p")).getText();
		} catch (Exception e) {
			project.current_status = "评价";
		}
		// 若无此项，则为空
		try {
			try {
				project.remaining_time = DateFormatUtil.parseTime(driver.findElement(By.cssSelector("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block > div.timeline > div > div"))
						.findElement(By.className("current")).findElement(By.className("clock")).getAttribute("data-difftime"));
			}
			catch (ParseException e) {
				e.printStackTrace();
			}
		}
		catch (NoSuchElementException e) {
			project.remaining_time = null;
		}

		try {
			project.pubdate = DateFormatUtil.parseTime(driver.findElement(By.cssSelector("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block > div.wrapper.header-block-div > p.task-describe > span:nth-child(2)"))
					.findElement(By.tagName("b")).getText());
		}
		catch (ParseException e) {
			e.printStackTrace();
		}
	}

	/**
	 *
	 * @param driver
	 */
	public void getTendererIdName(WebDriver driver, String src) {

		String link = driver.findElement(By.cssSelector("#j-content > div > div.user-toltit > dl")).findElement(By.tagName("img")).getAttribute("src").split("\\.")[2];

		String[] links = link.split("/");

		String ss = links[4].split("_")[2];

		project.tenderer_id = Integer.parseInt(links[1] + links[2] + links[3] + ss)+"";

		project.tenderer_name = driver.findElement(By.cssSelector("#j-content > div > div.user-toltit > dl")).findElement(By.tagName("img")).getAttribute("alt");

		Pattern pattern = Pattern.compile("<div class=\"taskmode-inline\" id=\"reward-all\">\\s+赏金分配：<em class=\"gray6\">(?<rewardType>.+?)</em>");
		Matcher matcher = pattern.matcher(src);
		if (matcher.find()) {
			project.reward_type = org.tfelab.txt.StringUtil.removeHTML(matcher.group("rewardType"));
		}

	}

	/**
	 *
	 * @param driver
	 * @param src
	 */
	public void finishProject(WebDriver driver, String src) {
		// 1 采集时刻投标人数
		// 1.1 项目已完成
		if (src.contains("<div class=\"banner-task-summary clearfix\">")) {

			try {
				project.bidder_num = getInt(driver,
						"#anytime-back > div.banner-task-summary.clearfix > div.summary-right > h4:nth-child(2) > em:nth-child(2)",
						"个|名");
			}
			catch (NoSuchElementException e) {
				project.bidder_num = getInt(driver,
						"body > div.main.task-details > div.main-con.user-page > div.banner-task-summary.clearfix > div.summary-right.clearfix > h4:nth-child(2) > em:nth-child(2)",
						"个|名");
			}
			project.bidder_total_num = project.bidder_num;
		}
		// 1.2 项目未完成
		else {

			if (src.contains("该需求可接受")) {
				project.bidder_total_num = StringUtil.getBidderTotalNum(driver,
						"#j-ibid-list > div > div.ibid-total.clearfix > b:nth-child(1)");

				project.bidder_num = StringUtil.getBidderNum(driver,
						"#j-ibid-list > div > div.ibid-total.clearfix",
						"b");
			}
		}
	}

	/**
	 * 页面格式1
	 * 获取信息
	 * @param src 页面源
	 * @param driver
	 * @param head 页面格式
	 * @param tasks
	 */
	public void pageOne(String src, WebDriver driver, String head, List<Task> tasks) {

		// 项目是否可投标，以及投标数量
		finishProject(driver, src);

		project.title = getString(driver, "#ed-tit > div.tctitle.clearfix > h1", "");

		project.area = getString(driver, "#j-receiptcon > span.ads", "");

		project.origin = getString(driver, "#j-receiptcon > a", "");

		// body > div.main.task-details > div.grid > ul
		project.category = getString(driver, "body > div.main.task-details > div.grid > ul", "");

		project.type = head;

		// TODO 需要额外处理图片, 下载
		String description_src = driver.findElement(By.cssSelector("#work-more"))
				.getAttribute("innerHTML")
				.replaceAll("<a.+?>查看全部</a>","");

		project.description = download(description_src);

		project.time_limit = StringUtil.getTimeSpan(StringUtil.detectTimeSpanString(project.description));

		// 预算处理  #ed-tit > div.micon > div.fl.money-operate > p > u
		double[] budget = StringUtil.budget_all(driver,
				"#ed-tit > div.micon > div.fl.money-operate > p > u",
				project.description);
		project.budget_lb = budget[0];
		project.budget_up = budget[1];

		// 发布时间
		try {
			project.pubdate = DateFormatUtil.parseTime(driver.findElement(By.cssSelector("#j-receiptcon > span.time")).getText());
		}
		catch (ParseException e) {
			project.pubdate = null;
		}

		// 获取招标人id
		getTendererIdName(driver, src);

		// 项目状态 剩余时间
		projectStateOne(driver);

		project.trade_type = driver.findElement(By.cssSelector("#j-content > div > div.taskmode-block.clearfix > div.header")).findElement(By.tagName("em")).getText();

		// 进入雇主页
		try {
			tasks.add(new TendererTask("https://home.zbj.com/" + project.tenderer_id));
		} catch (MalformedURLException | URISyntaxException e) {
			logger.error("Error extract url: {}, ", "http://home.zbj.com/" + project.tenderer_id, e);
		}
	}

	/**
	 * 页面格式2
	 * 数据采取
	 * @param src
	 * @param driver
	 * @param head
	 * @param tasks
	 */
	public void pageTwo(String src, WebDriver driver, String head, List<Task> tasks) {

		project.type = head;

		project.title = getString(driver,
				"#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block > div.wrapper.header-block-div > h1", "");

		project.req_no = getString(driver,
				"#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block > div.wrapper.header-block-div > p.task-describe > span:nth-child(1) > b", "");

		project.category = getString(driver,
				"#utopia_widget_3",
				"");

		String description_src = driver.findElement(By.cssSelector("#trade-content > div.page-info-content.clearfix > div.main-content")).findElement(By.className("order-header-block"))
				.findElement(By.className("task-detail"))
				.findElement(By.className("task-detail-content"))
				.getAttribute("innerHTML").replaceAll("<a.+?>显示全部</a>","")
				.replace(">\\s+<","><").replaceAll("\\s+<","<").replaceAll(">\\s+",">");

		// 下载
		project.description = download(description_src);

		project.time_limit = StringUtil.getTimeSpan(StringUtil.detectTimeSpanString(project.description));


		// 获取地点，来源
		Pattern pattern = Pattern.compile("<div class=\"more-info\"><span><i class=\"lbs\"></i>(?<area>.+?)</span><span><i></i>来自：(?<from>.+?)</span></div>");
		Matcher matcher = pattern.matcher(description_src);
		if (matcher.find()) {
			project.area = matcher.group("area");
			project.origin = matcher.group("from");
		}

		Pattern pattern_origin = Pattern.compile("<div class=\"more-info\"><span><i></i>来自：(?<from>.+?)</span></div>");
		Matcher matcher_origin = pattern_origin.matcher(description_src);
		if (matcher_origin.find()) {
			project.origin = matcher_origin.group("from");
		}

		// 预算处理
		try {
			double[] budget = StringUtil.budget_all(driver,
					"#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner > div.wrapper.header-block-div > p.mb4.price-describe > span:nth-child(1) > b",
					project.description);
			project.budget_lb = budget[0];
			project.budget_up = budget[1];

		}
		catch (NoSuchElementException e) {
			project.budget_lb = 0;
			project.budget_up = 0;
		}

		// 项目状态，剩余时间
		projectStateTwo(driver);

		// 投标总数与投标人数
		try {

			project.bidder_total_num = StringUtil.getBidderTotalNum(driver,
					"#taskTabs > div > div:nth-child(1) > div > div.task-wantbid-launch > p.data-task-info > span:nth-child(1)");
			project.bidder_num = StringUtil.getBidderNum(driver,
					"#taskTabs > div > div:nth-child(1) > div > div.task-wantbid-launch > p.data-task-info",
					"span");
		}
		catch (NoSuchElementException e) {
		}

		try {
			project.status = getString(driver,"#trade-content > div.page-info-content.clearfix > div.main-content > div.header-banner > i", "");

		} catch (NoSuchElementException e) {
			System.err.println("status is null");
		}

	}
}
