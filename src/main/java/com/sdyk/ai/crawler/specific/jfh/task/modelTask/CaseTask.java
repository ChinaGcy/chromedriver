package com.sdyk.ai.crawler.specific.jfh.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.witkey.Case;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.jfh.task.Task;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.task.ChromeTask;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Map;

public class CaseTask extends Task {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				CaseTask.class,
				"http://shop.jfh.com/{{user_id}}",
				ImmutableMap.of("user_id", String.class),
				ImmutableMap.of("user_id", ""),
				true,
				Priority.LOW
		);
	}

	public CaseTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setPriority(Priority.LOW);

		this.setValidator((a,t) -> {

			String src = getResponse().getText();
			if( src.contains("Log in JointForce") && src.contains("Don't have an account?") ){

				throw new AccountException.Failed(a.accounts.get("jfh.com"));

			}

		});

		//this.setNoFetchImages();

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			crawler(doc);

		});
	}

	public void crawler(Document doc){

		Case caseInfo = new Case(getUrl());

		String serUrl = getUrl().split("SL")[0] + "bu";

		caseInfo.user_id = one.rewind.txt.StringUtil.byteArrayToHex(
				one.rewind.txt.StringUtil.uuid(serUrl));

		//描述
		caseInfo.content = doc.select("#showdetail").toString();

		//标题
		caseInfo.title = doc.select("#wrap > div.showDetails > div.showDetails_top > div.booth-detailed > h1").text();

		//价格
		String price = CrawlerAction.getNumbers(doc.select("#showPrice > div > i").text());
		if(!"".equals(price)){
			caseInfo.budget_ub = Double.valueOf(price);
			caseInfo.budget_lb = Double.valueOf(price);
		}

		//成交量
		String purchaseNum = CrawlerAction.getNumbers(doc.select("#dis_dealNum").text());
		if(!"".equals(purchaseNum)){
			caseInfo.purchase_num =Integer.valueOf(purchaseNum);
		}

		//大类
		caseInfo.category = doc.select("#showdetail-category-categories").text();

		//标签
		caseInfo.tags = doc.select("#showdetail-category > div:nth-child(3) > div:nth-child(2)").text();

		//评价
		String rating = CrawlerAction.getNumbers(doc.select("#dis_rateNum").text());
		if(!"".equals(rating)){
			caseInfo.rating = Float.valueOf(rating);
		}

		//评价数
		Elements ratingList = doc.select("div.translateAll");
		caseInfo.rate_num = ratingList.size();

		try{
			caseInfo.insert();
		} catch (Exception e) {
			logger.error("error for caseInfo.insert", e);
		}

	}

	public static void registerBuilder(Class<? extends ChromeTask> clazz, String url_template, Map<String, Class> init_map_class, Map<String, Object> init_map_defaults){
		ChromeTask.registerBuilder( clazz, url_template, init_map_class, init_map_defaults );
	}

}
