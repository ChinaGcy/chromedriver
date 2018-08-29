package com.sdyk.ai.crawler.specific.company.tianyancha;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.model.company.CompanyFinancing;
import com.sdyk.ai.crawler.model.company.CompanyInformation;
import com.sdyk.ai.crawler.model.company.CompanyStaff;
import com.sdyk.ai.crawler.task.Task;
import com.sdyk.ai.crawler.util.LocationParser;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.action.ClickAction;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskFactory;
import one.rewind.io.requester.task.TaskHolder;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TianyanchaTask extends Task {

	public static long MIN_INTERVAL = 15 * 1000L;

	static {
		registerBuilder(
				TianyanchaTask.class,
				"https://www.tianyancha.com/{{company_id}}",
				ImmutableMap.of("company_id", String.class),
				ImmutableMap.of("company_id", ""),
				true,
				Priority.HIGHEST
		);
	}

	public static String domain(){
		return "tianyancha";
	}

	public TianyanchaTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setNoFetchImages();

		this.setPriority(Priority.HIGHEST);

		this.addAction( new ClickAction("div.summary > span.link-click", 2000));

		this.setValidator((a, t)->{

			String src = getResponse().getText();
			if( src.contains("登陆") && src.contains("注册") ){

				throw new AccountException.Failed(a.accounts.get("tianyancha.com"));
			}

		});

		this.addDoneCallback((t) -> {

			if( getUrl().length() < 28 ){
				return;
			}
			else {

				Document doc = getResponse().getDoc();

				crawler( doc );
			}
		});

	}

	public void crawler( Document doc ){

		String id =  one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(getUrl()));

		CompanyInformation companyInformation = CompanyInformation.getCompanyInformationById(id);

		companyInformation.english_name = doc.select("#_container_baseInfo > div > div.base0910 > table > tbody > tr:nth-child(5) > td:nth-child(4)").text();

		companyInformation.telephone = doc.select("#company_web_top > div.box > div.content > div.detail > div:nth-child(1) > div:nth-child(1) > span:nth-child(2)").text();

		companyInformation.email = doc.select("span.email").text();

		companyInformation.website = doc.select("a.company-link").text();

		String address = doc.select("span.address").text();
		//companyInformation.address = address;

		try {
			List<? extends LocationParser.Location> locations = LocationParser.getInstance().matchLocation(address);
			if( locations.size() > 0 ){
				//companyInformation.location = locations.get(0).toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		companyInformation.content = "<p>" + doc.select("div.body.-detail.modal-scroll").text().replaceAll("简介：", "") + "</p>";

		try {
			companyInformation.info_update_time = DateFormatUtil.parseTime(
					doc.select("span.updatetimeComBox").text());
		} catch (ParseException e) {
			logger.error("error for String to Date", e);
		}

		String[] operatingPeriod = doc.select("#_container_baseInfo > div > div.base0910 > table > tbody > tr:nth-child(4) > td:nth-child(2) > span")
				.text().split("至");
		if( operatingPeriod.length > 1 ){
			if( !operatingPeriod[1].contains("无") ){
				try {
					companyInformation.operating_period = DateFormatUtil.parseTime(operatingPeriod[1]);
				} catch (ParseException e) {
					logger.error("error for String to Date", e);
				}
			}
		}

		//融资情况
		Elements financingList = doc.select("#_container_rongzi > table > tbody > tr");
		int j =0;
		for( Element element : financingList ){
			j++;
			CompanyFinancing companyFinancing = new CompanyFinancing(getUrl() + "?financing=" + j);

			companyFinancing.company_id = companyInformation.id;

			//名称
			String financingName = element.select("td:nth-child(7) > div > a ").text();
			companyFinancing.name.add(financingName);

			String financingName1 = element.select("td:nth-child(7) > div > div > a ").text();
			if( financingName1 != null && financingName1.length() > 0 ){
				companyFinancing.name.add(financingName1);
			}

			//轮次
			companyFinancing.financing_round = element.select("td:nth-child(3)").text();

			try {
				//时间
				companyFinancing.financing_time = DateFormatUtil.parseTime(
						element.select("td:nth-child(2)").text());
			} catch (ParseException e) {
				logger.error("error for String to Date", e);
			}

			//金额
			companyFinancing.financing_amount = element.select("td:nth-child(3)").text();

			//新闻
			companyFinancing.financing_news = element.select("td:nth-child(8)").toString();

			try {
				companyFinancing.insert();
			} catch (Exception e) {
				logger.error("error for companyFinancing.insert");
			}

		}

		companyInformation.update();

		Elements staffList = doc.select("#_container_teamMember > div.card > div.card-team");
		if( staffList != null && staffList.size() > 0 ){

			// 提交天眼查高管任务
			try {

				//设置参数
				Map<String, Object> init_map = new HashMap<>();
				init_map.put("company_name", companyInformation.name);
				init_map.put("page", "1");
				init_map.put("uId", id);

				Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.company.tianyancha.TianyanchaStaffTask");

				//生成holder
				TaskHolder holder =  ChromeTaskFactory.getInstance().newHolder(clazz, init_map);

				//提交任务
				((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

			} catch ( Exception e) {

				logger.error("error for submit TianyanchaStaffTask.class", e);
			}
		}

		// 提交 IT橘子查询任务
		/*try {

			//设置参数
			Map<String, Object> init_map = new HashMap<>();
			init_map.put("company_name", companyInformation.name);
			init_map.put("uId", id);

			Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.company.itjuzi.ItjuziScanTAsk");

			//生成holder
			ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

			//提交任务
			((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

		} catch ( Exception e) {

			logger.error("error for submit ItjuziScanTAsk.class", e);
		}*/

	}

}
