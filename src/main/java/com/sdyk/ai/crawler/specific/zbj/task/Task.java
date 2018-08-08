package com.sdyk.ai.crawler.specific.zbj.task;

import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.db.DBName;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;

@DBName(value = "proc")
@DatabaseTable(tableName = "tasks")
public class Task extends ChromeTask {

	public static final Logger logger = LogManager.getLogger(Task.class.getName());

	public Task(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {
		super(url);
		this.setNoFetchImages();
	}

	/*public Task(String url) throws MalformedURLException, URISyntaxException {
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
	}*/

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
		try {
			return Integer.parseInt(getString(path, clean));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public float getFloat(String path, String... clean) {
		try {
			return Float.parseFloat(getString(path, clean));
		} catch (NumberFormatException e) {
			return 0.0f;
		}
	}

	public double getDouble(String path, String... clean) {
		try {
			return Double.parseDouble(getString(path, clean));
		} catch (NumberFormatException e) {
			return 0.0d;
		}
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

		String des_src = StringUtil.cleanContent(src, img_urls);

		if (img_urls.size() != 0 ) {
			//des_src = BinaryDownloader.download(des_src, img_urls, getUrl(), null);
		}

		if (a_urls.size() != 0) {
			//des_src = BinaryDownloader.download(des_src, a_urls, getUrl(), fileName);
		}

		return des_src;
	}
}
