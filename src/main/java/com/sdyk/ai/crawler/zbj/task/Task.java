package com.sdyk.ai.crawler.zbj.task;

import com.sdyk.ai.crawler.zbj.util.BinaryDownloader;
import com.sdyk.ai.crawler.zbj.util.StringUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;

public abstract class Task extends org.tfelab.io.requester.Task implements Comparable<Task> {

	public static final Logger logger = LogManager.getLogger(Task.class.getName());

	// 优先级
	public Priority priority = Priority.middle;

	public enum Priority {
		low,
		middle,
		high
	}

	public Task(String url) throws MalformedURLException, URISyntaxException {
		super(url);
	}

	public Task(String url, String post_data) throws MalformedURLException, URISyntaxException {
		super(url, post_data);
	}

	public Task(String url, String post_data, String cookies, String ref) throws MalformedURLException, URISyntaxException {
		super(url, post_data, cookies, ref);
	}

	public Task(String url, HashMap<String, String> headers, String post_data, String cookies, String ref) throws MalformedURLException, URISyntaxException {
		super(url, headers, post_data, cookies, ref);
	}

	public List<? extends Task> postProc(WebDriver driver) throws Exception {
		return null;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public Priority getPriority() {
		return priority;
	}

	public static String getString(WebDriver driver, String path, String... clean) {
		String txt = driver.findElement(By.cssSelector(path)).getText();
		for(String c : clean) {
			txt = txt.replaceAll(c, "");
		}
		return txt;
	}

	public static int getInt(WebDriver driver, String path, String... clean) {
		return Integer.parseInt(getString(driver, path, clean));
	}

	public static float getFloat(WebDriver driver, String path, String... clean) {
		return Float.parseFloat(getString(driver, path, clean));
	}

	public static double getDouble(WebDriver driver, String path, String... clean) {
		return Double.parseDouble(getString(driver, path, clean));
	}

	/**
	 *
	 * @param description_src
	 */
	public String download(String description_src) {

		Set<String> img_urls = new HashSet<>();
		Set<String> a_urls = new HashSet<>();
		List<String> fileName = new ArrayList<>();
		String des_src = StringUtil.cleanContent(description_src, img_urls, a_urls, fileName);
		if (img_urls.size() != 0 ) {
			des_src = BinaryDownloader.download(des_src, img_urls, getUrl(), null);
		}
		if (a_urls.size() != 0) {
			des_src = BinaryDownloader.download(des_src, a_urls, getUrl(),fileName);
		}
		return des_src;
	}

	/**
	 * 优先级比较
	 * @param another
	 * @return
	 */
	public int compareTo(Task another) {

		final Priority me = this.getPriority();
		final Priority it = another.getPriority();
		return me.ordinal() == it.ordinal() ? 0 : it.ordinal() - me.ordinal();
	}
}
