package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.witkey.Resume;
import com.sdyk.ai.crawler.model.witkey.ServiceProvider;
import com.sdyk.ai.crawler.specific.zbj.task.Task;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import com.sdyk.ai.crawler.util.DateFormatUtil;
import com.sdyk.ai.crawler.util.LocationParser;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskFactory;
import one.rewind.io.requester.task.TaskHolder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceProviderTask extends Task {

	public static long MIN_INTERVAL = 24 * 60 * 60 * 1000L;

	public static List<String> crons = Arrays.asList("* * */1 * *", "* * */2 * *", "* * */4 * *", "* * */8 * *");

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

				serviceProvider.origin_id = t.getStringFromVars("user_id");

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
					serviceProvider.platform_certification = new ArrayList<>();
					if (!serviceProvider.type.contains("旗舰") && !serviceProvider.type.contains("专业")) {
						serviceProvider.platform_certification.add(serviceProvider.type);
					} else {
						serviceProvider.platform_certification.add("企业");
					}

					serviceProvider.domain_id = 1;

					LocationParser parser = LocationParser.getInstance();
					if (serviceProvider.location != null && serviceProvider.location.length() > 0) {
						serviceProvider.location = parser.matchLocation(serviceProvider.location).get(0).toString();
					}
					serviceProvider.insert();
				} catch (Exception e) {
					logger.error("insert/update error {}", e);
				}

				try {

					//设置参数
					Map<String, Object> init_map = ImmutableMap.of("user_id", serviceProvider.origin_id, "page", String.valueOf(1));

					Class<? extends ChromeTask> clazz = (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.zbj.task.modelTask.ServiceProviderRatingTask");

					//生成holder
					TaskHolder holder = ChromeTaskFactory.getInstance().newHolder(clazz, init_map);

					//提交任务
					ChromeDriverDistributor.getInstance().submit(holder);

				} catch (Exception e) {

					logger.error("error for submit ServiceProviderRatingTask.class", e);
				}

				try {

					//设置参数
					Map<String, Object> init_map = ImmutableMap.of("user_id", serviceProvider.origin_id, "page", String.valueOf(1));

					Class<? extends ChromeTask> clazz = (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.zbj.task.scanTask.CaseScanTask");

					//生成holder
					TaskHolder holder = ChromeTaskFactory.getInstance().newHolder(clazz, init_map);

					//提交任务
					ChromeDriverDistributor.getInstance().submit(holder);

				} catch (Exception e) {

					logger.error("error for submit CaseScanTask.class", e);
				}


				try {

					//设置参数
					Map<String, Object> init_map = ImmutableMap.of("user_id", serviceProvider.origin_id, "page", String.valueOf(1));

					Class<? extends ChromeTask> clazz = (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.zbj.task.scanTask.WorkScanTask");

					//生成holder
					TaskHolder holder = ChromeTaskFactory.getInstance().newHolder(clazz, init_map);

					//提交任务
					ChromeDriverDistributor.getInstance().submit(holder);

				} catch (Exception e) {

					logger.error("error for submit CaseScanTask.class", e);
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
		try {
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

				Elements el = doc.select(".my-home");
				if(el != null) {
					String text = el.text();

					Pattern pattern = Pattern.compile("一周浏览量：(?<view>\\d+)次");
					Pattern pattern_ = Pattern.compile("收藏量：(?<fav>\\d+)人");

					Matcher matcher = pattern.matcher(text);
					Matcher matcher_ = pattern_.matcher(text);

					if (matcher.find()) {
						serviceProvider.view_num = Integer.parseInt(matcher.group("view"));
					}
					if (matcher_.find()) {
						serviceProvider.fav_num = Integer.parseInt(matcher.group("fav"));
					}
				}
			}

			// 图片下载
			Elements elements = doc.select("body > div.diy-content.preview.J-refuse-external-link > div > div:nth-child(2) > div.part");

			Map<String, String> map = new HashMap<>();

			for (Element e : elements) {
				map.put(e.select("img").attr("src"), e.select("img").attr("src").split("/task/")[1].split("/")[0]);
			}

			// 图片下载
			serviceProvider.cover_images = BinaryDownloader.download(getUrl(), map);

		} catch (Exception e) {

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

		//
		webPageUcenter();
		try {
			// 服务商详情
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

		// head 店铺主页
		if (src.contains("关于我们") && doc.title().contains("店铺主页")) {

			serviceProvider.name = doc.select("#utopia_widget_4 > div.right-wrap.fr.tag-content > h1").text();
			serviceProvider.grade = doc.select("#utopia_widget_4 > div.right-wrap.fr.tag-content > div.tag-wrap > div.ability-tag.ability-tag-0.text-tag").text();
			serviceProvider.type = doc.select("#utopia_widget_4 > div.right-wrap.fr.tag-content > div.tag-wrap > div.personal-tag.text-tag").text();
			serviceProvider.content = doc.select("#utopia_widget_4 > div.right-wrap.fr.tag-content > div.about-wrap > div.no-about").text();
			serviceProvider.location = doc.select("#utopia_widget_4 > div.left-wrap.fl > div.address-wrap").text();
			serviceProvider.addTag(doc.select("#utopia_widget_15 > div.skill-wrap").text().split(" "));
			serviceProvider.rating_num = Integer.parseInt(doc.select("#utopia_widget_18 > div.left-card-title").text().split("（")[1].split("）")[0]);

			String s = doc.select("#head-img").attr("src");
			Map<String, String> url_filename = new HashMap<>();
			url_filename.put(s, "head_portrait");
			List<String> headList = BinaryDownloader.download(getUrl(), url_filename);
			if( headList != null ){
				serviceProvider.head_portrait = headList.get(0);
			}

			serviceProvider.insert();

			if (doc.select("#utopia_widget_4 > div.right-wrap.fr.tag-content > div.about-wrap > div.our-info > div.work-wrap").text().contains("工作经历")) {

				getResumeInfo_work(doc);
			}

			if (doc.select("#utopia_widget_4 > div.right-wrap.fr.tag-content > div.about-wrap > div.our-info > div.study-wrap").text().contains("教育经历")) {

				getResumeInfo_study(doc);
			}

			return;
		}

		serviceProvider.name = doc.select("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-desc.J-shop-desc > a > h1")
				.text();
		if (doc.select("#j-zbj-header > div.personal-shop-more-info > div > div > div.personal-shop-name > div.personal-shop-desc.J-shop-desc > img")
				.size() >= 2) {
			serviceProvider.type = "企业";
		}
		else {
			serviceProvider.type = "个人";
		}
		// 交易评价
		webPageEvaluation();

		webPageUcenter();

		// 服务商档案
		ZBJSalerinfo();

	}

	/**
	 * 服务商信息
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

		serviceProvider.tags = Arrays.asList(doc.select("#utopia_widget_5 > p.label-box-wrap")
				.text().split(" "));
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

		// 获取头像 #utopia_widget_2 > div > a > img
		String head = doc.select("#utopia_widget_2 > div > a > img").attr("src");
		Map<String, String> url_filename = new HashMap<>();
		url_filename.put(head, "head_portrait");
		List<String> headList = BinaryDownloader.download(getUrl(), url_filename);
		if( headList != null ){
			serviceProvider.head_portrait = headList.get(0);
		}
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
		String head1 = StringUtil.cleanContent(head, head_img);
		this.download(head);
		serviceProvider.head_portrait = one.rewind.txt.StringUtil.byteArrayToHex(
				one.rewind.txt.StringUtil.uuid(head1));;
	}

	/**
	 * 天棚网 服务商信息  https://shop.tianpeng.com/15199471/salerinfo.html
	 * @throws IOException
	 */
	public void TPSalerinfo() throws IOException {

		Document doc = Jsoup.connect(getUrl() + "salerinfo.html").get();
		serviceProvider.company_name = serviceProvider.name;

		serviceProvider.company_address = doc.select("body > div.grid > div.main-wrap > div > div > div > div.introduce-content.introduce-content-first > div > dl > dd").text();

		serviceProvider.content = doc.select("body > div.grid > div.main-wrap > div > div > div > div.introduce-content.introduce-content-first > p.introduce-company-msg").text();

		// 图片下载
		Elements elements = doc.select("body > div.grid > div.main-wrap > div > div > div:nth-child(2) > div > ul > li");

		Map<String, String> map = new HashMap<>();

		for (Element e : elements) {
			map.put(e.select("div > img").attr("src"), e.select("span").text());
		}

		serviceProvider.cover_images.addAll(BinaryDownloader.download(getUrl(), map));

		//serviceProvider.tags = Arrays.asList(doc.select("body > div.grid > div.main-wrap > div > div > div:nth-child(3) > div.introduce-content > div").text().split(" "));

	}

	/**
	 * 猪八戒 服务商信息  https://shop.zbj.com/7163110/salerinfo.html
	 * @throws IOException
	 */
	public void ZBJSalerinfo() throws IOException {

		Document doc = Jsoup.connect(getUrl() + "salerinfo.html").get();
		serviceProvider.company_name = doc.select("#utopia_widget_4 > div.right-wrap.fr.tag-content > div.company-wrap > div:nth-child(2)")
				.text()
				.replace("公司名称：", "");

		serviceProvider.company_address = doc.select("#utopia_widget_4 > div.right-wrap.fr.tag-content > div.company-wrap > div:nth-child(2)")
				.text().replace("公司地址：", "");

		serviceProvider.content = doc.select("#utopia_widget_4 > div.right-wrap.fr.tag-content > div.about-wrap > pre")
				.text();

		// 图片下载
		Elements elements = doc.select("#certificate-carousel-con > div > div > div");

		Map<String, String> map = new HashMap<>();

		for (Element e : elements) {
			map.put(e.select("img.certificate-img").attr("src"), e.select("div").text());
		}

		// 图片下载
		serviceProvider.cover_images =BinaryDownloader.download(getUrl(), map);

		//serviceProvider.tags = Arrays.asList(doc.select("#utopia_widget_15 > div.skill-wrap").text().split(" "));

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
				//body > div.shop-fixed-im.sidebar-show > div.shop-fixed-im-hover.shop-customer.j-shop-fixed-im > div.shop-fix-im-qq > div.time-item
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

	/**
	 * 工作经历
	 * @param doc
	 */
	public void getResumeInfo_work(Document doc) {

		Elements elements = doc.select("#utopia_widget_4 > div.right-wrap.fr.tag-content > div.about-wrap > div.our-info > div.work-wrap > div.content-item");

		for (int i = 0; i < elements.size(); i++) {

			Resume resume = new Resume(getUrl() +"?_work_"+ i);
			resume.user_id = serviceProvider.id;

			String time = elements.get(i).select("div.time.item").text();
			String st = time.split("~")[0];
			int indes = st.split("\\.").length - 1;

			if (indes == 0) {
				st = st + ".01.01";
			}
			if (indes == 1) {
				st = st + ".01";
			}

			resume.sd = DateFormatUtil.parseTime(st);


			if (time.split("~")[1].contains("至今")) {
				resume.ed = new Date();
			} else {
				String et = time.split("~")[1];
				int inde = et.split("\\.").length - 1;

				if (inde == 0) {
					et = et + ".01.01";
				}
				if (inde == 1) {
					et = et + ".01";
				}

				resume.ed = DateFormatUtil.parseTime(et);

			}

			resume.org = elements.get(i).select("div.name.item").text();
			resume.degree_occupation = elements.get(i).select("div.position.item").text();

			resume.insert();
		}
	}

	/**
	 * 教育经历
	 * @param doc
	 */
	public void getResumeInfo_study(Document doc) {

		Elements elements = doc.select("#utopia_widget_4 > div.right-wrap.fr.tag-content > div.about-wrap > div.our-info > div.study-wrap > div.content-item");

		for (int i = 0; i < elements.size(); i++) {

			Resume resume = new Resume(getUrl() +"?_study_"+ i);
			resume.user_id = serviceProvider.id;

			String time = elements.get(i).select("div.time.item").text();
			String st = time.split("~")[0];
			int indes = st.split("\\.").length - 1;

			if (indes == 0) {
				st = st + ".01.01";
			}
			if (indes == 1) {
				st = st + ".01";
			}

			resume.sd = DateFormatUtil.parseTime(st);


			if (time.split("~")[1].contains("至今")) {
				resume.ed = new Date();
			} else {
				String et = time.split("~")[1];
				int inde = et.split("\\.").length - 1;

				if (inde == 0) {
					et = et + ".01.01";
				}
				if (inde == 1) {
					et = et + ".01";
				}

				resume.ed = DateFormatUtil.parseTime(et);

			}

			// #utopia_widget_4 > div.right-wrap.fr.tag-content > div.about-wrap > div.our-info > div.work-wrap > div.content-item > div.name.item
			resume.org = elements.get(i).select("div.name.item").text();
			resume.degree_occupation = elements.get(i).select("div.educlass.item").text();
			resume.content = elements.get(i).select("div.field.item").text();

			resume.insert();
		}
	}
}