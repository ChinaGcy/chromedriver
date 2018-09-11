package com.sdyk.ai.crawler.task;

import com.sdyk.ai.crawler.util.BinaryDownloader;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;

public abstract class Task extends ChromeTask {

	public static final Logger logger = LogManager.getLogger(com.sdyk.ai.crawler.specific.zbj.task.Task.class.getName());

	public Task(String url) throws MalformedURLException, URISyntaxException {
		super(url);
		setBuildDom();
		this.setNoFetchImages();
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
