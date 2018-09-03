package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.witkey.Work;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import one.rewind.io.requester.exception.ProxyException;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 案例详情
 */
public class WorkTask extends Task {

	public static long MIN_INTERVAL = 365 * 24 * 60 * 60 * 1000L;

	static {
		registerBuilder(
				WorkTask.class,
				"https://shop.zbj.com/works/detail-wid-{{work_webId}}.html",
				ImmutableMap.of("work_webId", String.class),
				ImmutableMap.of("work_webId", ""),
				false,
				Priority.MEDIUM
		);
	}

	Work work;

	public WorkTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {
		super(url);
		this.setBuildDom();

		this.addDoneCallback((t)-> {

			String work_webId = t.getStringFromVars("work_webId");

			try {

				Document doc = getResponse().getDoc();

				work = new Work(getUrl());

				work.user_id = one.rewind.txt.StringUtil.byteArrayToHex(
						one.rewind.txt.StringUtil.uuid(
								"https://shop.zbj.com/"+ work_webId +"/"));

				// 判断页面格式
				if (getUrl().contains("zbj")) {
					// 猪八戒
					pageOne(doc);
				} else {
					// 天棚网
					pageTwo(doc);
				}

				try {
					work.insert();
				} catch (Exception e) {
					logger.error("insert error for Work", e);
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

		work.title = doc.select("body > div.det-bg.yahei > div.det-content.clearfix > div.det-head.fl > div")
				.text();

		// 获取work类型
		String src_ = getString(
				"body > div.det-bg.yahei > div.det-content.clearfix > div.det-middle.clearfix > div.det-right.fr > div.det-middle-content > ul",
				"");
		Pattern pattern = Pattern.compile(".*客户名称：(?<T>.+?)\\s+");
		Pattern pattern_type = Pattern.compile(".*类型.*?： *(?<T>.+?)\\s+");
		Pattern pattern_field = Pattern.compile("行业.*?： *(?<T>.+?)\\s+");
		Matcher matcher = pattern.matcher(src_);
		Matcher matcher_type = pattern_type.matcher(src_);
		Matcher matcher_field = pattern_field.matcher(src_);

		if (matcher.find()) {
			work.tenderer_name = matcher.group("T");
		}
		while (matcher_type.find()) {
			work.addTag(matcher_type.group("T"));
		}
		if (matcher_field.find()) {
			work.category = matcher_field.group("T");
		}

		// 下载二进制文件
		String description_src = doc.select("body > div.det-bg.yahei > div.det-content.clearfix > div.det-middle.clearfix > div.det-left.fl")
				.html();

		work.content = download(description_src)
				.replace("<img src=\"https://t5.zbjimg.com/t5s/common/img/space.gif\">", "");

		// 价格
		if(doc.select("body > div.det-bg.yahei > div.det-content.clearfix > div.det-middle.clearfix > div.det-right.fr > div.det-middle-head > p.right-content")
				.text().contains("面议")) {
			work.price = 0.00;
		} else {
			try {
				work.price = Double.parseDouble(doc.select("body > div.det-bg.yahei > div.det-content.clearfix > div.det-middle.clearfix > div.det-right.fr > div.det-middle-head > p.right-content")
						.text().split("￥")[1]);
			} catch (Exception e) {}

		}
	}

	/**
	 * 天棚网案例格式
	 */
	public void pageTwo(Document doc) {

		try {

			work.title = doc.select("body > div.tp-works-hd > div > div.tp-works-hd-left > div.works-title > h2")
					.text();
			work.tenderer_name = doc.select("body > div.tp-works-hd > div > div.tp-works-hd-left > div.works-info > p.works-info-customer > em")
					.text();
			work.price = Double.parseDouble(doc.select("body > div.tp-works-hd > div > div.tp-works-hd-left > div.works-info > p.works-info-amount > em")
					.text().replaceAll("¥", "").replaceAll(",", ""));
			work.addTag(doc.select("body > div.tp-works-hd > div > div.tp-works-hd-left > ul").text().split(" "));

			String description_src = doc.select("body > div.tp-works-bd > div > div.works-bd-content > div")
					.html();
			// 二进制文件下载
			work.content = download(description_src)
					.replace("<img src=\"https://t5.zbjimg.com/t5s/common/img/space.gif\">", "");

		}catch (Exception e) {
			e.printStackTrace();
		}
	}


}
