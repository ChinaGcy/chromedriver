package com.sdyk.ai.crawler.specific.itijuzi.task;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.company.CompanyFinancing;
import com.sdyk.ai.crawler.model.company.CompanyInformation;
import com.sdyk.ai.crawler.model.company.CompanyProduct;
import com.sdyk.ai.crawler.model.company.CompanyStaff;
import com.sdyk.ai.crawler.specific.proLagou.task.scanTask.ProjectScanTask;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.action.ClickAction;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CompanyTask extends Task {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				CompanyTask.class,
				"https://www.itjuzi.com/company/{{id}}",
				ImmutableMap.of("id", String.class),
				ImmutableMap.of("id","33081784")
		);
	}

	public CompanyTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setBuildDom();

		this.setPriority(Priority.HIGH);

		this.setValidator((a, t) -> {

		});

		this.addDoneCallback((t) -> {

			Document doc = t.getResponse().getDoc();

			proc(doc);

		});
	}

	public void proc(Document doc){

		CompanyInformation c_info = new CompanyInformation(getUrl());

		CompanyFinancing c_financing = new CompanyFinancing(getUrl());

		c_financing.company_id = c_info.id;

		// 公司名称
		c_info.name = doc.select("h1.seo-important-title").attr("data-name");

		// 行业
		c_info.industry = doc.select("a.one-level-tag").text();

		// 子行业
		c_info.tags = doc.select("a.two-level-tag").text();

		// 地点
		c_info.location =doc.select("i.icon-address-o + span").text();

		// 投资
		String financing = doc.select("table.list-round-v2").text();
		if( financing.contains("暂未收录") ){
			c_financing.financing_round = "尚未获投";
		}
		else {
			c_financing.financing_round = doc.select("span.round").text();
			c_financing.financing_amount = doc.select("span.finades").text();

		}

		// 成立时间
		String foundedTime = doc.select("body > div.thewrap > div.boxed.invest-info.company-info.company-new > div.main-left-container > div.sec.ugc-block-item.bgpink.mart0 > div.block-inc-info.on-edit-hide > div:nth-child(2) > div > h3:nth-child(2) > span").text();
		try {
			c_info.founded_time = DateFormatUtil.parseTime(foundedTime);
		} catch (ParseException e) {
			logger.error("error for String to Date", e);
		}

		// 规模
		c_info.size = doc.select("body > div.thewrap > div.boxed.invest-info.company-info.company-new > div.main-left-container > div.sec.ugc-block-item.bgpink.mart0 > div.block-inc-info.on-edit-hide > div:nth-child(2) > div > h3:nth-child(3) > span").text();

		// 状态
		c_info.reg_status = doc.select("div.des-more > span").text();

		// 电话
		c_info.telephone = doc.select("i.icon-phone-o + span").text();

		// 邮箱
		c_info.email = doc.select("i.icon-email-o + span").text();

		// 团队信息
		Elements introduction = doc.select("div.introduction");
		Elements des = doc.select("div.des");
		if( introduction.size() > 0 ){
			c_info.content = introduction.get(0).text();
		}
		else if ( des.size() > 0 ){
			c_info.content = des.get(0).text();
		}

		// 竞品公司
		List<Task> task = new ArrayList<>();
		Elements competing = doc.select("i.pic");
		StringBuffer competingId = new StringBuffer();
		for(Element element : competing){

			String cUrl = element.select("a").attr("href");

			competingId.append(one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(cUrl)));
			competingId.append(",");

		}
		if(competingId.length() > 2){
			c_info.competing_company_ids = competingId.substring(0, competingId.length() - 1);
		}

		// 产品
		Elements products = doc.select("ul.product-list > li");
		int i = 1;
		for( Element element : products ){

			i++;
			CompanyProduct c_product = new CompanyProduct(getUrl() + "product=" + i);

			c_product.company_id = c_info.id;

			c_product.product_name = element.select("a.product-name").text();

			c_product.content = element.select("div.product-de").text();

			c_product.insert();
		}

		// 高管
		Elements staffs = doc.select("ul.team-list > li");
		int j = 0;
		for( Element element : staffs ){

			String starffUrtl = element.select("a.avatar").attr("href");

			CompanyStaff c_staff = new CompanyStaff(starffUrtl);

			c_staff.company_id = c_info.id;

			c_staff.name = element.select("a.person-name").text();

			c_staff.position = element.select("div.per-position").text();

			c_staff.content = element.select("div.per-des").text();

			c_staff.insert();

		}

		c_info.insert();
		if(c_financing.financing_round != null && c_financing.financing_round.length() < 1){
			c_financing.financing_round = "尚未获投";
		}
		if(c_financing.financing_amount != null && c_financing.financing_amount.length() < 1){
			c_financing.financing_amount = "无";
		}
		c_financing.insert();
	}

	public static void registerBuilder(Class<? extends ChromeTask> clazz, String url_template, Map<String, Class> init_map_class, Map<String, Object> init_map_defaults){
		ChromeTask.registerBuilder( clazz, url_template, init_map_class, init_map_defaults );
	}
}
