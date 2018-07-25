package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.sdyk.ai.crawler.model.witkey.Case;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.exception.ProxyException;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 乙方服务详情
 */
public class CaseTask extends Task {

	/*static {
		// init_map_class
		init_map_class = ImmutableMap.of("user_id", String.class,"case_id", String.class);
		// init_map_defaults
		init_map_defaults = ImmutableMap.of("user_id", "0","case_id", "0");
		// url_template
		url_template = "https://shop.zbj.com/{{user_id}}/sid-{{case_id}}.html";
	}*/

	Case ca;

	public CaseTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setBuildDom();

		this.addDoneCallback((t) -> {

			try {

				String src = getResponse().getText();
				Document doc = getResponse().getDoc();

				if (!src.contains("此服务审核未通过") && !src.contains("此服务已被官方下架")) {
					ca = new Case(getUrl());

					ca.user_id = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid("https://shop.zbj.com/" + getUrl().split("/")[3] + "/"));

					if (!getUrl().contains("https://shop.tianpeng.com")) {
						// 猪八戒页面：http://shop.zbj.com/7523816/sid-696012.html
						try {
							pageOne(doc);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						// 天蓬网页面2：http://shop.tianpeng.com/17773550/sid-1126164.html
						try {
							pageTwo(doc);
						} catch (Exception e) {}

					}

					// 以下是两个页面共有的信息
					// 二进制文件下载
					String description_src = doc.select("#J-content").html();

					try {
						ca.content = download(description_src);
					} catch (Exception e) {
						e.printStackTrace();
					}

					try {
						ca.insert();
					} catch (Exception e) {
						logger.error(e);
					}
				}
			} catch (Exception e) {
				logger.error(e);
			}
		});
	}

	/**
	 * 获取猪八戒服务价格预算
	 */
	public void budgetZBJ(Document doc) {

		try {
			double[] budget = StringUtil.budget_all(doc,"body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-cost-warp.yahei.clearfix.qrcode-version > div.cost-with-qrcode > dl.cost-panel.app-cost-panel.hot-cost > dd > span.cost",
					"");
			if (budget[0] == 0.00 && budget[1] == 0.00) {
				budget = StringUtil.budget_all(doc, "body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-cost-warp.yahei.clearfix.qrcode-version > div.cost-with-qrcode.no-app-cost > dl:nth-child(1) > dd > span.cost",
						"");
				ca.budget_lb = budget[0];
				ca.budget_ub = budget[1];
			}
			else {
				ca.budget_lb = budget[0];
				ca.budget_ub = budget[1];
			}
		}
		catch (Exception e) {
			logger.error("budget error {}", e);
		}
	}

	/**
	 * 获取天蓬网服务价格预算
	 */
	public void budgetTPW(Document doc) {

		try {
			double[] budget = StringUtil.budget_all(doc, "body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-cost-warp.yahei.clearfix.qrcode-version > div > dl:nth-child(1) > dd > span.cost",
					"");
			ca.budget_lb = budget[0];
			ca.budget_ub = budget[1];
		}
		catch (Exception e) {
			ca.budget_lb = 0.00;
			ca.budget_ub = 0.00;
		}
	}

	/**
	 * 猪八戒服务页面
	 */
	public void pageOne (Document doc) {

		ca.title = getString("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > h2",
				"");

		// body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-other-number.clearfix > div.service-complate-time > strong
		/*ca.time_limit = getString("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-other-number.clearfix > div.service-complate-time > strong",
				"");*/

		// 价格预算
		budgetZBJ(doc);

		ca.response_time = getString("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-other-number.clearfix > div.service-respond-time > div > strong",
				"");

		ca.service_attitude = getDouble("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-star-warp.clearfix > ul > li.first > strong",
				"");

		ca.work_speed = getDouble("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-star-warp.clearfix > ul > li:nth-child(2) > strong",
				"");

		ca.complete_quality = getDouble("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-star-warp.clearfix > ul > li:nth-child(3) > strong",
				"");

		ca.rating = getFloat("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-star-warp.clearfix > div.service-star-box > div.service-star-score",
				"");

		ca.rate_num = getInt("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > div.service-comment-warp.J-service-comment-warp > div.service-star-warp.clearfix > div.service-star-box > div.service-comment-count > em",
				"");

		// 获取服务描述
		String caseTask_des = "";

		caseTask_des = getString("#j-service-tab > div.service-tab-content.ui-switchable-content > div.service-tab-item.service-detail.ui-switchable-panel > ul.service-property",
					"");
		ca.tags = caseTask_des;

		Pattern pattern_tags = Pattern.compile(".*行业.*：(?<T>.+?)\\s+");
		Matcher matcher_tags = pattern_tags.matcher(caseTask_des);
		if (matcher_tags.find()) {
			ca.category = matcher_tags.group("T");
		}
	}

	/**
	 * 天蓬网服务页面
	 */
	public void pageTwo(Document doc) {

		ca.title = doc.select("body > div.grid.service-main.J-service-main.J-refuse-external-link > div.service-main-r > h2").text();

		budgetTPW(doc);

		ca.tags = doc.select("#j-service-tab > div.service-tab-content.ui-switchable-content > div.service-tab-item.service-detail.ui-switchable-panel > ul.service-property")
				.text();

		Pattern pattern_tags = Pattern.compile(".*行业.*：(?<T>.+?)\\s+");
		Matcher matcher_tags = pattern_tags.matcher(ca.category);
		if (matcher_tags.find()) {
			ca.category = matcher_tags.group("T");
		}
	}
}
