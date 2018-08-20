package com.sdyk.ai.crawler.specific.company.linkedin;/*
package com.sdyk.ai.crawler.specific.company.linkedinTask;

import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.company.util.MoveTask;
import com.sdyk.ai.crawler.task.ScanTask;
import one.rewind.io.requester.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class LinkedInScanTask extends ScanTask {

	public static LinkedInScanTask generateTask(String companyName, String location, int page){

		//生成LIST页
		StringBuffer url = new StringBuffer("https://www.linkedin.com/jobs/search/?keywords=");
		url.append(companyName);
		url.append("&location=");
		url.append(location);
		url.append("&locationId=OTHERS.worldwide");
		url.append("&start=");
		url.append(page);

		//创建任务
		try {
			LinkedInScanTask t = new LinkedInScanTask(url.toString(), page, companyName, location);
			return t;
		} catch (MalformedURLException | URISyntaxException e) {
			logger.error("error for new LinkedInScanTask", e);
		}

		return null;
	}



	public LinkedInScanTask(String url, int page, String companyName, String location) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setPriority(Priority.HIGH);

		this.addDoneCallback(() -> {

			int randomNumber = (int) Math.round(Math.random()*10 + 5);

			try {
				Thread.sleep(randomNumber * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Document doc = getResponse().getDoc();
			List<com.sdyk.ai.crawler.task.Task> task = new ArrayList<>();

			Elements elements = doc.select("ul.jobs-search-results__list > li");

			for( Element element : elements ){

				String urlId = element.select("div").attr("data-job-id");

				try {
					task.add(new LinkedInTask("https://www.linkedin.com/jobs/view/" + urlId, companyName));
				} catch (MalformedURLException e) {
					logger.error("error for task.add", e);
				} catch (URISyntaxException e) {
					logger.error("error for task.add", e);
				}

			}

			if( elements.size() > 0 ){

				String movePath1 = "ul.jobs-search-results__list > li:nth-child(3)";
				String movePath2 = "ul.jobs-search-results__list > li:nth-child(7)";
				String movePath3 = "ul.jobs-search-results__list > li:nth-child(11)";
				String movePath4 = "ul.jobs-search-results__list > li:nth-child(15)";
				String movePath5 = "ul.jobs-search-results__list > li:nth-child(20)";
				String movePath6 = "ul.jobs-search-results__list > li:nth-child(23)";
				String movePath7 = "ul.jobs-search-results__list > li:nth-child(25)";

				int nextPage = page + elements.size();
				com.sdyk.ai.crawler.task.Task s = generateTask(companyName, location, nextPage);

				s.addAction(new MoveTask(movePath1, "jobs-search-results--is-two-pane"));
				s.addAction(new MoveTask(movePath2, "jobs-search-results--is-two-pane"));
				s.addAction(new MoveTask(movePath3, "jobs-search-results--is-two-pane"));
				s.addAction(new MoveTask(movePath4, "jobs-search-results--is-two-pane"));
				s.addAction(new MoveTask(movePath5, "jobs-search-results--is-two-pane"));
				s.addAction(new MoveTask(movePath6, "jobs-search-results--is-two-pane"));
				s.addAction(new MoveTask(movePath7, "jobs-search-results--is-two-pane"));

				task.add(s);
			}

			for(com.sdyk.ai.crawler.task.Task t : task){
				System.out.println(t.getUrl());
				ChromeDriverRequester.getInstance().submit(t);
			}

		});
	}

	*/
/**
	 * 判断是否为最大页数
	 *
	 * @param path
	 * @param page
	 * @return
	 *//*

	@Override
	public boolean pageTurning(String path, int page) {
		return false;
	}

	*/
/**
	 * 获取ScanTask 标识
	 *
	 * @return
	 *//*

	@Override
	public TaskTrace getTaskTrace() {
		return null;
	}

	@Override
	public Task validate() throws ProxyException.Failed, AccountException.Failed, AccountException.Frozen {
		return null;
	}
}
*/
