package com.sdyk.ai.crawler.zbj.task.modelTask;

import com.sdyk.ai.crawler.zbj.model.ServiceSupplier;
import com.sdyk.ai.crawler.zbj.task.Task;
import com.sdyk.ai.crawler.zbj.task.scanTask.CaseScanTask;
import com.sdyk.ai.crawler.zbj.task.scanTask.WorkScanTask;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ServiceSupplierTask extends Task {

	// 实例化
	ServiceSupplier serviceSupplier;

	public ServiceSupplierTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);
		this.setPriority(Priority.MEDIUM);

		this.addDoneCallback(() -> {
			String src = getResponse().getText();
			Document doc = getResponse().getDoc();

			List<Task> tasks = new ArrayList<Task>();

			serviceSupplier = new ServiceSupplier(getUrl());

			shareData(doc, src);

			// 判断是哪个页面格式
			if (src.contains("as.zbjimg.com/static/nodejs-tianpeng-utopiacs-web/widget/tp-header/img/tianpeng-logo_31addeb.png")) {
				// 天棚网服务商信息
				try {
					pageOne(src, serviceSupplier, doc);
				} catch (IOException e) {
					logger.error(e);
				}
			} else {
				// 猪八戒服务商信息
				try {
					pageTwo(src, serviceSupplier, doc);
				} catch (IOException e) {
					logger.error(e);
				}
			}

			try {
				serviceSupplier.insert();
			} catch (Exception e) {
				logger.error("insert/update error {}" , e);
			}
			// 服务商评价地址：http://shop.zbj.com/evaluation/evallist-uid-13046360-type-1-page-5.html
			tasks.add(ServiceRatingTask.generateTask("https://shop.zbj.com/evaluation/evallist-uid-"+ serviceSupplier.website_id +"-type-1-page-",1));
			tasks.add(CaseScanTask.generateTask(getUrl(),1));
			tasks.add(WorkScanTask.generateTask(getUrl(), 1));

			for(Task t : tasks) {
				ChromeDriverRequester.getInstance().submit(t);
			}
		});

	}

	/**
	 *
	 * @param doc
	 * @param src
	 */
	public void shareData(Document doc, String src) {

		serviceSupplier.website_id = doc.select("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-desc.J-shop-desc").attr("data-userid");


		// 获取等级
		if (src.contains("<img align=\"absmiddle\" src=\"https://t5.zbjimg.com/t5s/common/img/user-level/level-")) {

			String s = doc.select("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-desc.J-shop-desc > img")
					.get(0).attr("src");

			serviceSupplier.grade = s.split("/level-")[1].split(".png")[0];
		}

		// 获取联系方式
		contactWay(src, serviceSupplier, doc);

		// 获取店铺流量
		if (src.contains("店铺流量")) {

			Element el = doc.selectFirst(".my-home");
			if(el != null) {
				String text = el.text()
						.replaceAll("(?s)^.+?收藏量：", "")
						.replaceAll(" *人", "");

				serviceSupplier.collection_num = Integer
						.parseInt(text);

			}

			// body > div.diy-content.preview.J-refuse-external-link > div > div.diy-sec.diy.w990 > div.case2-left > div:nth-child(5)
			/*Elements we = doc.select("body > div.diy-content.preview.J-refuse-external-link > div > div.diy-sec.diy.w990 > div.case2-left > div");

			for (Element web : we) {
				if (web.text().contains("店铺流量") && web.text().contains("收藏量")) {
					serviceSupplier.collection_num = Integer
							.parseInt(web.select("orange").get(2).text());
				}
			}*/
		}
	}

	/**
	 * 天蓬网服务商详情
	 * @param src
	 * @param serviceSupplier
	 */
	public void pageOne(String src, ServiceSupplier serviceSupplier, Document doc) throws IOException {

		serviceSupplier.name = doc.select("body > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-desc > a > strong").text();
		if (src.contains("专业店")) {
			serviceSupplier.type = "专业店";
		}else if (src.contains("旗舰店")){
			serviceSupplier.type = "旗舰店";
		}

		Elements webElements = doc.select("body > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-evaluate.clearfix > div.shop-evaluate-det > span");
		serviceSupplier.service_quality = Double.parseDouble(webElements.get(0).text());
		serviceSupplier.service_speed = Double.parseDouble(webElements.get(2).text());
		serviceSupplier.service_attitude = Double.parseDouble(webElements.get(4).text());

		try {

			serviceSupplier.good_rating_num = Integer.parseInt(doc.select("body > div.diy-content.preview.J-refuse-external-link > div > div.diy-sec.diy.w990 > div.case2-right > div:nth-child(3) > div.shop-evaluation-newstyle > div > div > div > div > div.filter-comment.J-filter-comment.lh-25 > label.icon-wrap.good.highlight > span")
					.text().replace("好评(", "").replace(")", ""));
			serviceSupplier.bad_rating_num = Integer.parseInt(doc.select("body > div.diy-content.preview.J-refuse-external-link > div > div.diy-sec.diy.w990 > div.case2-right > div:nth-child(3) > div.shop-evaluation-newstyle > div > div > div > div > div.filter-comment.J-filter-comment.lh-25 > label.icon-wrap.bad > span")
					.text().replace("差评(", "").replace(")", ""));
		} catch (NoSuchElementException e) {

		}

		webPageUcenter();
	}

	/**
	 * 猪八戒服务商详情
	 * @param src
	 * @param serviceSupplier
	 */
	public void pageTwo(String src, ServiceSupplier serviceSupplier, Document doc) throws IOException {

		serviceSupplier.name = doc.select("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-desc.J-shop-desc > a > strong")
				.text();
		if (doc.select("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-desc.J-shop-desc > img")
				.size() >= 2) {
			serviceSupplier.type = "企业";
		}
		else {
			serviceSupplier.type = "个人";
		}
		webPageEvaluation();

		webPageUcenter();

		}

	/**
	 *服务商信息
	 */
	public void webPageUcenter() throws IOException {

		Document doc = Jsoup.connect("https://ucenter.zbj.com/rencai/view/" + getUrl().split("/")[3]).get();

		try {
			serviceSupplier.location = doc.select("#utopia_widget_3 > div.shop-center-tit.clearfix > span.fr.active-address")
					.text();
		} catch (NoSuchElementException e) {
			serviceSupplier.location = "";
		}

		serviceSupplier.skills = doc.select("#utopia_widget_5 > p.label-box-wrap")
				.text();
		serviceSupplier.rating = Integer.parseInt(doc.select("#power").text().replaceAll("", "0"));

		serviceSupplier.description = doc.select("#utopia_widget_3 > div.user-about > div > span").text();

		serviceSupplier.rating_num = Integer.parseInt(doc.select("#utopia_widget_4 > div > div.clearfix > span")
				.text().split("（")[1].split("）")[0]);
		String recommendation = doc.select("#evaluationwrap > ul.evaluation-option.clearfix.J-evaluation-option > li:nth-child(6) > a")
				.text();

		serviceSupplier.recommendation_num = Integer.parseInt(recommendation.split("\\(")[1].split("\\)")[0]);

	}

	/**
	 *交易
	 */
	// TODO 天棚网可能有问题 页面具体分析 解决
	public void webPageEvaluation() throws IOException {

		Document doc = Jsoup.connect(getUrl() + "evaluation.html").get();

		Elements webElements = doc.select("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-evaluate.clearfix > div.shop-evaluate-det > span");

		serviceSupplier.service_quality = Double.parseDouble(webElements.get(0).text());
		serviceSupplier.service_speed = Double.parseDouble(webElements.get(2).text());
		serviceSupplier.service_attitude = Double.parseDouble(webElements.get(4).text());

		serviceSupplier.revenue = Double.parseDouble(doc.select("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-balance > span:nth-child(1)")
				.text().replaceAll(",", ""));
		serviceSupplier.project_num = Integer.parseInt(doc.select("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-balance > span:nth-child(2)")
				.text());
		serviceSupplier.success_ratio = (float) (1 - Double.parseDouble(doc.select("body > div.main > div > div.wk-r > div.w-con.shop-service-wrap > div.con.clearfix > div.shop-service-bd > div.fl.shop-service-left > div:nth-child(3) > div.td.td4")
				.text().replaceAll("%", "")) * 0.01);
		serviceSupplier.good_rating_num = Integer.parseInt(doc.select("body > div.main > div > div.wk-r > div:nth-child(3) > div.con.clearfix > div.shop-comment-bd > div.shop-comment-l > div > div:nth-child(4) > span:nth-child(1)")
				.text());
		serviceSupplier.bad_rating_num = Integer.parseInt(doc.select("body > div.main > div > div.wk-r > div:nth-child(3) > div.con.clearfix > div.shop-comment-bd > div.shop-comment-l > div > div:nth-child(4) > span:nth-child(3)")
				.text());
	}

	/**
	 * 获取联系方式
	 * @param src
	 * @param serviceSupplier
	 */
	public void contactWay(String src, ServiceSupplier serviceSupplier, Document doc) {

		if (src.contains("<div class=\"shop-fix-im-qq\">")) {
			try {

				Elements list_qq = doc.select("body > div.shop-fixed-im.sidebar-show > div.shop-fixed-im-hover.shop-customer.j-shop-fixed-im > div.shop-fix-im-qq > div.qq-item");

				String qq = "";
				for (Element webElement : list_qq) {
					qq = qq + " " + webElement.select("a").attr("data-qq");
				}
				serviceSupplier.qq = qq;
			} catch (Exception e) {
			}

			try {
				Elements list_phone = doc.select("body > div.shop-fixed-im.sidebar-show > div.shop-fixed-im-hover.shop-customer.j-shop-fixed-im > div.shop-fix-im-qq > div.time-item");

				String phone = "";
				for (Element webElement : list_phone) {
					phone = phone + " " + webElement.select("a").attr("data-phone");
				}
				serviceSupplier.cellphone = phone;
			} catch (Exception e) {
			}
		}
	}
}