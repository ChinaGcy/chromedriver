package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.ServiceProvider;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.WorkScanTask;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.CaseScanTask;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.exception.ProxyException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ServiceProviderTask extends Task {

	static {
		registerBuilder(
				ServiceProviderTask.class,
				"https://shop.zbj.com/{{user_id}}/",
				ImmutableMap.of("user_id", String.class),
				ImmutableMap.of("user_id", "0"),
				false,
				Priority.MEDIUM
		);
	}


	// 实例化
	ServiceProvider serviceProvider;

	public ServiceProviderTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setBuildDom();

		serviceProvider = new ServiceProvider(getUrl());

		this.addDoneCallback((t) -> {

			try {

				String src = getResponse().getText();
				Document doc = getResponse().getDoc();

				List<com.sdyk.ai.crawler.task.Task> tasks = new ArrayList<>();

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
				/*tasks.add(ServiceProviderRatingTask.generateTask(serviceProvider.origin_id, 1));
				tasks.add(CaseScanTask.generateTask(serviceProvider.origin_id, 1));
				tasks.add(WorkScanTask.generateTask(getUrl(), 1));

				for (com.sdyk.ai.proc.task.Task t : tasks) {
					ChromeTaskScheduler.getInstance().submit(t);
				}*/

				HttpTaskPoster.getInstance().submit(ServiceProviderRatingTask.class,
						ImmutableMap.of("user_id", serviceProvider.origin_id, "page", String.valueOf(1))
						);
				HttpTaskPoster.getInstance().submit(CaseScanTask.class,
						ImmutableMap.of("user_id", serviceProvider.origin_id, "page", String.valueOf(1))
						);
				HttpTaskPoster.getInstance().submit(WorkScanTask.class,
						ImmutableMap.of("user_id", serviceProvider.origin_id, "page", String.valueOf(1))
						);
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

		serviceProvider.name = doc.select("body > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-desc > a").attr("title");
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
		try {
			TPSalerinfo();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 猪八戒服务商详情
	 * @param src
	 * @param serviceProvider
	 */
	public void pageTwo(String src, ServiceProvider serviceProvider, Document doc) throws IOException {

		serviceProvider.name = doc.select("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-desc.J-shop-desc > a > h1")
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
			serviceProvider.rating = Float.parseFloat(doc.select("#power").text().replaceAll("", "0"))/20;
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

		// 获取头像
		String head = doc.select("body > div.grid > div.sidebar > div > div > div > div.w-head-pic > img").html();
		Set<String> head_img = new HashSet<>();
		String head1 = StringUtil.cleanContent(head, head_img,null, null);
		this.download(head);
		serviceProvider.head_portrait = one.rewind.txt.StringUtil.byteArrayToHex(
				one.rewind.txt.StringUtil.uuid(head1));;
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

		// 获取头像 body > div.main > div > div.wk-l > div > div.w-head-pic > img
		String head = doc.select("body > div.main > div > div.wk-l > div > div.w-head-pic > img").outerHtml();
		Set<String> head_img = new HashSet<>();
		String head1 = StringUtil.cleanContent(head, head_img,null, null);
		this.download(head);
		serviceProvider.head_portrait = one.rewind.txt.StringUtil.byteArrayToHex(
				one.rewind.txt.StringUtil.uuid(head1));;
	}

	// 天棚网 服务商信息  https://shop.tianpeng.com/15199471/salerinfo.html
	public void TPSalerinfo() throws IOException {

		Document doc = Jsoup.connect(getUrl() + "salerinfo.html").get();
		serviceProvider.company_name = serviceProvider.name;

		serviceProvider.company_address = doc.select("body > div.grid > div.main-wrap > div > div > div > div.introduce-content.introduce-content-first > div > dl > dd").text();

		serviceProvider.content = doc.select("body > div.grid > div.main-wrap > div > div > div > div.introduce-content.introduce-content-first > p.introduce-company-msg").text();

		String head = doc.select("body > div.grid > div.main-wrap > div > div > div > div > ul > li.license-item.fl > div.license-item-pic").html();
		Set<String> head_img = new HashSet<>();
		String head1 = StringUtil.cleanContent(head, head_img,null, null);
		this.download(head);
		serviceProvider.cover_images = one.rewind.txt.StringUtil.byteArrayToHex(
				one.rewind.txt.StringUtil.uuid(head1));

		serviceProvider.tags = doc.select("body > div.grid > div.main-wrap > div > div > div > div.introduce-content > div").text();




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

				String cellphone = "";
				String telephone = "";
				for (Element webElement : list_phone) {

					if (!webElement.select("a").attr("data-phone").contains("-")) {
						cellphone = cellphone + " " + webElement.select("a").attr("data-phone");
					} else {
						telephone = telephone + " " + webElement.select("a").attr("data-phone");
					}
				}
				if (!cellphone.equals("") && !telephone.equals("")) {
					serviceProvider.cellphone = cellphone;
					serviceProvider.telephone = telephone;
				}

			} catch (Exception e) {}
		}
	}
}