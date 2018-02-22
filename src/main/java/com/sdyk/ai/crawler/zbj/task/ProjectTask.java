package com.sdyk.ai.crawler.zbj.task;

import com.sdyk.ai.crawler.zbj.ChromeDriverWithLogin;
import com.sdyk.ai.crawler.zbj.StringUtil;
import com.sdyk.ai.crawler.zbj.model.Binary;
import com.sdyk.ai.crawler.zbj.model.Project;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.tfelab.db.Refacter;
import org.tfelab.io.requester.BasicRequester;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;
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


	public ProjectTask(String url) throws MalformedURLException, URISyntaxException {
		super(url);
	}

	public List<Task> postProc(WebDriver driver) throws ParseException, URISyntaxException, MalformedURLException {

		int i = 1;
		String src = getResponse().getText();
		Project project = new Project(getUrl());
		List<Task> tasks = new ArrayList<Task>();

		if ((!src.contains("无法查看")) && (!src.contains("无权查看")) && (!src.contains("不能查看")) ) {

			String head = driver.findElement(By.cssSelector("#headerNavWrap > div:nth-child(1) > div > div.header-nav-sub-title")).getText();

			if (head.equals("订单详情")) {
				if (src.contains("<div class=\"banner-task-summary clearfix\">")) {
					try {
						project.bidder_num = Integer.parseInt(driver.findElement(By.cssSelector("#anytime-back > div.banner-task-summary.clearfix > div.summary-right > h4:nth-child(2) > em:nth-child(2)"))
								.getText().replaceAll("个","").replaceAll("名", ""));
					} catch (NoSuchElementException e) {
						project.bidder_num = Integer.parseInt(driver.findElement(By.cssSelector("body > div.main.task-details > div.main-con.user-page > div.banner-task-summary.clearfix > div.summary-right.clearfix > h4:nth-child(2) > em:nth-child(2)"))
								.getText().replaceAll("个","").replaceAll("名", ""));
					}

					project.bidder_total_num = 0;

				} else {

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

				}

				/*
				获取标题
				 */
				project.title = driver.findElement(By.cssSelector("#ed-tit > div.tctitle.clearfix > h1")).getText();

				project.area = driver.findElement(By.cssSelector("#j-receiptcon > span.ads")).getText();

				project.from_ = driver.findElement(By.cssSelector("#j-receiptcon > a")).getText();

				project.category = driver.findElement(By.cssSelector("body > div.main.task-details > div.grid > ul")).getText();

				project.type = head;


				/*
				TODO 需要额外处理图片, 下载
				 */
				String description_src = driver.findElement(By.cssSelector("#work-more")).getAttribute("innerHTML").replaceAll("<a.+?>查看全部</a>","");

				Set<String> img_urls = new HashSet<>();
				Set<String> a_urls = new HashSet<>();

				String des_src = StringUtil.cleanContent(description_src, img_urls, a_urls);

				//处理图片
				for (String img : img_urls) {

					if (img.equals("http://t5.zbjimg.com/t5s/common/img/fuwubao/wan-detail.png")) {
						continue;
					}
					try {
						org.tfelab.io.requester.Task t_ = new org.tfelab.io.requester.Task(img);
						BasicRequester.getInstance().fetch(t_);
						String fileName = null;
						Binary binary = new Binary();
						binary.src = t_.getResponse().getSrc();

						if (t_.getResponse().getHeader() != null) {
							for (Map.Entry<String, List<String>> entry : t_.getResponse().getHeader().entrySet()) {

								if (entry.getKey() != null && entry.getKey().toLowerCase().equals("content-type")) {
									binary.content_type = entry.getValue().toString();
								}

								if (entry.getKey() != null && entry.getKey().toLowerCase().equals("content-disposition")) {

									fileName = entry.getValue().toString()
											.replaceAll("^.*?filename\\*=utf-8' '", "")
											.replaceAll("\\].*?$", "");
									fileName = java.net.URLDecoder.decode(fileName, "UTF-8");

									if(fileName == null || fileName.length() == 0) {

										fileName = entry.getValue().toString()
												.replaceAll("^.*?\"", "")
												.replaceAll("\".*$", "");
									}

								}
							}
						}
						if(fileName == null) {
							fileName = t_.getUrl().replaceAll("^.+/", "");
						}
						binary.file_name = fileName;
						binary.id = org.tfelab.txt.StringUtil.byteArrayToHex(org.tfelab.txt.StringUtil.uuid(img));
						binary.url = project.url;
						des_src = des_src.replace(img, binary.file_name).replaceAll("&s\\.w=\\d+&s\\.h=\\d+","");
						binary.insert();

					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}

				//处理下载
				for (String a : a_urls) {

					if (!a.contains("key=")) {
						continue;
					}

					try {
						org.tfelab.io.requester.Task t_ =null;
						if (!a.contains("https")) {
							String a1 = a.replace("http", "https");
							t_ = new org.tfelab.io.requester.Task(a1);
						}else {
							t_ = new org.tfelab.io.requester.Task(a);
						}
						BasicRequester.getInstance().fetch(t_);
						String fileName = null;
						Binary binary = new Binary();
						binary.src = t_.getResponse().getSrc();

						if (t_.getResponse().getHeader() != null) {
							for (Map.Entry<String, List<String>> entry : t_.getResponse().getHeader().entrySet()) {

								if (entry.getKey() != null && entry.getKey().toLowerCase().equals("content-type")) {
									binary.content_type = entry.getValue().toString();
								}

								if (entry.getKey() != null && entry.getKey().toLowerCase().equals("content-disposition")) {

									fileName = entry.getValue().toString()
											.replaceAll("^.*?filename\\*=utf-8' '", "")
											.replaceAll("\\].*?$", "");
									fileName = java.net.URLDecoder.decode(fileName, "UTF-8");

									if(fileName == null || fileName.length() == 0) {

										fileName = entry.getValue().toString()
												.replaceAll("^.*?\"", "")
												.replaceAll("\".*$", "");
									}

								}
							}
						}
						if(fileName == null) {
							fileName = t_.getUrl().replaceAll("^.+/", "");
						}
						binary.file_name = fileName;
						binary.id = org.tfelab.txt.StringUtil.byteArrayToHex(org.tfelab.txt.StringUtil.uuid(a));
						binary.url = project.url;
						des_src = des_src.replace(a, binary.file_name);
						binary.insert();

					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}

				project.description = des_src;

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



				/*
				获取招标人id
				 */
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

				try {
					project.current_status = driver.findElement(By.cssSelector("#j-content > div > div.taskmode-block.clearfix"))
							.findElement(By.className("modecont")).findElement(By.className("cur"))
							.findElement(By.tagName("p")).getText().split("2")[0];
				} catch (Exception e) {
					project.current_status = "评价状态";
				}

				try {
					project.remaining_time = DateFormatUtil.parseTime(driver.findElement(By.cssSelector("#j-content > div > div.taskmode-block.clearfix"))
							.findElement(By.className("modecont")).findElement(By.className("cur"))
							.findElement(By.className("taskmode-clock")).getAttribute("data-difftime"));

				} catch (NoSuchElementException e) { }

				project.trade_type = driver.findElement(By.cssSelector("#j-content > div > div.taskmode-block.clearfix > div.header")).findElement(By.tagName("em")).getText();

				//进入雇主页
				//tasks.add(new TendererTask("http://home.zbj.com/" + project.tenderer_id));

			}
			else if (head.equals("需求详情")) {

				project.type = head;

				project.title = driver.findElement(By.cssSelector("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner > div.wrapper.header-block-div > h1")).getText();

				project.req_no = driver.findElement(By.cssSelector("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner > div.wrapper.header-block-div > p.task-describe > span:nth-child(1) > b"))
						.getText();

				project.category = driver.findElement(By.cssSelector("#utopia_widget_3")).getText();

				String description_src = driver.findElement(By.cssSelector("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner")).findElement(By.className("task-detail"))
						.getAttribute("innerHTML").replaceAll("<a.+?>查看全部</a>","")
						.replace(">\\s+<","><").replaceAll("\\s+<","<").replaceAll(">\\s+",">");

				Set<String> img_urls = new HashSet<>();
				Set<String> a_urls = new HashSet<>();

				String des_src = StringUtil.cleanContent(description_src, img_urls, a_urls);

				//处理图片
				for (String img : img_urls) {

					if (img.equals("http://t5.zbjimg.com/t5s/common/img/fuwubao/wan-detail.png")) {
						continue;
					}
					try {
						org.tfelab.io.requester.Task t_ = new org.tfelab.io.requester.Task(img);
						BasicRequester.getInstance().fetch(t_);
						String fileName = null;
						Binary binary = new Binary();
						binary.src = t_.getResponse().getSrc();

						if (t_.getResponse().getHeader() != null) {
							for (Map.Entry<String, List<String>> entry : t_.getResponse().getHeader().entrySet()) {

								if (entry.getKey() != null && entry.getKey().toLowerCase().equals("content-type")) {
									binary.content_type = entry.getValue().toString();
								}

								if (entry.getKey() != null && entry.getKey().toLowerCase().equals("content-disposition")) {

									fileName = entry.getValue().toString()
											.replaceAll("^.*?filename\\*=utf-8' '", "")
											.replaceAll("\\].*?$", "");
									fileName = java.net.URLDecoder.decode(fileName, "UTF-8");

									if(fileName == null || fileName.length() == 0) {

										fileName = entry.getValue().toString()
												.replaceAll("^.*?\"", "")
												.replaceAll("\".*$", "");
									}

								}
							}
						}
						if(fileName == null) {
							fileName = t_.getUrl().replaceAll("^.+/", "");
						}
						binary.file_name = fileName;
						binary.id = org.tfelab.txt.StringUtil.byteArrayToHex(org.tfelab.txt.StringUtil.uuid(img));
						binary.url = project.url;
						des_src = des_src.replace(img, binary.file_name).replaceAll("&s\\.w=\\d+&s\\.h=\\d+","");
						binary.insert();

					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}

				//处理下载
				for (String a : a_urls) {

					if (!a.contains("key=")) {
						continue;
					}

					try {
						org.tfelab.io.requester.Task t_ =null;
						if (!a.contains("https")) {
							String a1 = a.replace("http", "https");
							t_ = new org.tfelab.io.requester.Task(a1);
						}else {
							t_ = new org.tfelab.io.requester.Task(a);
						}
						BasicRequester.getInstance().fetch(t_);
						String fileName = null;
						Binary binary = new Binary();
						binary.src = t_.getResponse().getSrc();

						if (t_.getResponse().getHeader() != null) {
							for (Map.Entry<String, List<String>> entry : t_.getResponse().getHeader().entrySet()) {

								if (entry.getKey() != null && entry.getKey().toLowerCase().equals("content-type")) {
									binary.content_type = entry.getValue().toString();

								}

								if (entry.getKey() != null && entry.getKey().toLowerCase().equals("content-disposition")) {

									fileName = entry.getValue().toString()
											.replaceAll("^.*?filename\\*=utf-8' '", "")
											.replaceAll("\\].*?$", "");
									fileName = java.net.URLDecoder.decode(fileName, "UTF-8");

									if(fileName == null || fileName.length() == 0) {

										fileName = entry.getValue().toString()
												.replaceAll("^.*?\"", "")
												.replaceAll("\".*$", "");
									}

								}
							}
						}
						if(fileName == null) {
							fileName = t_.getUrl().replaceAll("^.+/", "");
						}
						binary.file_name = fileName;
						binary.id = org.tfelab.txt.StringUtil.byteArrayToHex(org.tfelab.txt.StringUtil.uuid(a));
						binary.url = project.url;
						des_src = des_src.replace(a, binary.file_name);
						binary.insert();

					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}

				project.description = des_src;

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

				try {
					project.remaining_time = DateFormatUtil.parseTime(driver.findElement(By.cssSelector("#trade-content > div.page-info-content.clearfix > div.main-content > div.order-header-block.new-bid.header-block-with-banner > div.timeline > div > div"))
							.findElement(By.className("current")).findElement(By.className("clock")).getAttribute("data-difftime"));
				} catch (org.openqa.selenium.NoSuchElementException e) {
				}

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


		ChromeDriverAgent agent = new ChromeDriverWithLogin("zbj.com").login();

		Thread.sleep(1000);
		Task task = new ProjectTask("http://task.zbj.com/12909258/");

		Queue<Task> taskQueue = new LinkedList<>();
		taskQueue.add(task);
		while(!taskQueue.isEmpty()) {
			Task t = taskQueue.poll();
			if(t != null) {
				try {
					agent.fetch(t);
					for (Task t_ : t.postProc(agent.getDriver())) {
						taskQueue.add(t_);
						//agent.fetch(t_);
					}

				} catch (Exception e) {

					taskQueue.add(t);
				}
			}
		}

	}

}
