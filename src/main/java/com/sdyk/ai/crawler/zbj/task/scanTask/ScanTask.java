package com.sdyk.ai.crawler.zbj.task.scanTask;

import com.sdyk.ai.crawler.zbj.task.Task;
import one.rewind.txt.DateFormatUtil;
import one.rewind.util.FileUtil;
import org.jsoup.select.Elements;
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
		this.addDoneCallback(() -> {
			FileUtil.appendLineToFile(
					url + "\t" + DateFormatUtil.dff.print(System.currentTimeMillis()),
					"scantask.txt");
		});
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
	 * @param path
	 * @param page
	 * @return
	 */
	public boolean pageTurning(String path, int page) {

		if(!backtrace) return false;

		try {

			// div.pagination > ul > li
			Elements pageList = getResponse().getDoc().select(path);

			// 获取最大页数
			int maxPage = Integer.parseInt(pageList.get(pageList.size() - 2).text());

			return page < maxPage;

		} catch (NoSuchElementException | ArrayIndexOutOfBoundsException e) {
			logger.error(e);
			return false;
		}
	}

}
