package com.sdyk.ai.crawler.zbj.task.modelTask;

import com.sdyk.ai.crawler.zbj.exception.IpException;
import com.sdyk.ai.crawler.zbj.model.ServiceSupplier;
import com.sdyk.ai.crawler.zbj.task.Task;
import com.sdyk.ai.crawler.zbj.task.scanTask.CaseScanTask;
import com.sdyk.ai.crawler.zbj.task.scanTask.WorkScanTask;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ServiceSupplierTask extends Task {

	// 实例化
	ServiceSupplier serviceSupplier;

	public ServiceSupplierTask(String url) throws MalformedURLException, URISyntaxException {
		super(url);
		this.priority = Priority.high;

	}

	public List<Task> postProc(WebDriver driver) {

		String src = getResponse().getText();
		List<Task> tasks = new ArrayList<Task>();

		// 判断是否被禁
		try {
			ProxyReplace.proxyWork(src, this);
		} catch (IpException e) {
			ProxyReplace.replace(this);
			return tasks;
		}

		serviceSupplier = new ServiceSupplier(getUrl());

		shareData(driver, src);

		// 判断是哪个页面格式
		if (src.contains("as.zbjimg.com/static/nodejs-tianpeng-utopiacs-web/widget/tp-header/img/tianpeng-logo_31addeb.png")) {
			// 天棚网服务商信息
			pageOne(src, serviceSupplier, driver);
		} else {
			// 猪八戒服务商信息
			pageTwo(src, serviceSupplier, driver);
		}

		// 服务商评价地址：http://shop.zbj.com/evaluation/evallist-uid-13046360-type-1-page-5.html
		tasks.add(ServiceRatingTask.generateTask("https://shop.zbj.com/evaluation/evallist-uid-"+ serviceSupplier.website_id +"-type-1-page-",1));
		tasks.add(CaseScanTask.generateTask(getUrl(),1));
		tasks.add(WorkScanTask.generateTask(getUrl(), 1));

		try {
			serviceSupplier.insert();
		} catch (Exception e) {
			logger.error("insert/update error {}" , e);
		}
		return tasks;
	}


	/**
	 *
	 * @param driver
	 * @param src
	 */
	public void shareData(WebDriver driver, String src) {

		serviceSupplier.website_id = getUrl().split("/")[3];

		// 获取等级
		if (src.contains("<img align=\"absmiddle\" src=\"https://t5.zbjimg.com/t5s/common/img/user-level/level-")) {

			String s = driver.findElement(By.cssSelector("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name"))
					.findElements(By.tagName("img")).get(0).getAttribute("src");

			serviceSupplier.grade = s.split("/level-")[1].split(".png")[0];
		}

		// 获取联系方式
		contactWay(src, serviceSupplier, driver);

		// 获取店铺流量
		if (src.contains("店铺流量")) {
			// body > div.diy-content.preview.J-refuse-external-link > div > div.diy-sec.diy.w990 > div.case2-left > div:nth-child(5) > div.my-home > p > b:nth-child(5)
			List<WebElement> we = driver.findElement(By.cssSelector("body > div.diy-content.preview.J-refuse-external-link > div > div.diy-sec.diy.w990 > div.case2-left")).findElements(By.className("part"));
			for (WebElement web : we) {
				if (web.getText().contains("店铺流量") && web.getText().contains("收藏量")) {
					serviceSupplier.collection_num = Integer
							.parseInt(web.findElements(By.className("orange")).get(2).getText());
				}
			}
		}
	}

	/**
	 * 天蓬网服务商详情
	 * @param src
	 * @param serviceSupplier
	 * @param driver
	 */
	public void pageOne(String src, ServiceSupplier serviceSupplier, WebDriver driver) {

		serviceSupplier.name = driver.findElement(By.cssSelector("body > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-desc > a > strong")).getText();
		if (src.contains("专业店")) {
			serviceSupplier.type = "专业店";
		}else if (src.contains("旗舰店")){
			serviceSupplier.type = "旗舰店";
		}

		List<WebElement> webElements = driver.findElement(By.cssSelector("body > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-evaluate.clearfix > div.shop-evaluate-det")).findElements(By.tagName("span"));
		serviceSupplier.service_quality = Double.parseDouble(webElements.get(0).getText());
		serviceSupplier.service_speed = Double.parseDouble(webElements.get(2).getText());
		serviceSupplier.service_attitude = Double.parseDouble(webElements.get(4).getText());

		try {

			serviceSupplier.good_rating_num = Integer.parseInt(driver.findElement(By.cssSelector("body > div.diy-content.preview.J-refuse-external-link > div > div.diy-sec.diy.w990 > div.case2-right > div:nth-child(3) > div.shop-evaluation-newstyle > div > table > tbody > tr > td.evaluation-num-list > ul:nth-child(1) > li:nth-child(1) > span.eval-num"))
					.getText());
			serviceSupplier.bad_rating_num = Integer.parseInt(driver.findElement(By.cssSelector("body > div.diy-content.preview.J-refuse-external-link > div > div.diy-sec.diy.w990 > div.case2-right > div:nth-child(3) > div.shop-evaluation-newstyle > div > table > tbody > tr > td.evaluation-num-list > ul:nth-child(1) > li.fl.eval-rate-li.last > span.eval-num"))
					.getText());
		} catch (NoSuchElementException e) {

		}

		webPageUcenter(driver);
	}

	/**
	 * 猪八戒服务商详情
	 * @param src
	 * @param serviceSupplier
	 * @param driver
	 */
	public void pageTwo(String src, ServiceSupplier serviceSupplier, WebDriver driver) {

		serviceSupplier.name = driver.findElement(By.cssSelector("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-desc.J-shop-desc > a > strong"))
				.getText();
		if (driver.findElement(By.cssSelector("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-desc.J-shop-desc"))
				.findElements(By.tagName("img")).size() >= 2) {
			serviceSupplier.type = "企业";
		}
		else {
			serviceSupplier.type = "个人";
		}
		webPageEvaluation(driver);

		webPageUcenter(driver);

		}

	/**
	 *服务商信息
	 * @param driver
	 */
	public void webPageUcenter(WebDriver driver) {
		driver.get("https://ucenter.zbj.com/rencai/view/" + getUrl().split("/")[3]);

		try {
			serviceSupplier.location = driver.findElement(By.cssSelector("#utopia_widget_3 > div.shop-center-tit.clearfix > span.fr.active-address"))
					.getText();
		} catch (NoSuchElementException e) {
			serviceSupplier.location = "";
		}

		serviceSupplier.skills = driver.findElement(By.cssSelector("#utopia_widget_5 > p.label-box-wrap"))
				.getText();
		serviceSupplier.rating = Integer.parseInt(driver.findElement(By.cssSelector("#power")).getText().replaceAll("", "0"));

		serviceSupplier.description = driver.findElement(By.cssSelector("#utopia_widget_3 > div.user-about > div > span")).getText();

		serviceSupplier.rating_num = Integer.parseInt(driver.findElement(By.cssSelector("#utopia_widget_4 > div > div.clearfix > span"))
				.getText().split("（")[1].split("）")[0]);
		String recommendation = driver.findElement(By.cssSelector("#evaluationwrap > ul.evaluation-option.clearfix.J-evaluation-option > li:nth-child(6) > a"))
				.getText();

		serviceSupplier.recommendation_num = Integer.parseInt(recommendation.split("\\(")[1].split("\\)")[0]);

	}

	/**
	 *交易
	 * @param driver
	 */
	// TODO 天棚网可能有问题 页面具体分析 解决
	public void webPageEvaluation(WebDriver driver) {

		driver.get(getUrl() + "evaluation.html");

		List<WebElement> webElements = driver
				.findElement(By.cssSelector("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-evaluate.clearfix > div.shop-evaluate-det"))
				.findElements(By.tagName("span"));

		serviceSupplier.service_quality = Double.parseDouble(webElements.get(0).getText());
		serviceSupplier.service_speed = Double.parseDouble(webElements.get(2).getText());
		serviceSupplier.service_attitude = Double.parseDouble(webElements.get(4).getText());

		serviceSupplier.revenue = Double.parseDouble(driver.findElement(By.cssSelector("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-balance > span:nth-child(1)"))
				.getText().replaceAll(",", ""));
		serviceSupplier.project_num = Integer.parseInt(driver.findElement(By.cssSelector("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-balance > span:nth-child(2)"))
				.getText());
		serviceSupplier.success_ratio = (float) (1 - Double.parseDouble(driver.findElement(By.cssSelector("body > div.main > div > div.wk-r > div.w-con.shop-service-wrap > div.con.clearfix > div.shop-service-bd > div.fl.shop-service-left > div:nth-child(3) > div.td.td4"))
				.getText().replaceAll("%", "")) * 0.01);
		serviceSupplier.good_rating_num = Integer.parseInt(driver.findElement(By.cssSelector("body > div.main > div > div.wk-r > div:nth-child(3) > div.con.clearfix > div.shop-comment-bd > div.shop-comment-l > div > div:nth-child(4) > span:nth-child(1)"))
				.getText());
		serviceSupplier.bad_rating_num = Integer.parseInt(driver.findElement(By.cssSelector("body > div.main > div > div.wk-r > div:nth-child(3) > div.con.clearfix > div.shop-comment-bd > div.shop-comment-l > div > div:nth-child(4) > span:nth-child(3)"))
				.getText());
	}

	/**
	 * 获取联系方式
	 * @param src
	 * @param serviceSupplier
	 * @param driver
	 */
	public void contactWay(String src, ServiceSupplier serviceSupplier, WebDriver driver) {

		if (src.contains("<div class=\"shop-fix-im-qq\">")) {
			try {

				List<WebElement> list_qq = driver.findElement
						(By.cssSelector("body > div.shop-fixed-im.sidebar-show > div.shop-fixed-im-hover.shop-customer.j-shop-fixed-im"))
						.findElement(By.className("shop-fix-im-qq"))
						.findElements(By.className("qq-item"));
				String qq = "";
				for (WebElement webElement : list_qq) {
					qq = qq + " " + webElement.findElement(By.tagName("a")).getAttribute("data-qq");
				}
				serviceSupplier.qq = qq;
			} catch (Exception e) {
			}

			try {
				List<WebElement> list_phone = driver.findElement
						(By.cssSelector("body > div.shop-fixed-im.sidebar-show > div.shop-fixed-im-hover.shop-customer.j-shop-fixed-im"))
						.findElement(By.className("shop-fix-im-qq"))
						.findElements(By.className("time-item"));
				String phone = "";
				for (WebElement webElement : list_phone) {
					phone = phone + " " + webElement.findElement(By.tagName("a")).getAttribute("data-phone");
				}
				serviceSupplier.cellphone = phone;
			} catch (Exception e) {
			}
		}
	}
}