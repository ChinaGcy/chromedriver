package com.sdyk.ai.crawler.specific.proginn.task.scanTask;

import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.proginn.task.Task;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.txt.DateFormatUtil;
import one.rewind.util.FileUtil;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class ScanTask extends com.sdyk.ai.crawler.task.ScanTask {

	public static String domain(){
		return "projinn";
	}

	public ScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.addDoneCallback((t) -> {
			FileUtil.appendLineToFile(
					url + "\t" + DateFormatUtil.dff.print(System.currentTimeMillis()),
					"scantask.txt");
		});
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

}