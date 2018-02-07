package com.sdyk.ai.crawler.zbj.task;

import com.sdyk.ai.crawler.zbj.StringUtil;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.tfelab.io.requester.account.AccountWrapper;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TendererOrderTask extends Task{

	public static TendererOrderTask generateTask(String url, int page, String webId) {

		TendererOrderTask t = null;
		String url_= url+ "/?op=" + page;
		try {
			t = new TendererOrderTask(url_, page, webId);
			return t;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return t;

	}

	public TendererOrderTask(String url, int page, String webId) throws MalformedURLException, URISyntaxException {
		super(url);
		this.setParam("page", page);
		this.setParam("webId", webId);
	}

	public List<Task> postProc(WebDriver driver) throws ParseException, MalformedURLException, URISyntaxException {

		String src = getResponse().getText();
		List<Task> tasks = new ArrayList<>();

		int op_page = this.getParamInt("page");

		/*WebElement webElement = driver.findElement(By.cssSelector("#order > div > div.panel-content"));

		if (webElement.getText().contains("金额")) {
			//如果数量为5 ，翻页
			if (webElement.findElements(By.className("li")).size() == 5) {
				for (WebElement w : webElement.findElements(By.className("li"))) {
					String url = w.findElement(By.tagName("a")).getAttribute("href");
					tasks.add(new ProjectTask(url));
				}

				//翻页
				if (driver.findElement(By.cssSelector("#order > div > div.panel-content > ul")).findElements(By.tagName("li")).size() == 5) {
					Task t = generateTask("http://home.zbj.com/"
							+ this.getParamString("webId"), ++op_page, this.getParamString("webId"));
					if (t != null) {
						t.setPrior();
						tasks.add(t);
					}
				}
			}
			for (WebElement w : webElement.findElements(By.className("li"))) {
				String url = w.findElement(By.tagName("a")).getAttribute("href");
				tasks.add(new ProjectTask(url));
			}

		}*/

		Pattern pattern = Pattern.compile("<div class=\"order-item-content\"><div class=\"order-item-title\"><a href=\"(?<T>.+?)\" target=\"_blank\">");
		Matcher matcher = pattern.matcher(src.replaceAll(">\\s+<", "><"));

		List<String> list = new ArrayList<>();

		while (matcher.find()) {

			String url = matcher.group("T") + "/";

			System.out.println(url);
			if(!list.contains(url)) {
				list.add(url);
				tasks.add(new ProjectTask(url));
			}
		}
		if (list.size() == 5) {
			//翻页
			Task t = generateTask("http://home.zbj.com/"
					+ this.getParamString("webId"), ++op_page, this.getParamString("webId"));
			if (t != null) {
				t.setPrior();
				tasks.add(t);

			}
		}

		return tasks;
	}
}
