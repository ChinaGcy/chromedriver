package com.sdyk.ai.crawler.specific.mihuashi.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.model.witkey.ServiceProvider;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.mihuashi.task.Task;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;

import one.rewind.io.requester.task.ScheduledChromeTask;
import org.jsoup.nodes.Document;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceProviderTask extends Task {

	public static long MIN_INTERVAL = 12 * 60 * 60 * 1000L;

	public static List<String> crons = Arrays.asList("* * */1 * *", "* * */2 * *", "* * */4 * *", "* * */8 * *");

	static {
		registerBuilder(
				ServiceProviderTask.class,
				"https://www.mihuashi.com/users/{{service_id}}?role=painter",
				ImmutableMap.of("service_id", String.class),
				ImmutableMap.of("service_id", "")
		);
	}

	public ServiceProviderTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setPriority(Priority.HIGH);

		this.setNoFetchImages();

		this.setValidator((a,t) -> {

			String src = getResponse().getText();
			if( src.contains("邮箱登陆") && src.contains("注册新账号") ){

				throw new AccountException.Failed(a.accounts.get("mihuashi.com"));
			}
		});

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();
			String src = getResponse().getText();

			if( src.contains("迷路啦") || src.contains("非常抱歉") ) {
				return;
			}
			//页面正常
			else{
				try {

					ChromeTask t_ = (ChromeTask) t;
					crawlerJob(doc, t_);
				} catch (ChromeDriverException.IllegalStatusException e) {
					logger.info("error on crawlerJob",e);
				}
			}

		});
	}

	public void crawlerJob(Document doc, ChromeTask task) throws ChromeDriverException.IllegalStatusException {

		ServiceProvider serviceProvider = new ServiceProvider(getUrl());

		String[] url = getUrl().split("users");

		try {
			serviceProvider.origin_id = URLDecoder.decode(url[1], "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("error for 字符集转换", e);
		}

		//名字
		String name = doc.select("#users-show > div.container-fluid > div.profile__container > aside > section.profile__avatar-wrapper > h5").text();
		String renzhang = doc.select("#users-show > div.container-fluid > div.profile__container > aside > section.profile__avatar-wrapper > h5 > span").text();
		serviceProvider.name = name.replace(renzhang,"");

		serviceProvider.domain_id = 4;

		// 平台认证
		//serviceProvider.platform_certification = doc.select("span.enterprise").text();

		//介绍
		Set<String> set = new HashSet<>();
		serviceProvider.content = StringUtil.cleanContent( doc.select("div.summary").html(), set);
		if( set.size() > 0 ){
			serviceProvider.content = BinaryDownloader.download(serviceProvider.content, set, getUrl());
		}

		//评论数
		String ratNum = doc.select("#users-show > div.container-fluid > div.profile__container > aside > section.profile__avatar-wrapper > section.credit > p")
				.text().replace("共","").replace("条评价","");
		ratNum = CrawlerAction.getNumbers(ratNum);
		if(ratNum!=null&&!"".equals(ratNum)){
			serviceProvider.rating_num = Integer.valueOf(ratNum);
		}

		// 标签
		/*serviceProvider.tags = doc.select("#users-show > div.container-fluid > div.profile__container > aside > section.profile__skill-wrapper").text()
				.replace("擅长画风 ", "")
				.replace("擅长类型 ", "")
				.replace(" ", ",");*/

		//项目数
		String projectNum = doc.select("#users-show > div.container-fluid > div.profile__container > main > header > ul > li.active > a > span").text();
		if( projectNum != null && !"".equals(projectNum) ){
			serviceProvider.project_num = Integer.valueOf(projectNum);
		}

		//服务质量
		String serviceQuality = doc.select("#credit > div:nth-child(1) > section > section > div:nth-child(1) > span.percent.hidden-md.visible-lg-inline-block").text().replace("%","");
		if( serviceQuality!=null && !"".equals(serviceQuality) ){
			serviceProvider.service_quality = Double.valueOf(serviceQuality) / 20;
		}

		//服务速度
		String serviceSpeed = doc.select("#credit > div:nth-child(1) > section > section > div:nth-child(2) > span.percent.hidden-md.visible-lg-inline-block").text().replace("%","");
		if( serviceSpeed!=null && !"".equals(serviceSpeed) ){
			serviceProvider.service_speed = Double.valueOf(serviceSpeed) /20;
		}

		//服务态度
		String serviceAttitude = doc.select("#credit > div:nth-child(1) > section > section > div:nth-child(3) > span.percent.hidden-md.visible-lg-inline-block").text().replace("%","");
		if( serviceAttitude!=null && !"".equals(serviceAttitude)){
			serviceProvider.service_attitude = Double.valueOf(serviceAttitude) / 20;
		}

		// 头像
		String imageUrl = doc.select("img.profile__avatar-image").attr("src");
		Map<String, String> url_filename = new HashMap<>();
		url_filename.put(imageUrl, "head_portrait");
		serviceProvider.head_portrait = BinaryDownloader.download(getUrl(), url_filename);

		//作品图像
		String allImags = doc.select("div.masonry").toString();
		Pattern pattern = Pattern.compile("https://images.mihuashi.com/(?<imageSrc>.+?)\">");
		Matcher matcher = pattern.matcher(allImags);
		Map<String, String> url_name = new HashMap<>();

		//设置图片链接
		while(matcher.find()) {
			url_name.put("https://images.mihuashi.com/" + matcher.group("imageSrc"), null);
		}

		//下载图片
		//serviceProvider.cover_images = BinaryDownloader.download(getUrl(), url_name);

		serviceProvider.type = "个人";

		if( serviceProvider.name.contains("工作室") ){
			serviceProvider.type = "团队";
		}
		else if(  serviceProvider.name.contains("公司") ){
			serviceProvider.type = "团队-公司";
		}
		serviceProvider.category.replace("", "");

		boolean status = serviceProvider.insert();

		// 公司信息补全任务
		if( serviceProvider.type.contains("公司") ){

			try {

				//设置参数
				Map<String, Object> init_map = new HashMap<>();
				init_map.put("company_name", serviceProvider.name);

				Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.company.CompanyInformationTask");

				//生成holder
				//ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

				//提交任务
				((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

			} catch (Exception e){
				logger.error("error for create CompanyInformationTask", e);
			}
		}

		ScheduledChromeTask st = task.getScheduledChromeTask();

		// 第一次抓取生成定时任务
		if(st == null) {

			try {
				//st = new ScheduledChromeTask(task.getHolder(this.init_map), crons);
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

	}
}