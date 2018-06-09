package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.sdyk.ai.crawler.model.ServiceProvider;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.CaseScanTask;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.WorkScanTask;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ServiceProviderTask extends Task {

	// 实例化
	ServiceProvider serviceProvider;

	public ServiceProviderTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);
		this.setPriority(Priority.MEDIUM);

		this.addDoneCallback(() -> {

			try {

				String src = getResponse().getText();
				Document doc = getResponse().getDoc();

				List<com.sdyk.ai.crawler.task.Task> tasks = new ArrayList<>();

				serviceProvider = new ServiceProvider(getUrl());

				shareData(doc, src);

				// 判断是哪个页面格式
				if (src.contains("as.zbjimg.com/static/nodejs-tianpeng-utopiacs-web/widget/tp-header/img/tianpeng-logo_31addeb.png")) {
					// 天棚网服务商信息
					try {
						pageOne(src, serviceProvider, doc);
					} catch (Exception e) {
						logger.error(e);
					}
				} else {
					// 猪八戒服务商信息
					try {
						pageTwo(src, serviceProvider, doc);
					} catch (Exception e) {
						logger.error(e);
					}
				}

				try {
					serviceProvider.insert();
				} catch (Exception e) {
					logger.error("insert/update error {}", e);
				}
				// 服务商评价地址：http://shop.zbj.com/evaluation/evallist-uid-13046360-type-1-page-5.html
				tasks.add(ServiceProviderRatingTask.generateTask(serviceProvider.origin_id, 1));
				tasks.add(CaseScanTask.generateTask(serviceProvider.origin_id, 1));
				tasks.add(WorkScanTask.generateTask(getUrl(), 1));

				for (com.sdyk.ai.crawler.task.Task t : tasks) {
					ChromeDriverRequester.getInstance().submit(t);
				}
			} catch(Exception e) {
				logger.error(e);
			}
		});

	}

	/**3
	 *
	 * @param doc
	 * @param src
	 */
	public void shareData(Document doc, String src) {

		//https://shop.zbj.com/15774587/
		serviceProvider.origin_id = this.getUrl().split("/")[3];

		// 获取等级
		if (src.contains("<img align=\"absmiddle\" src=\"https://t5.zbjimg.com/t5s/common/img/user-level/level-")) {

			String s = doc.select("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-desc.J-shop-desc > img")
					.get(0).attr("src");

			serviceProvider.grade = s.split("/level-")[1].split(".png")[0];
		}

		// 获取联系方式
		contactWay(src, serviceProvider, doc);

		// 获取店铺流量
		if (src.contains("店铺流量")) {

			Element el = doc.selectFirst(".my-home");
			if(el != null) {
				String text = el.text()
						.replaceAll("(?s)^.+?收藏量：", "")
						.replaceAll(" *人", "");

				if (!(text.equals("") || text == null)) {
					serviceProvider.fav_num = Integer
							.parseInt(text);
				}
			}

			// body > div.diy-content.preview.J-refuse-external-link > div > div.diy-sec.diy.w990 > div.case2-left > div:nth-child(5)
			/*Elements we = doc.select("body > div.diy-content.preview.J-refuse-external-link > div > div.diy-sec.diy.w990 > div.case2-left > div");

			for (Element web : we) {
				if (web.text().contains("店铺流量") && web.text().contains("收藏量")) {
					serviceProvider.collection_num = Integer
							.parseInt(web.select("orange").get(2).text());
				}
			}*/
		}
	}

	/**
	 * 天蓬网服务商详情
	 * @param src
	 * @param serviceProvider
	 */
	public void pageOne(String src, ServiceProvider serviceProvider, Document doc) {

		serviceProvider.name = doc.select("body > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-desc > a > strong").text();
		if (src.contains("专业店")) {
			serviceProvider.type = "专业店";
		}else if (src.contains("旗舰店")){
			serviceProvider.type = "旗舰店";
		}

		Elements webElements = doc.select("body > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-evaluate.clearfix > div.shop-evaluate-det > span");
		if (webElements.get(0).text() != null && !webElements.get(0).text().equals("")) {
			serviceProvider.service_quality = Double.parseDouble(webElements.get(0).text());
		}
		if (webElements.get(2).text() != null && !webElements.get(0).text().equals("")) {
			serviceProvider.service_speed = Double.parseDouble(webElements.get(2).text());
		}
		if (webElements.get(4).text() != null && !webElements.get(0).text().equals("")) {
			serviceProvider.service_attitude = Double.parseDouble(webElements.get(4).text());
		}
		try {
			serviceProvider.praise_num = Integer.parseInt(doc.select("body > div.diy-content.preview.J-refuse-external-link > div > div.diy-sec.diy.w990 > div.case2-right > div:nth-child(3) > div.shop-evaluation-newstyle > div > div > div > div > div.filter-comment.J-filter-comment.lh-25 > label.icon-wrap.good.highlight > span")
					.text()
					.replace("好评(", "").replace(")", ""));
			serviceProvider.negative_num = Integer.parseInt(doc.select("body > div.diy-content.preview.J-refuse-external-link > div > div.diy-sec.diy.w990 > div.case2-right > div:nth-child(3) > div.shop-evaluation-newstyle > div > div > div > div > div.filter-comment.J-filter-comment.lh-25 > label.icon-wrap.bad > span")
					.text()
					.replace("差评(", "").replace(")", ""));
		} catch (NumberFormatException e) {}

		webPageUcenter();
	}

	/**
	 * 猪八戒服务商详情
	 * @param src
	 * @param serviceProvider
	 */
	public void pageTwo(String src, ServiceProvider serviceProvider, Document doc) throws IOException {

		serviceProvider.name = doc.select("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-desc.J-shop-desc > a > strong")
				.text();
		if (doc.select("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-desc.J-shop-desc > img")
				.size() >= 2) {
			serviceProvider.type = "企业";
		}
		else {
			serviceProvider.type = "个人";
		}
		webPageEvaluation();

		webPageUcenter();

		}

	/**
	 *服务商信息
	 */
	public void webPageUcenter() {

		Document doc = null;
		try {
			doc = Jsoup.connect("https://ucenter.zbj.com/rencai/view/" + getUrl().split("/")[3]).get();
		} catch (IOException e) {
			e.printStackTrace();
		}

		serviceProvider.location = doc.select("#utopia_widget_3 > div.shop-center-tit.clearfix > span.fr.active-address")
					.text();

		serviceProvider.tags = doc.select("#utopia_widget_5 > p.label-box-wrap")
				.text();
		try {
			serviceProvider.rating = Integer.parseInt(doc.select("#power").text().replaceAll("", "0"));
		}catch (Exception e) {}

		serviceProvider.content = doc.select("#utopia_widget_3 > div.user-about > div > span").text();

		try {
			serviceProvider.rating_num = Integer.parseInt(doc.select("#utopia_widget_4 > div > div.clearfix > span")
					.text().split("（")[1].split("）")[0]);
		}catch (Exception e) {}

		String recommendation = doc.select("#evaluationwrap > ul.evaluation-option.clearfix.J-evaluation-option > li:nth-child(6) > a")
				.text();

		try {
			serviceProvider.rcmd_num = Integer.parseInt(recommendation.split("\\(")[1].split("\\)")[0]);
		} catch (Exception e) {}

	}

	/**
	 *交易
	 */
	// TODO 天棚网可能有问题 页面具体分析 解决
	public void webPageEvaluation() throws IOException {

		Document doc = Jsoup.connect(getUrl() + "evaluation.html").get();

		Elements webElements = doc.select("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-evaluate.clearfix > div.shop-evaluate-det > span");

		try {
			serviceProvider.service_quality = Double.parseDouble(webElements.get(0).text());
		} catch (Exception e) {}
		try {
			serviceProvider.service_speed = Double.parseDouble(webElements.get(2).text());
		} catch (Exception e) {}
		try {
			serviceProvider.service_attitude = Double.parseDouble(webElements.get(4).text());
		} catch (Exception e) {}
		try {
			serviceProvider.income = Double.parseDouble(doc.select("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-balance > span:nth-child(1)")
					.text().replaceAll(",", ""));
		} catch (Exception e) {}
		try {
			serviceProvider.project_num = Integer.parseInt(doc.select("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-balance > span:nth-child(2)")
					.text());
		} catch (Exception e) {}
		try {
			serviceProvider.success_ratio = (float) (1 - Double.parseDouble(doc.select("body > div.main > div > div.wk-r > div.w-con.shop-service-wrap > div.con.clearfix > div.shop-service-bd > div.fl.shop-service-left > div:nth-child(3) > div.td.td4")
					.text().replaceAll("%", "")) * 0.01);
		} catch (Exception e) {}
		try {
			serviceProvider.praise_num = Integer.parseInt(doc.select("body > div.main > div > div.wk-r > div:nth-child(3) > div.con.clearfix > div.shop-comment-bd > div.shop-comment-l > div > div:nth-child(4) > span:nth-child(1)")
					.text());
		} catch (Exception e) {}
		try {
			serviceProvider.negative_num = Integer.parseInt(doc.select("body > div.main > div > div.wk-r > div:nth-child(3) > div.con.clearfix > div.shop-comment-bd > div.shop-comment-l > div > div:nth-child(4) > span:nth-child(3)")
					.text());
		} catch (Exception e) {}
	}

	/**
	 * 获取联系方式
	 * @param src
	 * @param serviceProvider
	 */
	public void contactWay(String src, ServiceProvider serviceProvider, Document doc) {

		if (src.contains("<div class=\"shop-fix-im-qq\">")) {
			try {

				Elements list_qq = doc.select("body > div.shop-fixed-im.sidebar-show > div.shop-fixed-im-hover.shop-customer.j-shop-fixed-im > div.shop-fix-im-qq > div.qq-item");

				String qq = "";
				for (Element webElement : list_qq) {
					qq = qq + " " + webElement.select("a").attr("data-qq");
				}
				serviceProvider.qq = qq;
			} catch (Exception e) {}
			try {
				Elements list_phone = doc.select("body > div.shop-fixed-im.sidebar-show > div.shop-fixed-im-hover.shop-customer.j-shop-fixed-im > div.shop-fix-im-qq > div.time-item");

				String phone = "";
				for (Element webElement : list_phone) {
					phone = phone + " " + webElement.select("a").attr("data-phone");
				}
				serviceProvider.cellphone = phone;
			} catch (Exception e) {}
		}
	}
}