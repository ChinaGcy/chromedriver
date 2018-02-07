package com.sdyk.ai.crawler.zbj.task;

import com.sdyk.ai.crawler.zbj.model.ServiceSupplier;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.tfelab.db.Refacter;
import org.tfelab.txt.StringUtil;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceSupplierTask extends Task {

	public String type;

	public ServiceSupplierTask(String url) throws MalformedURLException, URISyntaxException {
		super(url);

	}

	public List<Task> postProc(WebDriver driver) throws Exception {

		String src = getResponse().getText();
		ServiceSupplier serviceSupplier = new ServiceSupplier();
		List<Task> tasks = new ArrayList<Task>();

		serviceSupplier.id = StringUtil.byteArrayToHex(StringUtil.uuid(getUrl()));
		serviceSupplier.website_id = getUrl().split("/")[3];
		serviceSupplier.url = this.getUrl();
		if (src.matches("<img align=\"absmiddle\" src=\"https://t5.zbjimg.com/t5s/common/img/user-level/level-\\d.png\">")) {
			Pattern pattern = Pattern.compile("<img align=\"absmiddle\" src=\"https://t5.zbjimg.com/t5s/common/img/user-level/level-(?<T>\\d+).png\">");
			Matcher matcher = pattern.matcher(src);
			serviceSupplier.grade = matcher.group("T");
		}

		if (src.matches("<div class=\"shop-fix-im-qq\">")) {
			try {

				List<WebElement> list_qq = driver.findElement
						(By.cssSelector("body > div.diy-content.preview.J-refuse-external-link > div > div.diy-sec.diy.w990 > div.case2-left > div:nth-child(2) > div.shop-fixed-im-hover.shop-customer.static-customer.j-shop-fixed-im > div.shop-fix-im-qq"))
						.findElements(By.className("qq-item"));
				for (WebElement webElement : list_qq) {
					serviceSupplier.qq = " " + webElement.findElement(By.tagName("a")).getAttribute("data-qq");
				}
			} catch (Exception e) {}

			try {
				List<WebElement> list_phone = driver.findElement
						(By.cssSelector("body > div.diy-content.preview.J-refuse-external-link > div > div.diy-sec.diy.w990 > div.case2-left > div:nth-child(2) > div.shop-fixed-im-hover.shop-customer.static-customer.j-shop-fixed-im > div.shop-fix-im-qq"))
						.findElements(By.className("time-item"));
				for (WebElement webElement : list_phone) {
					serviceSupplier.cellphone = " " + webElement.findElement(By.tagName("a")).getAttribute("data-phone");
				}
			} catch (Exception e) {}
		}


		if (this.getUrl().matches("http://shop.tianpeng.com/\\d/")) {
			serviceSupplier.name = driver.findElement(By.cssSelector("body > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-desc > a > strong")).getText();
		} else {
			serviceSupplier.name = driver.findElement(By.cssSelector("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-desc.J-shop-desc > a > strong")).getText();
			if (src.matches("<img src=\"https://t5.zbjimg.com/t5s/uc/img/ee-icon.png\" alt=\"企业标志\">\n")) {
				serviceSupplier.type = "企业";
			} else {
				serviceSupplier.type = "个人";
			}

		}

		driver.get(getUrl() + "evaluation.html");
		System.out.println(driver.getCurrentUrl());
		serviceSupplier.revenue = Double.parseDouble(driver.findElement(By.cssSelector("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-balance > span:nth-child(1)"))
				.getText());
		serviceSupplier.project_num = Integer.parseInt(driver.findElement(By.cssSelector("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-balance > span:nth-child(2)"))
				.getText());
		serviceSupplier.success_ratio = 1 - Float.parseFloat(driver.findElement(By.cssSelector("body > div.main > div > div.wk-r > div.w-con.shop-service-wrap > div.con.clearfix > div.shop-service-bd > div.fl.shop-service-left > div:nth-child(3) > div.td.td4"))
				.getText().replaceAll("%",""));
		serviceSupplier.good_rating_num = Integer.parseInt(driver.findElement(By.cssSelector("body > div.main > div > div.wk-r > div:nth-child(3) > div.con.clearfix > div.shop-comment-bd > div.shop-comment-l > div > div:nth-child(4) > span:nth-child(1)"))
				.getText());
		serviceSupplier.bad_rating_num = Integer.parseInt(driver.findElement(By.cssSelector("body > div.main > div > div.wk-r > div:nth-child(3) > div.con.clearfix > div.shop-comment-bd > div.shop-comment-l > div > div:nth-child(4) > span:nth-child(3)"))
				.getText());

		driver.get(getUrl() + "salerinfo.html");
		System.out.println(driver.getCurrentUrl());
		serviceSupplier.skills = com.sdyk.ai.crawler.zbj.StringUtil.cleanContent(driver.findElement(By.cssSelector("#utopia_widget_5 > p.label-box-wrap")).getText(),null);

		serviceSupplier.rating = Integer.parseInt(driver.findElement(By.cssSelector("#power")).getText());



		serviceSupplier.description = driver.findElement(By.cssSelector("#utopia_widget_3 > div.user-about > div > span")).getText();

		serviceSupplier.rating_num = Integer.parseInt(driver.findElement(By.cssSelector("#utopia_widget_4 > div > div.clearfix > span")).getText().split("（")[1].split("）")[0]);

		//服务商评价地址：http://shop.zbj.com/evaluation/evallist-uid-13046360-type-1-page-5.html

		try {
			serviceSupplier.insert();
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (getUrl().matches("http://shop.(.+?).com/\\d/")) {

			tasks.add(new CaseScanTask(getUrl(),"1"));
		}else {
			tasks.add(new CaseScanTask(getUrl()+serviceSupplier.website_id, "1"));
		}

		return tasks;
	}

	public static void main(String[] args) throws Exception {
		Refacter.dropTable(ServiceSupplier.class);
		Refacter.createTable(ServiceSupplier.class);

	}

}