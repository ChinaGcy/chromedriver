package com.sdyk.ai.crawler.task;

import com.sdyk.ai.crawler.model.TaskTrace;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 *
 */
public abstract class ScanTask extends Task {

	public boolean backtrace = true;

	public ScanTask(String url) throws MalformedURLException, URISyntaxException {
		super(url);
		/*this.addDoneCallback(() -> {
			FileUtil.appendLineToFile(
					url + "\t" + DateFormatUtil.dff.print(System.currentTimeMillis()),
					"scantask.txt");
		});*/
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