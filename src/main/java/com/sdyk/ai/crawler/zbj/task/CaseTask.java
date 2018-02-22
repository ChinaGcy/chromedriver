package com.sdyk.ai.crawler.zbj.task;

import com.sdyk.ai.crawler.zbj.StringUtil;
import com.sdyk.ai.crawler.zbj.model.Case;
import com.sdyk.ai.crawler.zbj.model.Binary;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.tfelab.db.Refacter;
import org.tfelab.io.requester.BasicRequester;
import org.tfelab.io.requester.chrome.ChromeDriverAgent;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 乙方项目详情
 */
public class CaseTask extends Task {

	public CaseTask(String url) throws MalformedURLException, URISyntaxException {
		super(url);
	}

	public List<Task> postProc(WebDriver driver) throws Exception {
		String src = getResponse().getText();

		Case ca = new Case();

		if (!getUrl().contains("http://shop.tianpeng.com")) {
			ca.id = org.tfelab.txt.StringUtil.byteArrayToHex(org.tfelab.txt.StringUtil.uuid(getUrl()));
			ca.user_id = driver.findElement(By.cssSelector("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-desc.J-shop-desc"))
					.getAttribute("data-userid");
			ca.title = driver.findElement(By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > h2"))
					.getText();
		/*ca.ongoing = driver.findElement(By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-buy"))
				.findElement();*/
			ca.url = getUrl();
			ca.cycle = driver.findElement(By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-other-number.clearfix > div.service-complate-time > strong"))
					.getText();

			if (driver.findElement(By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-price-warp.yahei.clearfix.qrcode-version > div.price-with-qrcode"))
					.findElements(By.className("price-panel ")).size() == 1) {
				if (driver.findElement(By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-price-warp.yahei.clearfix.qrcode-version > div.price-with-qrcode.no-app-price > dl:nth-child(1) > dd > span.price"))
						.getText().contains("-")) {
					String[] budget = driver.findElement(By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-price-warp.yahei.clearfix.qrcode-version > div.price-with-qrcode.no-app-price > dl:nth-child(1) > dd > span.price"))
							.getText().split("-");
					ca.budget_lb = Double.parseDouble(budget[0]);
					ca.budget_up = Double.parseDouble(budget[1]);
				} else {
					ca.budget_lb = Double.parseDouble(driver.findElement(By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-price-warp.yahei.clearfix.qrcode-version > div.price-with-qrcode.no-app-price > dl:nth-child(1) > dd > span.price"))
							.getText());
					ca.budget_up = ca.budget_lb;
				}

			} else {
				if (driver.findElement(
						By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-price-warp.yahei.clearfix.qrcode-version > div.price-with-qrcode > dl.price-panel.app-price-panel.hot-price > dd > span.price"))
						.getText().contains("-")) {

					String[] budget = driver.findElement(
							By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-price-warp.yahei.clearfix.qrcode-version > div.price-with-qrcode > dl.price-panel.app-price-panel.hot-price > dd > span.price"))
							.getText().split("-");
					ca.budget_lb = Double.parseDouble(budget[0]);
					ca.budget_up = Double.parseDouble(budget[1]);
				} else {
					ca.budget_lb = Double.parseDouble(driver.findElement(By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-price-warp.yahei.clearfix.qrcode-version"))
							.findElement(By.className("price-with-qrcode")).findElement(By.className("price"))
							.getText());
					ca.budget_up = ca.budget_lb;
				}

			}

			ca.response_time = driver.findElement(By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-other-number.clearfix > div.service-respond-time > div > strong"))
					.getText();

			ca.service_attitude = Double.parseDouble(driver.findElement(By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-star-warp.clearfix > ul > li.first > strong"))
					.getText());

			ca.work_speed = Double.parseDouble(driver.findElement(By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-star-warp.clearfix > ul > li:nth-child(2) > strong"))
					.getText());

			ca.complete_quality = Double.parseDouble(driver.findElement(By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-star-warp.clearfix > ul > li:nth-child(3) > strong"))
					.getText());


			ca.rating = Float.parseFloat(driver.findElement(By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-star-warp.clearfix > div.service-star-box > div.service-star-score"))
					.getText());
			ca.rate_num = Integer.parseInt(driver.findElement(By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-star-warp.clearfix > div.service-star-box > div.service-comment-count > em"))
					.getText());


			String caseTask_des = driver.findElement(By.cssSelector("#j-service-tab > div.service-tab-content.ui-switchable-content > div.service-tab-item.service-detail.ui-switchable-panel > ul.service-property"))
					.getText();

			Pattern pattern1 = Pattern.compile(".*行业.*：(?<T>.+?)\\s+");
			Matcher matcher1 = pattern1.matcher(caseTask_des);

			ca.type = caseTask_des;

			if (matcher1.find()) {
				ca.tags = matcher1.group("T");
			}

		}
		else {

			ca.id = org.tfelab.txt.StringUtil.byteArrayToHex(org.tfelab.txt.StringUtil.uuid(getUrl()));
			ca.user_id = getUrl().split("/")[3];
			ca.title = driver.findElement(By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > h2"))
					.getText();
		/*ca.ongoing = driver.findElement(By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-buy"))
				.findElement();*/
			ca.url = getUrl();

			ca.budget_lb = Double.parseDouble(driver.findElement(By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-price-warp.yahei.clearfix.qrcode-version > div > dl:nth-child(1) > dd > span"))
					.getText());

			ca.budget_up = ca.budget_lb;

			String caseTask_des = driver.findElement(By.cssSelector("#j-service-tab > div.service-tab-content.ui-switchable-content > div.service-tab-item.service-detail.ui-switchable-panel > ul.service-property"))
					.getText();

			Pattern pattern1 = Pattern.compile(".*行业.*：(?<T>.+?)\\s+");
			Matcher matcher1 = pattern1.matcher(caseTask_des);

			ca.type = caseTask_des;

			if (matcher1.find()) {
				ca.tags = matcher1.group("T");
			}

		}


		String description_src = driver.findElement(By.cssSelector("#J-description")).getAttribute("innerHTML");

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
				binary.url = ca.url;
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
				binary.url = ca.url;
				des_src = des_src.replace(a, binary.file_name);
				binary.insert();

			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}

		ca.description = des_src;

		ca.insert();

		return new ArrayList<Task>();
	}

	public static void main(String[] args) throws Exception {
		Refacter.dropTable(Case.class);
		Refacter.createTable(Case.class);
	}
}
