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

		this.setBuildDom();
	}

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