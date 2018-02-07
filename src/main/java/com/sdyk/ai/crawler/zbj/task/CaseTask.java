package com.sdyk.ai.crawler.zbj.task;

import com.sdyk.ai.crawler.zbj.StringUtil;
import com.sdyk.ai.crawler.zbj.model.Case;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.tfelab.db.Refacter;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
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

		ca.budget = Double.parseDouble(driver.findElement(By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-price-warp.yahei.clearfix.qrcode-version > div.price-with-qrcode > dl.price-panel.app-price-panel.hot-price > dd > span.price"))
				.getText());
		ca.rating = Float.parseFloat(driver.findElement(By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-star-warp.clearfix > div.service-star-box > div.service-star-score"))
				.getText());
		ca.rate_num = Integer.parseInt(driver.findElement(By.cssSelector("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-star-warp.clearfix > div.service-star-box > div.service-comment-count > em"))
				.getText());

		if (driver.findElement(By.cssSelector("#j-service-tab > div.service-tab-content.ui-switchable-content > div.service-tab-item.service-detail.ui-switchable-panel"))
				.getText().matches("<ul class=\"service-property\">")) {
			String src_ = StringUtil.cleanContent(driver.findElement(By.cssSelector("#j-service-tab > div.service-tab-content.ui-switchable-content > div.service-tab-item.service-detail.ui-switchable-panel > ul.service-property"))
					.getText(),null);
			Pattern pattern = Pattern.compile(">.*类型.*：(?<T>.+?)<");
			Matcher matcher = pattern.matcher(src_);
			if (matcher.matches()) {
				ca.type = matcher.group("T");
			}

		}

		return null;
	}

	public static void main(String[] args) throws Exception {
		Refacter.dropTable(Case.class);
		Refacter.createTable(Case.class);
	}
}
