package com.sdyk.ai.crawler.zbj.task;

import com.sdyk.ai.crawler.zbj.model.Work;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.tfelab.db.Refacter;
import org.tfelab.txt.StringUtil;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 案例详情
 */
public class WorkTask extends Task {

	public WorkTask(String url, String user_id) throws MalformedURLException, URISyntaxException {
		super(url);
		this.setParam("user_id",user_id);
	}

	public List<Task> postProc(WebDriver driver) throws Exception {

		String src = getResponse().getText();
		Work work = new Work();
		work.id = StringUtil.byteArrayToHex(StringUtil.uuid(getUrl()));
		work.name = driver.findElement(By.cssSelector("body > div.det-bg.yahei > div.det-content.clearfix > div.det-head.fl > div"))
				.getText();
		work.user_id = this.getParamString("user_id");

		String src_ = com.sdyk.ai.crawler.zbj.StringUtil.cleanContent(driver.findElement(By.cssSelector("body > div.det-bg.yahei > div.det-content.clearfix > div.det-middle.clearfix > div.det-right.fr > div.det-middle-content")).getText(), null);
		Pattern pattern = Pattern.compile("<li><strong>客户名称：</strong>(?<T>.+?)</li>");
		Pattern pattern1 = Pattern.compile("<li><strong>.*类型.*：</strong>(?<T>.+?)</li>");
		Pattern pattern2 = Pattern.compile("<li><strong>.*行业.*：</strong>(?<T>.+?)</li>");
		Matcher matcher = pattern.matcher(src_);
		Matcher matcher1 = pattern1.matcher(src_);
		Matcher matcher2 = pattern2.matcher(src_);

		if (matcher.matches()) {
			work.tenderer_name = matcher.group("T");
		}
		if (matcher1.matches()) {
			work.type = matcher.group("T");
		}
		if (matcher2.matches()) {
			work.field = matcher.group("T");
		}


		work.description = driver.findElement(By.cssSelector("body > div.det-bg.yahei > div.det-content.clearfix > div.det-middle.clearfix > div.det-left.fl > h2 > pre"))
				.getText();
		work.pricee = Double.parseDouble(driver.findElement(By.cssSelector("body > div.det-bg.yahei > div.det-content.clearfix > div.det-middle.clearfix > div.det-right.fr > div.det-middle-head > p.right-content"))
				.getText().split("￥")[1]);

		try {

			work.insert();
		} catch (Exception e) {
			e.printStackTrace();
		}


		return null;
	}


	public static void main(String[] args) throws Exception {
		Refacter.dropTable(Work.class);
		Refacter.createTable(Work.class);

	}
}
