package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.sdyk.ai.crawler.model.Work;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sdyk.ai.crawler.specific.zbj.task.Task;


/**
 * 案例详情
 */
public class WorkTask extends Task {

	Work work;

	public WorkTask(String url, String user_id) throws MalformedURLException, URISyntaxException {
		super(url);
		this.setParam("user_id",user_id);
		this.setBuildDom();

		this.addDoneCallback(()-> {

			try {

				Document doc = getResponse().getDoc();
				List<Task> tasks = new ArrayList<>();

				work = new Work(getUrl());

				// 判断页面格式
				if (getUrl().contains("zbj")) {
					// 猪八戒
					pageOne(doc);
				} else {
					// 天棚网
					pageTwo(doc);
				}

				if (work.name == null) {
					ChromeDriverRequester.getInstance().submit(this);
				}

				try {
					work.insert();
				} catch (Exception e) {
					logger.error("insert error for Work", e);
				}

				for (Task t : tasks) {
					ChromeDriverRequester.getInstance().submit(t);
				}
			}catch (Exception e) {
				logger.error(e);
			}
		});
	}
	/**
	 * 猪八戒案例格式
	 */
	public void pageOne(Document doc) {

		work.name = doc.select("body > div.det-bg.yahei > div.det-content.clearfix > div.det-head.fl > div")
				.text();
		work.user_id = this.getParamString("user_id");

		// 获取work类型
		String src_ = getString(
				"body > div.det-bg.yahei > div.det-content.clearfix > div.det-middle.clearfix > div.det-right.fr > div.det-middle-content > ul",
				"") + " ";
		Pattern pattern = Pattern.compile(".*客户名称：(?<T>.+?)\\s+");
		Pattern pattern_type = Pattern.compile(".*类型.*： ?(?<T>.+?)\\s+");
		Pattern pattern_field = Pattern.compile(".*行业.*： ?(?<T>.+?)\\s+");
		Matcher matcher = pattern.matcher(src_);
		Matcher matcher_type = pattern_type.matcher(src_);
		Matcher matcher_field = pattern_field.matcher(src_);

		if (matcher.find()) {
			work.tenderer_name = matcher.group("T");
		}
		if (matcher_type.find()) {
			work.type = matcher_type.group("T");
		}
		if (matcher_field.find()) {
			work.field = matcher_field.group("T");
		}

		// 下载二进制文件
		String description_src = doc.select("body > div.det-bg.yahei > div.det-content.clearfix > div.det-middle.clearfix > div.det-left.fl")
				.html();

		work.description = download(description_src)
				.replace("<img src=\"https://t5.zbjimg.com/t5s/common/img/space.gif\">", "");

		// 价格
		if(doc.select("body > div.det-bg.yahei > div.det-content.clearfix > div.det-middle.clearfix > div.det-right.fr > div.det-middle-head > p.right-content")
				.text().contains("面议")) {
			work.pricee = 0.00;
		} else {
			try {
				work.pricee = Double.parseDouble(doc.select("body > div.det-bg.yahei > div.det-content.clearfix > div.det-middle.clearfix > div.det-right.fr > div.det-middle-head > p.right-content")
						.text().split("￥")[1]);
			} catch (Exception e) {}

		}
	}

	/**
	 * 天棚网案例格式
	 */
	public void pageTwo(Document doc) {

		try {

			work.user_id = this.getParamString("user_id");
			work.name = doc.select("body > div.tp-works-hd > div > div.tp-works-hd-left > div.works-title > h2")
					.text();
			work.tenderer_name = doc.select("body > div.tp-works-hd > div > div.tp-works-hd-left > div.works-info > p.works-info-customer > em")
					.text();
			work.pricee = Double.parseDouble(doc.select("body > div.tp-works-hd > div > div.tp-works-hd-left > div.works-info > p.works-info-amount > em")
					.text().replaceAll("¥", "").replaceAll(",", ""));
			work.tags = doc.select("body > div.tp-works-hd > div > div.tp-works-hd-left > ul").text();

			String description_src = doc.select("body > div.tp-works-bd > div > div.works-bd-content > div")
					.html();
			// 二进制文件下载
			work.description = download(description_src)
					.replace("<img src=\"https://t5.zbjimg.com/t5s/common/img/space.gif\">", "");

		}catch (Exception e) {
			e.printStackTrace();
		}
	}


}
