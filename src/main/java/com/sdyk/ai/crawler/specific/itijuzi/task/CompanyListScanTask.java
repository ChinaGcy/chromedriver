package com.sdyk.ai.crawler.specific.itijuzi.task;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.proLagou.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.task.ScanTask;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.action.ClickAction;
import one.rewind.io.requester.chrome.action.SetValueAction;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import one.rewind.txt.DateFormatUtil;
import one.rewind.util.FileUtil;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompanyListScanTask extends ScanTask {

	static {
		registerBuilder(
				CompanyListScanTask.class,
				"http://radar.itjuzi.com/company{{page}}",
				ImmutableMap.of("page", String.class),
				ImmutableMap.of("page", "1")
		);
	}

	public CompanyListScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url.replaceAll("/\\d+$", ""));

		this.setPriority(Priority.HIGH);

		this.setBuildDom();

		// TODO 通过Action 选中条件
		// 选择地点
		for( int i = 1; i<4; i++ ){

			this.addAction(
					new ClickAction("body > div.company-main > div.filter-box > ul > li:nth-child(3) > ul > li:nth-child("+i+")"));

		}

		//选择融资轮次
		for(int j =1 ; j < 16 ; j++){
			this.addAction(new
					ClickAction("body > div.company-main > div.filter-box > ul > li:nth-child(4) > ul > li:nth-child("+j+")"));
		}

		this.addAction(new
				ClickAction("body > div.company-main > div.filter-box > ul > li:nth-child(4) > ul > li:nth-child(16)",
				5000
		));

		int next = 2; //Integer.valueOf((Integer) init_map.get("page"));


		//填写跳转页数
		String wPath = "#goto_page_num";
		String detail = String.valueOf(next);
		this.addAction(new SetValueAction(wPath,detail));

		//点击跳转按钮
		String cliPath = "#goto_page_btn";  //#goto_page_btn
		this.addAction(new ClickAction(cliPath,5000));

		this.addAction(new SetValueAction(
				"#goto_page_num", String.valueOf(String.valueOf(next)), 2000
		));

		this.setResponseFilter((request, contents, messageInfo) -> {

			if(messageInfo.getOriginalUrl().matches("http://radar.itjuzi.com/company/infonew\\?page=\\d+")) {
				this.getResponse().setVar("json", contents.getTextContents());
			}
		});

		this.addDoneCallback((t) -> {

			Document doc = t.getResponse().getDoc();

			proc(doc, Integer.valueOf((Integer) init_map.get("page")));

		});
	}

	public void proc(Document doc, Integer page) throws Exception {

		List<Task> task = new ArrayList<>();

		Elements company_list = doc.select("a.logo-box");

		for( int i = 1; i<company_list.size(); i++ ){

			String url = company_list.get(i).attr("href");

			task.add(new CompanyTask(url));
		}

		if( company_list.size() > 0 ){

			FileUtil.appendLineToFile(DateFormatUtil.dff.print(System.currentTimeMillis()) + "\t"+ "当前页 ：" + page  ,
					"page.txt");

			int next = page+1;

			int maxPage = 2168;

			if( next <= maxPage ){

				Map<String, Object> init_map = new HashMap<>();
				init_map.put("page", next);

				ChromeTaskHolder holder = ChromeTask.buildHolder(CompanyListScanTask.class, init_map );

				ChromeDriverDistributor.getInstance().submit(holder);

			}

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

}
