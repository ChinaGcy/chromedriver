package com.sdyk.ai.crawler.specific.jfh.task.scanTask;

import com.sdyk.ai.crawler.model.TaskTrace;
import one.rewind.io.requester.exception.ProxyException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class ScanTask extends com.sdyk.ai.crawler.task.ScanTask {

	public ScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {
		super(url);
	}

	/**
	 * 判断是否为最大页数
	 *
	 * @param path
	 * @param page
	 * @return
	 */
	@Override
	public boolean pageTurning(String path, int page) {
		return false;
	}

	/**
	 * 获取ScanTask 标识
	 *
	 * @return
	 */
	@Override
	public TaskTrace getTaskTrace() {
		return null;
	}

	public static String domain(){
		return "jfh";
	}

}
