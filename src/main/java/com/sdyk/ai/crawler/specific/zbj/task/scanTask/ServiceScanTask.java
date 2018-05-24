package com.sdyk.ai.crawler.specific.zbj.task.scanTask;

import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.ServiceSupplierTask;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import org.jsoup.nodes.Document;

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
			ServiceScanTask t = new ServiceScanTask(url, channel, page);
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
	public ServiceScanTask(String url, String channel, int page) throws MalformedURLException, URISyntaxException {

		super(url);
		this.setParam("page", page);
		this.setParam("channel", channel);
		this.setPriority(Priority.HIGH);
		this.setBuildDom();

		this.addDoneCallback(() -> {

			try {

				String src = getResponse().getText();

				Document document = getResponse().getDoc();

				List<Task> tasks = new ArrayList<>();

				Pattern pattern = Pattern.compile("//shop.zbj.com/\\d+/");
				Matcher matcher = pattern.matcher(src);

				List<String> list = new ArrayList<>();

				while (matcher.find()) {

					String new_url = "https:" + matcher.group();

					if (!list.contains(new_url)) {

						list.add(new_url);
						try {
							tasks.add(new ServiceSupplierTask(new_url));
						} catch (MalformedURLException | URISyntaxException e) {
							logger.error(e);
						}
					}
				}

				// 当前页数
				int i = (page - 1) / 40 + 1;

				// #utopia_widget_18 > div.pagination > ul > li:nth-child(1)
				// 翻页
				if (pageTurning("#utopia_widget_18 > div.pagination > ul > li", i)) {
					Task t = ServiceScanTask.generateTask(getUrl().split("/")[3], page + 40);
					tasks.add(t);
				}

				for (Task t : tasks) {
					ChromeDriverRequester.getInstance().submit(t);
				}

				logger.info("Task driverCount: {}", tasks.size());

			} catch (Exception e) {
				logger.error(e);
			}
		});
	}
	@Override
	public TaskTrace getTaskTrace() {
		return new TaskTrace(this.getClass(), this.getParamString("channel"), this.getParamString("page"));
	}
}
