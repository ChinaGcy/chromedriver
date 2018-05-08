package com.sdyk.ai.crawler.zbj.task.modelTask;

import com.sdyk.ai.crawler.zbj.task.Task;
import com.sdyk.ai.crawler.zbj.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.zbj.task.scanTask.ScanTask;
import com.sdyk.ai.crawler.zbj.util.StatManager;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TendererOrderTask extends ScanTask {

	/**
	 * 翻页
	 * @param url
	 * @param page
	 * @param webId
	 * @return
	 */
	public static TendererOrderTask generateTask(String url, int page, String webId) {

		TendererOrderTask t = null;
		String url_ = url+ "/?op=" + page;
		try {
			t = new TendererOrderTask(url_, page, webId);
			return t;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return t;
	}

	public TendererOrderTask(String url, int page, String webId) throws MalformedURLException, URISyntaxException {
		super(url);
		this.setParam("page", page);
		this.setParam("webId", webId);
		this.setBuildDom();

		this.addDoneCallback(() -> {
			String src = getResponse().getText();
			List<Task> tasks = new ArrayList<>();
			Document doc = getResponse().getDoc();

			int op_page = this.getParamInt("page");

			Pattern pattern = Pattern.compile("<div class=\"order-item-content\"><div class=\"order-item-title\"><a href=\"(?<T>.+?)\" target=\"_blank\">");
			Matcher matcher = pattern.matcher(src.replaceAll(">\\s+<", "><"));

			while (matcher.find()) {

				String new_url = matcher.group("T") + "/";
				// 去除重复Task
				if(!ProjectScanTask.tasks.contains(new_url)) {
					try {
						tasks.add(new ProjectTask(new_url));

					} catch (MalformedURLException | URISyntaxException e) {
						e.printStackTrace();
					}
				}
			}
			if (pageTurning("#order > div > div.pagination-wrapper > div > ul", op_page)) {
				// 翻页
				Task t = generateTask("https://home.zbj.com/"
						+ this.getParamString("webId"), ++op_page, this.getParamString("webId"));
				if (t != null) {
					t.setPriority(Priority.MEDIUM);
					tasks.add(t);

				}
			}

			for(Task t : tasks) {
				ChromeDriverRequester.getInstance().submit(t);
			}
		});
	}
}
