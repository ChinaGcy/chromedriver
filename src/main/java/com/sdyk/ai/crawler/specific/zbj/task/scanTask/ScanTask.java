package com.sdyk.ai.crawler.specific.zbj.task.scanTask;

import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.txt.DateFormatUtil;
import one.rewind.util.FileUtil;
import org.jsoup.select.Elements;
import org.openqa.selenium.NoSuchElementException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;

public abstract class ScanTask extends com.sdyk.ai.crawler.task.ScanTask{

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
			if (pageList == null || pageList.size() < 3) {
				return false;
			}else {
				// TODO 获取最大页数 此处不能正常解析
				int maxPage = Integer.parseInt(pageList.get(pageList.size() - 2).text());
				return page < maxPage;
			}
		}
		catch (NullPointerException | ArrayIndexOutOfBoundsException e) {
			logger.error("{}", getUrl(), e);
			return false;
		}
	}

	public abstract TaskTrace getTaskTrace();

}
