package com.sdyk.ai.crawler.task;

import com.sdyk.ai.crawler.model.TaskTrace;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 *
 */
public abstract class ScanTask extends Task {

	public boolean backtrace = true;

	public ScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {
		super(url);
	}

	/*public ScanTask(String url, String post_data) throws MalformedURLException, URISyntaxException {
		super(url, post_data);
	}

	public ScanTask(String url, String post_data, String cookies, String ref) throws MalformedURLException, URISyntaxException {
		super(url, post_data, cookies, ref);
	}

	public ScanTask(String url, HashMap<String, String> headers, String post_data, String cookies, String ref) throws MalformedURLException, URISyntaxException {
		super(url, headers, post_data, cookies, ref);
	}*/

	/**
	 * 判断是否为最大页数
	 * @param path
	 * @param page
	 * @return
	 */
	public abstract boolean pageTurning(String path, int page);

	/**
	 * 获取ScanTask 标识
	 * @return
	 */
	public abstract TaskTrace getTaskTrace();

}