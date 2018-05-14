package com.sdyk.ai.crawler.zbj.task;

import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.zbj.util.BinaryDownloader;
import com.sdyk.ai.crawler.zbj.util.StatManager;
import com.sdyk.ai.crawler.zbj.util.StringUtil;
import one.rewind.db.DBName;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import org.openqa.selenium.remote.UnreachableBrowserException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;

@DBName(value = "crawler")
@DatabaseTable(tableName = "tasks")
public class Task extends one.rewind.io.requester.Task {

	public static final Logger logger = LogManager.getLogger(Task.class.getName());

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

	public String getString(String path, String... clean) {

		String found = null;
		if(getResponse().getDoc() != null) {
			found = getResponse().getDoc().select(path).text();
			for(String c : clean) {
				found = found.replaceAll(c, "");
			}
		}

		return found;
	}

	public int getInt(String path, String... clean) {
		return Integer.parseInt(getString(path, clean));
	}

	public float getFloat(String path, String... clean) {
		return Float.parseFloat(getString(path, clean));
	}

	public double getDouble(String path, String... clean) {
		return Double.parseDouble(getString(path, clean));
	}

	/**
	 * 对源代码中的附件进行下载，并替换链接
	 * @param src 包含附件链接的源代码
	 * @return 替换后的内容
	 */
	public String download(String src) {

		Set<String> img_urls = new HashSet<>();
		Set<String> a_urls = new HashSet<>();
		List<String> fileName = new ArrayList<>();

		String des_src = StringUtil.cleanContent(src, img_urls, a_urls, fileName);

		if (img_urls.size() != 0 ) {
			des_src = BinaryDownloader.download(des_src, img_urls, getUrl(), null);
		}

		if (a_urls.size() != 0) {
			des_src = BinaryDownloader.download(des_src, a_urls, getUrl(), fileName);
		}

		return des_src;
	}


	public one.rewind.io.requester.Task validate() throws ProxyException.Failed, AccountException.Failed, AccountException.Frozen {
		//throw new ProxyException.Failed();

		//throw new UnreachableBrowserException("chromedriver is bad");
		if (getResponse().getDoc().title().contains("您的访问存在异常-猪八戒网")) {
			throw new ProxyException.Failed();
		}
		return this;

	}
}
