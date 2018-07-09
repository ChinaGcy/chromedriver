package com.sdyk.ai.crawler.specific.itijuzi.task;

import com.sdyk.ai.crawler.model.company.CompanyFinancing;
import com.sdyk.ai.crawler.model.company.CompanyInformation;
import com.sdyk.ai.crawler.model.company.CompanyProduct;
import com.sdyk.ai.crawler.model.company.CompanyStaff;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class AssistItijuziTask extends Task {

	public CompanyInformation companyInformation;
	public CompanyFinancing companyFinancing;

	public AssistItijuziTask(String url, String local) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setPriority(Priority.HIGH);

		this.addDoneCallback(() -> {

			Document doc = getResponse().getDoc();

			crawler(doc, local);

		});

	}

	public void crawler(Document doc, String local){

		companyInformation = new CompanyInformation(getUrl());

		companyFinancing = new CompanyFinancing(getUrl());
		companyFinancing.company_id = companyInformation.id;

		//公司名称
		companyInformation.name = doc.select("h1.seo-important-title").attr("data-name");

		//行业
		companyInformation.industry = doc.select("a.one-level-tag").text();

		//子行业
		companyInformation.tags = doc.select("a.two-level-tag").text();

		//地点
		companyInformation.location =doc.select("i.icon-address-o + span").text();

		//投资
		String financing = doc.select("table.list-round-v2").text();
		if( financing.contains("暂未收录") ){
			companyFinancing.financing_round = "尚未获投";
		}
		else {

			companyFinancing.financing_round = doc.select("span.round").text();
			companyFinancing.financing_amount = doc.select("span.finades").text();

		}

		//成立时间
		String foundedTime = doc.select("body > div.thewrap > div.boxed.invest-info.company-info.company-new > div.main-left-container > div.sec.ugc-block-item.bgpink.mart0 > div.block-inc-info.on-edit-hide > div:nth-child(2) > div > h3:nth-child(2) > span").text();
		try {
			companyInformation.founded_time = DateFormatUtil.parseTime(foundedTime);
		} catch (ParseException e) {
			logger.error("error for String to Date", e);
		}

		//规模
		companyInformation.size = doc.select("body > div.thewrap > div.boxed.invest-info.company-info.company-new > div.main-left-container > div.sec.ugc-block-item.bgpink.mart0 > div.block-inc-info.on-edit-hide > div:nth-child(2) > div > h3:nth-child(3) > span").text();

		//状态
		companyInformation.reg_status = doc.select("div.des-more > span").text();

		//电话
		companyInformation.telephone = doc.select("i.icon-phone-o + span").text();

		//邮箱
		companyInformation.email = doc.select("i.icon-email-o + span").text();

		//团队信息
		Elements introduction = doc.select("div.introduction");
		Elements des = doc.select("div.des");
		if( introduction.size() > 0 ){
			companyInformation.content = introduction.get(0).text();
		}
		else if ( des.size() > 0 ){
			companyInformation.content = des.get(0).text();
		}

		//竞品
		List<Task> task = new ArrayList<>();
		Elements competing = doc.select("i.pic");
		StringBuffer competingId = new StringBuffer();
		for(Element element : competing){

			String cUrl = element.select("a").attr("href");

			/*try {
				task.add(new AssistItijuziTask(cUrl));
			} catch (MalformedURLException | URISyntaxException e) {
				e.printStackTrace();
			}*/

			competingId.append(one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(cUrl)));
			competingId.append(",");

		}
		if(competingId.length() > 2){
			companyInformation.competing_product = competingId.substring(0, competingId.length() - 1);
		}

		//产品
		Elements products = doc.select("ul.product-list > li");
		int i = 1;
		for( Element element : products ){

			i++;
			CompanyProduct companyProduct = new CompanyProduct(getUrl() + "product=" + i);

			companyProduct.company_id = companyInformation.id;

			companyProduct.product_name = element.select("a.product-name").text();

			companyProduct.content = element.select("div.product-de").text();

			companyProduct.insert();
		}

		//高管
		Elements strffs = doc.select("ul.team-list > li");
		int j = 0;
		for( Element element : strffs ){

			j++;
			CompanyStaff companyStaff = new CompanyStaff(getUrl() + "staff=" + j);

			companyStaff.company_id = companyInformation.id;

			companyStaff.name = doc.select("a.person-name").text();

			companyStaff.position = doc.select("div.per-position").text();

			companyStaff.content = doc.select("div.per-des").text();

			companyStaff.insert();

		}

		/*for( Task t : task ){
			ChromeDriverRequester.getInstance().submit(t);
		}
*/
		companyInformation.insert();
		if(companyFinancing.financing_round != null && companyFinancing.financing_round.length() < 1){
			companyFinancing.financing_round = "尚未获投";
		}
		if(companyFinancing.financing_amount != null && companyFinancing.financing_amount.length() < 1){
			companyFinancing.financing_amount = "无";
		}
		companyFinancing.insert();

	}

	@Override
	public one.rewind.io.requester.Task validate() throws ProxyException.Failed, AccountException.Failed, AccountException.Frozen {
		return null;
	}
}
