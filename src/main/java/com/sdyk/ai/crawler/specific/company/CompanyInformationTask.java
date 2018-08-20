package com.sdyk.ai.crawler.specific.company;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.model.company.CompanyInformation;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.task.Task;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskFactory;
import one.rewind.io.requester.task.TaskHolder;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.*;

public class CompanyInformationTask extends Task {

	public static long MIN_INTERVAL = 1000L;

	static {
		registerBuilder(
				CompanyInformationTask.class,
				"http://testwww.315free.com/tianyancha/info?q={{company_name}}",
				ImmutableMap.of("company_name", String.class),
				ImmutableMap.of("company_name", "")
		);
	}

	public CompanyInformationTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setPriority(Priority.HIGHEST);

		//this.setNoFetchImages();

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			crawler( doc );

		});

	}

	public void crawler( Document doc ){

		CompanyInformation companyInfo;

		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = null;

		try {
			node = mapper.readTree(doc.text());
			node = node.get("data");
		} catch (IOException e) {
			logger.error("error on String to json", e);
		}

		if( node != null ){

			//公司ID
			String company_id = node.get("id").toString().replaceAll("\"","");

			String id = "https://www.tianyancha.com/company/"+ company_id;
			companyInfo = new CompanyInformation(id);

			//公司名称
			companyInfo.name = node.get("name").textValue();

			//工商注册号
			if( node.get("regNumber") != null && node.get("regNumber").size() > 0 ){
				companyInfo.reg_number = node.get("regNumber").textValue();
			}

			//注册成本
			companyInfo.reg_capital = Double.valueOf(CrawlerAction.getNumbers(node.get("regCapital").textValue()));

			//注册机关
			companyInfo.reg_institute = node.get("regInstitute").textValue();

			//注册地址
			companyInfo.reg_location = node.get("regLocation").textValue();
			companyInfo.address = companyInfo.reg_location;

			//行业
			companyInfo.industry = node.get("industry").textValue();

			//认证时间
			try {
				companyInfo.approved_time = DateFormatUtil.parseTime(node.get("approvedTime").textValue());
			} catch (ParseException e) {
				logger.error("error for String to Date", e);
			}

			//logo
			String logoUrl = node.get("logo").textValue();
			String orgUrl = "http://img.tianyancha.com/";

			Map<String, String> map = new HashMap();
			map.put(logoUrl, "logo");
			List<String> logoList = BinaryDownloader.download(getUrl(), map);
			if( logoList != null ){
				companyInfo.logo = logoList.get(0);
			}

			//统一信用代码
			companyInfo.tax_number = node.get("taxNumber").textValue();

			//经营范围
			companyInfo.business_scope = node.get("businessScope").textValue();

			//组织机构代码
			companyInfo.org_number = node.get("orgNumber").textValue();

			//公司状态
			companyInfo.reg_status = node.get("regStatus").textValue();

			//法人
			companyInfo.legal_person_name = node.get("legalPersonName").textValue();

			//类型
			companyInfo.type = node.get("companyOrgType").textValue();

			//纳税人识别号
			companyInfo.credit_code = node.get("creditCode").textValue();

			companyInfo.insert();

			// 提交天眼查查询任务
			try {

				//设置参数
				Map<String, Object> init_map = new HashMap<>();
				init_map.put("company_id", ("company/" + company_id));

				Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.company.tianyancha.TianyanchaTask");

				//生成holder
				TaskHolder holder =  ChromeTaskFactory.getInstance().newHolder(clazz, init_map);

				//提交任务
				((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

			} catch ( Exception e) {

				logger.error("error for submit TianyanchaTask.class", e);
			}

			// 提交拉钩查询任务
			try {

				//设置参数
				Map<String, Object> init_map = new HashMap<>();
				init_map.put("company_name", companyInfo.name);
				init_map.put("uId", companyInfo.id);

				Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.company.lagou.LagouScanTask");

				//生成holder
				TaskHolder holder =  ChromeTaskFactory.getInstance().newHolder(clazz, init_map);

				//提交任务
				((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

			} catch ( Exception e) {

				logger.error("error for submit LagouScanTask.class", e);
			}

		}
		else {
			// 提交天眼查查询任务
			try {

				//设置参数
				Map<String, Object> init_map = new HashMap<>();
				init_map.put("company_id", "");

				Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.company.tianyancha.TianyanchaTask");

				//生成holder
				TaskHolder holder =  ChromeTaskFactory.getInstance().newHolder(clazz, init_map);

				//提交任务
				((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

			} catch ( Exception e) {

				logger.error("error for submit TianyanchaTask.class", e);
			}
		}

	}
}
