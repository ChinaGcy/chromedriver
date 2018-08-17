package com.sdyk.ai.crawler.specific.shichangbu.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.witkey.ServiceProvider;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.shichangbu.task.Task;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.ChromeTaskScheduler;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import one.rewind.io.requester.task.ScheduledChromeTask;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceProviderTask extends Task {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	public static List<String> crons = Arrays.asList("* * */1 * *", "* * */2 * *", "* * */4 * *", "* * */8 * *");

	static {
		registerBuilder(
				ServiceProviderTask.class,
				"http://www.shichangbu.com/agency-{{service_id}}.html",
				ImmutableMap.of("service_id", String.class),
				ImmutableMap.of("service_id", "")
		);
	}

	public ServiceProviderTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setPriority(Priority.HIGH);

		this.setNoFetchImages();

		// 检测异常
		this.setValidator((a,t) -> {

			String src = getResponse().getText();
			if( src.contains("账号登陆")
					&& src.contains("第三方登陆")){

				throw new AccountException.Failed(a.accounts.get("shichangbu.com"));
			}
		});

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			crawlerJob(doc, (ChromeTask)t);

		});
	}

	/**
	 * 页面解析方法
	 * @param doc
	 */
	public void crawlerJob(Document doc, ChromeTask t){

		ServiceProvider serviceProvider = new ServiceProvider(getUrl());

		serviceProvider.origin_id = getUrl().split("com/")[1].replace(".html","");

		//名称
		serviceProvider.name = doc.select("div.se-fws-header-title").text();

		//公司名称
		serviceProvider.company_name = doc.getElementsByClass("se-fws-header-title").text();
		if( serviceProvider.name == null || serviceProvider.name.length() <1 ){
			serviceProvider.name = serviceProvider.company_name;
		}

		//类型
		serviceProvider.type = "团队-公司";

		//标签
		serviceProvider.tags = doc.getElementsByClass("se-fws-header-labs").text().replace(" ",",");

		//成员数量
		String src = doc.toString();
		Pattern pTeanNum = Pattern.compile("公司规模:(?<T>.+?)人");
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
				"body > div.se-fws-header.se-module > div > div.se-fws-header-left > div.se-fws-header-info > div:nth-child(4) > span:nth-child(1)").text();

		// 公司地点
		serviceProvider.company_address = doc.select(
				"body > div.se-fws-header.se-module > div > div.se-fws-header-left > div.se-fws-header-info > span > span")
				.text().replace("公司地址：", "");

		// 公司网址
		serviceProvider.company_website = doc.select("i.se-fws-header-a").text();

		// 头像
		String head_portrait = doc.select("img.se-fws-header-img").attr("src");
		serviceProvider.head_portrait = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(head_portrait));
		Map<String, String> url_filename = new HashMap<>();
		url_filename.put(head_portrait, "head_portrait");
		BinaryDownloader.download(getUrl(), url_filename);


		Set<String> set = new HashSet<>();
		//内容
		serviceProvider.content = StringUtil.cleanContent(doc.select("#se-desc").html(), set);
		if( set.size() > 0 ){
			serviceProvider.content = BinaryDownloader.download(serviceProvider.content, set, getUrl());
		}

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

			String work_id = T.replace(";","&")
					.replace("&amp","");

			try {

				//设置参数
				Map<String, Object> init_map = new HashMap<>();
				init_map.put("work_id", work_id);

				Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.shichangbu.task.modelTask.WorkTask");

				//生成holder
				ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

				//提交任务
				((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

			} catch ( Exception e) {

				logger.error("error for submit WorkTask.class", e);
			}

		}

		//生成 CaseTask
		for(String T : t1Set){

			String case_id = T.replace(";","&")
					.replace("&amp","");

			try {

				//设置参数
				Map<String, Object> init_map = new HashMap<>();
				init_map.put("case_id", case_id);

				Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.shichangbu.task.modelTask.CaseTask");

				//生成holder
				ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

				//提交任务
				((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

			} catch ( Exception e) {

				logger.error("error for submit CaseTask.class", e);
			}

		}

		serviceProvider.domain_id = 8;

		//插入数据库
		try {
			boolean status = false;

			if( serviceProvider.name != null && serviceProvider.name.length() > 1 ){

				try {

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					init_map.put("company_name", serviceProvider.name);

					Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.company.CompanyInformationTask");

					//生成holder
					ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

					//提交任务
					((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

				} catch (Exception e){
					logger.error("error for create CompanyInformationTask", e);
				}


				status = serviceProvider.insert();
			}

			ScheduledChromeTask st = t.getScheduledChromeTask();

			// 第一次抓取生成定时任务
			if(st == null) {

				try {
					st = new ScheduledChromeTask(t.getHolder(this.init_map), crons);
					st.start();
				} catch (Exception e) {
					logger.error("error for creat ScheduledChromeTask", e);
				}

			}
			else {
				if( !status ){
					st.degenerate();
				}
			}

		} catch (Exception e) {
			logger.error("serviceProvider.insert() error", serviceProvider.toJSON(), e);
		}

	}

}
