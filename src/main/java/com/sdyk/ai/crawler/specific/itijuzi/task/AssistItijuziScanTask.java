package com.sdyk.ai.crawler.specific.itijuzi.task;

import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.company.util.MouseSuspensionAction;
import com.sdyk.ai.crawler.specific.company.util.MyClickAction;
import com.sdyk.ai.crawler.specific.company.util.WriteAction;
import com.sdyk.ai.crawler.task.ScanTask;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.chrome.action.ClickAction;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.util.FileUtil;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class AssistItijuziScanTask extends ScanTask {

	public AssistItijuziScanTask(String url, int page, String flag, int maxPage) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setPriority(Priority.HIGH);

		this.addDoneCallback(() -> {

			Document doc = getResponse().getDoc();

			crawler(doc, page, flag, maxPage);

		});
	}

	public void crawler(Document doc, int page, String flag, int maxPage){

		List<Task> task = new ArrayList<>();

		Elements comList = doc.select("a.logo-box");
		//Elements infoList = doc.select("div.company-list-info > li");

		for( int i = 1; i< comList.size(); i++ ){

			String url = comList.get(i).attr("href");
			//String local = infoList.get(i).select("div:nth-child(6)").text();
			try {
				task.add(new AssistItijuziTask(url, null));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}

		}

		if( comList.size() > 0 ){

			FileUtil.appendLineToFile("天使论page ：" + page, "page.txt");

			int next = page+1;

			if( next <= maxPage ){

				AssistItijuziScanTask assistItijuziTask = null;
				try {
					assistItijuziTask = new AssistItijuziScanTask(getUrl(), next, flag, maxPage);
				} catch (MalformedURLException | URISyntaxException e) {
					e.printStackTrace();
				}

				//选择地点
				assistItijuziTask.addAction(new MouseSuspensionAction("body > div.company-main > div.filter-box > ul > li:nth-child(3)"));
				for( int i = 1; i<4; i++ ){

					assistItijuziTask.addAction(
							new ClickAction("body > div.company-main > div.filter-box > ul > li:nth-child(3) > ul > li:nth-child("+i+")"));

				}

				//选择融资轮次
				assistItijuziTask.addAction(new MouseSuspensionAction("body > div.company-main > div.filter-box > ul > li:nth-child(4) > span"));

				assistItijuziTask.addAction(new
						MyClickAction("body > div.company-main > div.filter-box > ul > li:nth-child(4) > ul > li:nth-child("+flag+")"));

				if( next > 1 ){
					//填写跳转页数
					String wPath = "#goto_page_num";
					String detail = String.valueOf(next);
					assistItijuziTask.addAction(new WriteAction(wPath,detail));

					//点击跳转按钮
					String cliPath = "#goto_page_btn";  //#goto_page_btn
					assistItijuziTask.addAction(new MyClickAction(cliPath));
				}

				task.add(assistItijuziTask);

			}

		}

		for( Task t : task ){
			ChromeDriverRequester.getInstance().submit(t);
		}

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

	@Override
	public one.rewind.io.requester.Task validate() throws ProxyException.Failed, AccountException.Failed, AccountException.Frozen {
		return null;
	}
}
