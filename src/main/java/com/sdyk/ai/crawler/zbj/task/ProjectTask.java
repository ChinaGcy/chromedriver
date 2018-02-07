package com.sdyk.ai.crawler.zbj.task;

import com.sdyk.ai.crawler.zbj.Crawler;
import com.sdyk.ai.crawler.zbj.Helper;
import com.sdyk.ai.crawler.zbj.StringUtil;
import com.sdyk.ai.crawler.zbj.model.Project;
import com.sdyk.ai.crawler.zbj.model.Tenderer;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.tfelab.db.Refacter;
import org.tfelab.io.requester.account.AccountWrapper;
import org.tfelab.io.requester.account.AccountWrapperImpl;
import org.tfelab.io.requester.chrome.ChromeDriverRequester;
import org.tfelab.txt.DateFormatUtil;
import reactor.core.support.UUIDUtils;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.Driver;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 甲方需求详情
 */
public class ProjectTask extends Task {


	public ProjectTask(String url) throws MalformedURLException, URISyntaxException {
		super(url);
	}

	public List<Task> postProc(WebDriver driver) throws ParseException, URISyntaxException, MalformedURLException {

		int i = 1;
		String src = getResponse().getText();
		Project project = new Project(getUrl());
		List<Task> tasks = new ArrayList<Task>();

		if ((!src.contains("暂时无法查看")) && (!src.contains("您无权查看此任务"))) {

			String head = driver.findElement(By.cssSelector("#headerNavWrap > div:nth-child(1) > div > div.header-nav-sub-title")).getText();

			if (head.equals("订单详情")) {
				/*
				获取标题
				 */
				project.title = driver.findElement(By.cssSelector("#ed-tit > div.tctitle.clearfix > h1")).getText();

				project.area = driver.findElement(By.cssSelector("#j-receiptcon > span.ads")).getText();

				project.from_ = driver.findElement(By.cssSelector("#j-receiptcon > a")).getText();

				project.category = driver.findElement(By.cssSelector("body > div.main.task-details > div.grid > ul")).getText();

				project.type = head;


				/*
				TODO 需要额外处理图片
				 */
				String description_src = driver.findElement(By.cssSelector("#work-more")).getAttribute("innerHTML").replaceAll("<a.+?>查看全部</a>","");

				Set<String> img_urls = new HashSet<>();

				project.description = StringUtil.cleanContent(description_src, img_urls);

				project.time_limit = StringUtil.getTimeSpan(StringUtil.detectTimeSpanString(project.description));

				/**
				 * 预算处理
				 */
				//1.描述中拿预算
				double budget_all = StringUtil.detectBudget(project.description);
				//2.判断拿到的预算是否为0，如果拿不到就从预算栏中获取预算；拿到预算直接赋值。
				if (budget_all == 0) {

					//3.拿到预算栏中的数据
					String budget = driver.findElement(By.cssSelector("#ed-tit")).findElement(By.className("micon"))
							.findElement(By.className("money-operate ")).findElement(By.tagName("u")).getText();

					//4.判断格式
					if (budget.equals("可议价")) {

						project.budget_lb = 0;
						project.budget_up = 0;
					} else if (budget.contains("￥") && budget.contains("-")) {

						String pr = budget.split("￥")[1];
						String price[] = pr.split("-");
						project.budget_lb = Double.parseDouble(price[0]);
						project.budget_up = Double.parseDouble(price[1]);
					} else if (budget.contains("￥")) {

						String price[] = budget.split("￥");
						project.budget_lb = Double.parseDouble(price[1]);
						project.budget_up = Double.parseDouble(price[1]);
					}
				}
				else {

					project.budget_up = budget_all;
					project.budget_lb = budget_all;
				}
				//发布时间
				project.pubdate = DateFormatUtil.parseTime(driver.findElement(By.cssSelector("#j-receiptcon > span.time")).getText());


				if (src.contains("该需求可接受")) {

					project.bidder_total_num = Integer.parseInt(

							driver.findElement(By.cssSelector("#j-ibid-list > div > div.ibid-total.clearfix"))
									.findElements(By.tagName("b")).get(0).getText());
					if (driver.findElement(By.cssSelector("#j-ibid-list > div > div.ibid-total.clearfix"))
							.findElements(By.tagName("b")).size() > 1) {

						project.bidder_num = Integer.parseInt(
								driver.findElement(By.cssSelector("#j-ibid-list > div > div.ibid-total.clearfix"))
										.findElements(By.tagName("b")).get(1).getText());
					} else {

						project.bidder_num = Integer.parseInt(
								driver.findElement(By.cssSelector("#j-ibid-list > div > div.ibid-total.clearfix"))
										.findElements(By.tagName("b")).get(0).getText());
					}
				} else {

					project.bidder_total_num = 0;
					project.bidder_num = 0;
				}
				/*
				获取招标人id
				 */
				String link = driver.findElement(By.cssSelector("#j-content > div > div.user-toltit > dl")).findElement(By.tagName("img")).getAttribute("src").split("\\.")[2];

				String[] links = link.split("/");

				String ss = links[4].split("_")[2];

				project.tenderer_id = links[1] + links[2] + links[3] + ss;

				project.tenderer_name = driver.findElement(By.cssSelector("#j-content > div > div.user-toltit > dl")).findElement(By.tagName("img")).getAttribute("alt");

				Pattern pattern = Pattern.compile("<div class=\"taskmode-inline\" id=\"reward-all\">\\s+赏金分配：<em class=\"gray6\">(?<rewardType>.+?)</em>");

				Matcher matcher = pattern.matcher(src);
				if (matcher.find()) {

					project.reward_type = org.tfelab.txt.StringUtil.removeHTML(matcher.group("rewardType"));
				}

				try {
					project.current_status = driver.findElement(By.cssSelector("#j-content > div > div.taskmode-block.clearfix"))
							.findElement(By.className("modecont")).findElement(By.className("cur"))
							.findElement(By.tagName("p")).getText().split("2")[0];

					project.remaining_time = DateFormatUtil.parseTime(driver.findElement(By.cssSelector("#j-content > div > div.taskmode-block.clearfix"))
							.findElement(By.className("modecont")).findElement(By.className("cur"))
							.findElement(By.className("taskmode-clock")).getAttribute("data-difftime"));
				} catch (Exception e) {
					project.current_status = "评价状态";
				}

				project.trade_type = driver.findElement(By.cssSelector("#j-content > div > div.taskmode-block.clearfix > div.header")).findElement(By.tagName("em")).getText();

				//进入雇主页
				tasks.add(new TendererTask("http://home.zbj.com/" + project.tenderer_id.substring(1)));

			}
			else if (head.equals("需求详情")) {

				project.type = head;

				project.title = driver.findElement(By.cssSelector("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner > div.wrapper.header-block-div > h1")).getText();

				project.req_no = driver.findElement(By.cssSelector("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner > div.wrapper.header-block-div > p.task-describe > span:nth-child(1) > b"))
						.getText();

				project.category = driver.findElement(By.cssSelector("#utopia_widget_3")).getText();

				String description_src = driver.findElement(By.cssSelector("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner")).findElement(By.className("task-detail"))
						.getAttribute("innerHTML").replaceAll("<a.+?>查看全部</a>","")
						.replaceAll(">\\s+<","><").replaceAll("\\s+<","<").replaceAll(">\\s+",">");

				Set<String> img_urls = new HashSet<>();
				project.description = StringUtil.cleanContent(description_src,img_urls);

				project.time_limit = StringUtil.getTimeSpan(StringUtil.detectTimeSpanString(project.description));

				/*
				获取地点，来源
				 */
				Pattern pattern = Pattern.compile("<div class=\"more-info\"><span><i class=\"lbs\"></i>(?<area>.+?)</span><span><i></i>来自：(?<from>.+?)</span></div>");
				Matcher matcher = pattern.matcher(description_src);
				if (matcher.find()) {
					project.area = matcher.group("area");
					project.from_ = matcher.group("from");
				}
				Pattern pattern1 = Pattern.compile("<div class=\"more-info\"><span><i></i>来自：(?<from>.+?)</span></div>");
				Matcher matcher1 = pattern1.matcher(description_src);
				if (matcher1.find()) {
					project.from_ = matcher1.group("from");
				}

				/**
				 * 预算处理
				 */
				double budget_All = StringUtil.detectBudget(project.description);
				if (budget_All == 0) {
					String budget = driver.findElement(By.cssSelector("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner > div.wrapper.header-block-div > p.mb4.price-describe"))
							.findElement(By.tagName("span")).getText();

					if (budget.contains("￥")) {
						if (budget.contains("-")) {
							project.budget_lb = Double.parseDouble(budget.split("￥")[1].split("-")[0]);
							project.budget_up = Double.parseDouble(budget.split("￥")[1].split("-")[1]);
						} else {
							project.budget_lb = Double.parseDouble(budget.split("￥")[1]);
							project.budget_up = project.budget_lb;
						}
					} else {
						project.budget_up = 0;
						project.budget_lb = 0;
					}
				}
				else {
					project.budget_up = budget_All;
					project.budget_lb = budget_All;
				}
				project.current_status = driver.findElement(By.cssSelector("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner > div.timeline > div > div"))
						.findElement(By.className("current")).findElement(By.tagName("p")).getText();

				project.remaining_time = DateFormatUtil.parseTime(driver.findElement(By.cssSelector("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner > div.timeline > div > div"))
						.findElement(By.className("current")).findElement(By.className("clock")).getAttribute("data-difftime"));

				project.pubdate = DateFormatUtil.parseTime(driver.findElement(By.cssSelector("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner > div.wrapper.header-block-div > p.task-describe > span:nth-child(2)"))
						.findElement(By.tagName("b")).getText());

				/*
				投标总数与投标人数
				 */
				try {

					project.bidder_total_num = Integer.parseInt(
							driver.findElement(By.cssSelector("#taskTabs > div > div:nth-child(1) > div > div.task-wantbid-launch > p.data-task-info"))
									.findElements(By.className("datanum")).get(0).getText());

					if (driver.findElement(By.cssSelector("#taskTabs > div > div:nth-child(1) > div > div.task-wantbid-launch > p.data-task-info"))
							.findElements(By.className("datanum")).size() > 1) {
						project.bidder_num = Integer.parseInt(
								driver.findElement(By.cssSelector("#taskTabs > div > div:nth-child(1) > div > div.task-wantbid-launch > p.data-task-info"))
										.findElements(By.className("datanum")).get(1).getText());
					} else {
						project.bidder_num = Integer.parseInt(
								driver.findElement(By.cssSelector("#taskTabs > div > div:nth-child(1) > div > div.task-wantbid-launch > p.data-task-info"))
										.findElements(By.className("datanum")).get(0).getText());
					}
				} catch (Exception e) {
					project.bidder_num = 0;
					project.bidder_total_num = 0;

				}
				project.status = driver.findElement(By.cssSelector("#trade-content > div.page-info-content.clearfix > div.main-content > div.header-banner > i"))
						.getText();
			}

			try {
				project.insert();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return tasks;
	}


	/**
	 * 测试方法
	 */
	public static void main(String[] args) throws Exception {

		Refacter.dropTable(Project.class);
		Refacter.createTable(Project.class);
/*
		AccountWrapper accountWrapper = new AccountWrapperImpl();

		Crawler crawler = Crawler.getInstance();

		List<Task> ts = new ArrayList<Task>();

		crawler.addTask(ts);*/

	}

}
