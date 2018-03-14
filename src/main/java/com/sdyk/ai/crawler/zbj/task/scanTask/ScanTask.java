package com.sdyk.ai.crawler.zbj.task.scanTask;

import com.sdyk.ai.crawler.zbj.task.Task;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

public abstract class ScanTask extends Task{

	public boolean backtrace = true;

	public ScanTask(String url) throws MalformedURLException, URISyntaxException {
		super(url);
	}

	public ScanTask(String url, String post_data) throws MalformedURLException, URISyntaxException {
		super(url, post_data);
	}

	public ScanTask(String url, String post_data, String cookies, String ref) throws MalformedURLException, URISyntaxException {
		super(url, post_data, cookies, ref);
	}

	public ScanTask(String url, HashMap<String, String> headers, String post_data, String cookies, String ref) throws MalformedURLException, URISyntaxException {
		super(url, headers, post_data, cookies, ref);
	}

	/**
	 * 判断是否为最大页数
	 * @param driver
	 * @param path
	 * @param page
	 * @return
	 */
	public boolean pageTurning(WebDriver driver, String path, int page) {

		if(!backtrace) return false;

		try {
			List<WebElement> pageList = driver.findElement(
					By.cssSelector(path))
					.findElements(By.tagName("li"));
			// 获取最大页数
			int maxPage = Integer.parseInt(pageList.get(pageList.size() - 2).getText());

			return maxPage > page;

		} catch (NoSuchElementException | ArrayIndexOutOfBoundsException e) {
			return false;
		}
	}

}
