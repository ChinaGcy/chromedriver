package com.sdyk.ai.crawler.zbj.task.scanTask;

import com.sdyk.ai.crawler.zbj.task.modelTask.ServiceSupplierTask;
import com.sdyk.ai.crawler.zbj.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import org.jsoup.nodes.Document;
import org.openqa.selenium.WebDriver;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * 服务商列表
 * 1. 找到url
 * 2. 翻页
 */
public class ServiceScanTask extends ScanTask {

	public static ServiceScanTask generateTask(String channel, int page) {

		String url = "https://www.zbj.com/" + channel + "/pk" + page + ".html";

		try {
			ServiceScanTask t = new ServiceScanTask(url, page);
			return t;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 *
	 * @param url
	 * @param page
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public ServiceScanTask(String url, int page) throws MalformedURLException, URISyntaxException {

		super(url);
		this.setPriority(Priority.HIGH);

		this.addDoneCallback(() -> {

			String src = getResponse().getText();

			Document document = getResponse().getDoc();

			List<Task> tasks = new ArrayList<>();

			Pattern pattern = Pattern.compile("//shop.zbj.com/\\d+/");
			Matcher matcher = pattern.matcher(src);

			List<String> list = new ArrayList<>();

			while (matcher.find()) {

				String new_url = "https:" + matcher.group();

				if(!list.contains(new_url)) {

					list.add(new_url);
					try {
						tasks.add(new ServiceSupplierTask(new_url));
					} catch (MalformedURLException | URISyntaxException e) {
						logger.error(e);
					}
				}
			}

			// 当前页数
			int i = (page-1)/40+1;
			// 翻页
			if (pageTurning("div.pagination > ul > li", i)) {
				Task t = ServiceScanTask.generateTask(getUrl().split("/")[3],page + 40);
				tasks.add(t);
			}

			for(Task t : tasks) {
				ChromeDriverRequester.getInstance().submit(t);
			}

			logger.info("Task num: {}", tasks.size());
		});
	}
}
