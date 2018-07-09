package com.sdyk.ai.crawler.specific.shichangbu.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.witkey.ServiceProvider;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.shichangbu.task.Task;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceProviderTask extends Task {

	static {
		registerBuilder(
				ServiceProviderTask.class,
				"http://www.shichangbu.com/agency-{{service_id}}.html",
				ImmutableMap.of("service_id", String.class),
				ImmutableMap.of("service_id", "")
		);
	}

	ServiceProvider serviceProvider;

	public ServiceProviderTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setBuildDom();

		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			crawlerJob(doc);

		});
	}

	/**
	 * 页面解析方法
	 * @param doc
	 */
	public void crawlerJob(Document doc){

		serviceProvider = new ServiceProvider(getUrl());

		serviceProvider.origin_id = getUrl().split("com/")[1].replace(".html","");

		//名称
		serviceProvider.name = doc.select("#se-fws-contact-cont > div > div.se-panel-header > div > span").text();

		//公司名称
		serviceProvider.company_name = doc.getElementsByClass("se-fws-header-title").text();
		if( serviceProvider.name == null || serviceProvider.name.length() <1 ){
			serviceProvider.name = serviceProvider.company_name;
		}

		//类型
		serviceProvider.type = "团队-公司";

		//标签
		serviceProvider.tags = doc.getElementsByClass("se-fws-header-labs").text();

		//成员数量
		String src = getResponse().getText();
		Pattern pTeanNum = Pattern.compile("公司规模:(?<T>.+?)</span>");
		Matcher mTeamNum = pTeanNum.matcher(src);
		if( mTeamNum.find() ) {
			String teamNum = mTeamNum.group("T");
			if( teamNum.contains("-") ) {
				String teamSize = teamNum.split("-")[1];
				teamSize = CrawlerAction.getNumbers(teamSize);
				if( teamSize != null && !"".equals(teamSize) ) {
					serviceProvider.team_size = Integer.valueOf(teamSize);
				}
			}
			//为少于多少人
			else {
				serviceProvider.team_size = Integer.valueOf(CrawlerAction.getNumbers(teamNum));
			}
		}

		//地点
		serviceProvider.location = doc.select(
				"body > div.se-fws-header.se-module > div > div.se-fws-header-left > div.se-fws-header-info > div:nth-child(4) > span:nth-child(1)")
				.text();

		//内容
		serviceProvider.content = doc.select("#se-desc").html();

		//浏览人数
		Pattern pViewNum = Pattern.compile("浏览(?<T>.+?)</span>");
		Matcher mViewNum = pViewNum.matcher(src);
		if( mViewNum.find() ) {
			String view_num = CrawlerAction.getNumbers(mViewNum.group("T"));
			if( view_num != null && !"".equals(view_num) ) {
				serviceProvider.view_num = Integer.valueOf(view_num);
			}
		}

		Elements elements = doc.getElementsByClass("se-fws-contact-item");

		//联系方式
		for(Element element : elements){

			String title = element.getElementsByClass("se-fws-contact-label").text();

			if ( title.contains("手机") ){
				serviceProvider.cellphone = element.getElementsByClass("se-fws-contact-text").text();
			}

			if ( title.contains("邮件") ){
				serviceProvider.email = element.getElementsByClass("se-fws-contact-text").text();
			}

			if ( title.contains("微信") ){
				serviceProvider.weixin = element.getElementsByClass("se-fws-contact-text").text();
			}

			if ( title.contains("固定电话") ){
				serviceProvider.telephone = element.getElementsByClass("se-fws-contact-text").text();
			}

			if ( title.contains("QQ") ){
				serviceProvider.qq = element.getElementsByClass("se-fws-contact-text").text();
			}

		}

		Pattern p = Pattern.compile("href=\"portal.php?(?<T>.+?)\" target=\"_blank\" title=\"");

		//服务任务
		String src1 = doc.select("#se-product").html();
		Set<String> t1Set = new HashSet<>();
		Matcher m1 = p.matcher(src1);
		while(m1.find()) {
			t1Set.add(m1.group("T"));
		}

		//项目任务
		String src2 = doc.select("#se-case").html();
		Set<String> t2Set = new HashSet<>();
		Matcher m2 = p.matcher(src2);
		while(m2.find()) {
			t2Set.add(m2.group("T"));
		}

		//生成 workTask
		for(String T : t2Set){

			String work_id = "http://www.shichangbu.com/portal.php" + T.replace(";","&")
					.replace("&amp","");

			try {
				HttpTaskPoster.getInstance().submit(WorkTask.class,
						ImmutableMap.of("work_id", work_id));
			} catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {

				logger.error("error fro HttpTaskPoster.submit WorkTask.class", e);
			}

		}

		//生成 CaseTask
		for(String T : t1Set){

			String case_id = T.replace(";","&")
					.replace("&amp","");

			try {
				HttpTaskPoster.getInstance().submit(CaseTask.class,
						ImmutableMap.of("case_id", case_id));
			} catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {

				logger.error("error fro HttpTaskPoster.submit CaseTask.class", e);
			}

		}

		//插入数据库
		try {
			serviceProvider.insert();
		} catch (Exception e) {
			logger.error("serviceProvider.insert() error", serviceProvider.toJSON(), e);
		}

	}

}
