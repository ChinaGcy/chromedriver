package com.sdyk.ai.crawler.specific.proginn.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.witkey.Resume;
import com.sdyk.ai.crawler.model.witkey.ServiceProvider;
import com.sdyk.ai.crawler.model.witkey.ServiceProviderRating;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.proginn.task.Task;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import com.sdyk.ai.crawler.util.LocationParser;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskFactory;
import one.rewind.io.requester.task.TaskHolder;
import one.rewind.io.requester.task.ScheduledChromeTask;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ServiceProviderTask extends Task {

	public static long MIN_INTERVAL = 24 * 60 * 60 * 1000L;

	public static List<String> crons = Arrays.asList("* * */1 * *", "* * */2 * *", "* * */4 * *", "* * */8 * *");

	static {
		registerBuilder(
				ServiceProviderTask.class,
				"https://www.proginn.com/wo/{{servicer_id}}",
				ImmutableMap.of("servicer_id", String.class),
				ImmutableMap.of("servicer_id", "")
		);
	}

	public ServiceProviderTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setPriority(Priority.HIGH);

		this.setNoFetchImages();

		// 检测异常
		this.setValidator((a,t) -> {

			String src = getResponse().getText();
			if( src.contains("手机登陆") && src.contains("忘记密码") ){

				throw new AccountException.Failed(a.accounts.get(t.getDomain()));
			}
		});

		// 页面解析
		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			crawlerJob(doc, (ChromeTask)t);

		});

	}

	public void crawlerJob(Document doc, ChromeTask t) throws Exception {

		ServiceProvider serviceProvider = new ServiceProvider(getUrl());

		//原网站ID
		serviceProvider.origin_id = getUrl().split("wo/")[1];

		//乙方名称
		serviceProvider.name = doc.select("a.header").text();

		// 头像
		String imageUrl = doc.select("body > div.main > div > div.four.wide.column.side-profile > div.avatar > a > img")
				.attr("src");
		Map<String, String> url_filename = new HashMap<>();
		url_filename.put(imageUrl, "head_portrait");
		List<String> headList = BinaryDownloader.download(getUrl(), url_filename);
		if( headList != null ){
			serviceProvider.head_portrait = headList.get(0);
		}

		//介绍
		String introduction = doc.select("div.introduction").text();

		serviceProvider.position = "";
		String[] introductions = introduction.split(" ");
		if( introductions.length > 2 ){

			serviceProvider.location = LocationParser.getInstance().matchLocation(introductions[0]).size() > 0 ?
					LocationParser.getInstance().matchLocation(introductions[0]).get(0).toString() : null;

			if( !introductions[1].contains("远程") ){
				serviceProvider.company_name = introductions[1];
			}
			for( int i = 2; i < introductions.length ; i++  ){
				serviceProvider.position = serviceProvider.position + introductions[i];
			}

		}
		//信息不全时
		else if( introductions.length ==2 ) {

			Elements icon1 = doc.select("div.introduction > i.arrow.location.icon");

			if( icon1 != null && icon1.size() > 0 ){
				serviceProvider.location = LocationParser.getInstance().matchLocation(introductions[0]).size() > 0 ?
						LocationParser.getInstance().matchLocation(introductions[0]).get(0).toString() : null;
			}
			else{
				serviceProvider.company_name = introductions[0];
			}

			serviceProvider.position = introductions[1];
		}

		//薪资
		String price = doc.select("p.work-price").text();
		price = CrawlerAction.getNumbers(price);
		if( !"".equals(price) ) {
			serviceProvider.price_per_day = Integer.valueOf(price);
		}

		//描述
		serviceProvider.content = StringUtil.cleanContent(
				doc.select("#J_WoMainContent > div:nth-child(1) > div").toString(), new HashSet<>());

		//点赞
		String zan = doc.select("#J_WoMainContent > div:nth-child(6) > a > em").text();
		zan = CrawlerAction.getNumbers(zan);
		if( !"".equals(zan) ){
			serviceProvider.like_num = Integer.valueOf(zan);
		}

		//浏览数
		String updateView = doc.select("div.ui.icon.compact.button").text();
		String[] views = updateView.split("浏览");
		if( views.length > 1 ){
			String view = CrawlerAction.getNumbers(views[1]);
			if( !"".equals(view) ){
				serviceProvider.view_num = Integer.valueOf(view);
			}
		}

		//更新时间
		try {
			serviceProvider.update_time = DateFormatUtil.parseTime(
					updateView.replace("更新于: ", "").split(" ")[0]);
		} catch (ParseException e) {
			logger.error("error for String to Date", e);
		}

		//关注
		String favNum = doc.select("body > div.main > div > div.four.wide.column.side-profile > div.stats > dl:nth-child(2) > dd > a").text();
		favNum = CrawlerAction.getNumbers(favNum);
		if( !"".equals(favNum) ){
			serviceProvider.like_num = Integer.valueOf(favNum);
		}

		//粉丝
		String fanNum = doc.select("body > div.main > div > div.four.wide.column.side-profile > div.stats > dl:nth-child(3) > dd > a").text();
		fanNum = CrawlerAction.getNumbers(fanNum);
		if( !"".equals(fanNum) ){
			serviceProvider.fan_num = Integer.valueOf(fanNum);
		}

		//小标签
		serviceProvider.tags = Arrays.asList(doc.select("div.skill-list").text(), " ");

		//成功率及评价数
		String ratioRating = doc.select("#proginn_wo_omment > h3 > div").text();
		String[] ratios = ratioRating.split("%");
		if( ratios.length>1 ){

			//成功率
			String ratio = CrawlerAction.getNumbers(ratios[0]);
			if( !"".equals(ratio) ){
				serviceProvider.success_ratio = Integer.valueOf(ratio);
			}

			//评论数
			String ratingNum = CrawlerAction.getNumbers(ratios[1]);
			if( !"".equals(ratingNum) ){
				serviceProvider.rating_num = Integer.valueOf(ratingNum);
			}
		}
		//缺少成功率或缺少评论数
		else {
			//只有评论
			if( ratioRating.contains("评论") ) {
				String ratingNum = CrawlerAction.getNumbers(ratioRating);
				if( !"".equals(ratingNum) ){
					serviceProvider.rating_num = Integer.valueOf(ratingNum);
				}
			}
			//只有成功率
			else if( ratioRating.contains("成功率") ) {
				String ratio = CrawlerAction.getNumbers(ratioRating);
				if( !"".equals(ratio) ){
					serviceProvider.success_ratio = Integer.valueOf(ratio);
				}
			}
		}

		//推荐
		Elements rcmd = doc.getElementsByClass("#proginn_wo_omment > h3 > div > div > div > ul > li:nth-child(1)")
				.select("i.icon");
		if( rcmd.size() != 0 ){
			serviceProvider.rcmd_num = Integer.valueOf(rcmd.size());
		}

		//准时率
		Elements speed = doc.select("#proginn_wo_omment > h3 > div > div > div > ul > li:nth-child(2)")
				.select("i.icon");
		if( speed.size() != 0 ){
			serviceProvider.service_speed = Integer.valueOf(speed.size());
		}

		//服务态度
		Elements attitude = doc.select("#proginn_wo_omment > h3 > div > div > div > ul > li:nth-child(3)")
				.select("i.icon");
		if( attitude.size() != 0 ){
			serviceProvider.service_attitude = Integer.valueOf(attitude.size());
		}

		//工作地点
		if( serviceProvider.location == null ){
			String workLocation = doc.select("body > div.main > div > div.four.wide.column.side-profile > div.hire-info > p:nth-child(2)")
					.text().replace("可工作地点: ", "").replace("远程", "");
			if( workLocation != null && workLocation.length() > 1){
				serviceProvider.location = LocationParser.getInstance().matchLocation(workLocation).size() > 0 ?
						LocationParser.getInstance().matchLocation(workLocation).get(0).toString() : null;
			}
		}

		//工作经验及教育经历
		Elements workException = doc.select("ul.J_Works > li");
		int i = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
		for(Element e : workException){

			i++;
			Resume resume = new Resume(getUrl() + "?resume=" + i);

			resume.user_id = getId();

			resume.is_current = 1;

			//时间
			String time = e.select("p.title > span:nth-child(1)").text();
			String[] times = time.split(" - ");
			if( times.length > 1 ){

				//开始时间
				try {
					resume.sd = sdf.parse(times[0].replaceAll(" ", ""));
				} catch (ParseException e1) {
					logger.error("error for String to Date", e1);
				}

				//结束时间
				if( !times[1].contains("至今") ){

					resume.is_current = 0;

					try {
						resume.ed = sdf.parse(times[1].replaceAll(" ", ""));
					} catch (ParseException e1) {
						logger.error("error for string to Date", e1);
					}
				}
			}

			//公司名、学校名
			resume.org = e.select("p.title > span:nth-child(2)").text();

			if( resume.org.contains("大学") ){

				//院系
				resume.dep = e.select("p.title > span:nth-child(3)").text();

				//学位
				resume.degree_occupation = e.select("p.title > span:nth-child(4)").text();
			}
			else {
				//职位
				resume.degree_occupation = e.select("p.title > span:nth-child(3)").text();
			}

			//描述
			resume.content =  "</p>" + e.getElementsByClass("summary").text() + "</p>";

			try{
				resume.insert();
			} catch (Exception e1) {
				logger.error("error for resume.insert()", e1);
			}

		}

		serviceProvider.domain_id = 9;

		//评论内容
		Elements ratingContent = doc.select("div.comment");
		int j = 0;
		for( Element element : ratingContent ){

			j++;
			ServiceProviderRating serviceProviderRating = new ServiceProviderRating(getUrl() + "?rating=" + j);

			serviceProviderRating.service_provider_id = serviceProvider.id;

			serviceProviderRating.tenderer_name = element.select("a.author").text();
			String tendererUrl = "https://www.proginn.com/"
					+ element.select("a.author").attr("href");

			serviceProviderRating.tenderer_id = one.rewind.txt.StringUtil.byteArrayToHex(
					one.rewind.txt.StringUtil.uuid(tendererUrl));

			serviceProviderRating.content = "<p>" + element.select("div.text").text() + "</p>";

			String ratingTime = element.select("div.date").text();
			try {
				serviceProviderRating.pubdate = DateFormatUtil.parseTime(ratingTime);
			} catch (ParseException e) {
				logger.error("error for String to Date", e);
			}

			Elements ratingList = element.select("i.star");
			if( ratingList.size() > 0 ){
				serviceProviderRating.rating = Integer.valueOf(ratingList.size());
			}

			// 添加甲方任务
			try {

				String tenderer_id = element.select("a.author").attr("href");

				String tUrl = "https://www.proginn.com/" + tenderer_id;
				long lastRunTime = 0;
				if( Distributor.URL_VISITS.keySet().contains(one.rewind.txt.StringUtil.MD5(tUrl)) ){
					lastRunTime = Distributor.URL_VISITS.get( one.rewind.txt.StringUtil.MD5(tUrl));
				}

				if( (new Date().getTime() - lastRunTime) > TendererTask.MIN_INTERVAL ){

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					init_map.put("tenderer_id", tenderer_id);

					Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.proginn.task.modelTask.TendererTask");

					//生成holder
					TaskHolder holder =  ChromeTaskFactory.getInstance().newHolder(clazz, init_map);

					//提交任务
					((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);
				}

			} catch ( Exception e) {

				logger.error("error for submit ProjectTask.class", e);
			}

			try{
				serviceProviderRating.insert();
			} catch (Exception e) {
				logger.error("error for serviceProviderRating.insert()", e);
			}

		}

		//项目
		Elements workList = doc.select("div.work-list > ul > li");
		for( Element element : workList ){

			String workUrl = element.select("a.media").attr("href");
			String like_num = element.select("a.plus_button > em").text();

			try {

				//设置参数
				Map<String, Object> init_map = new HashMap<>();
				init_map.put("work_id", workUrl);
				if( like_num != null ){
					init_map.put("like_num", like_num);
				}

				Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.proginn.task.modelTask.WorkTask");

				//生成holder
				TaskHolder holder =  ChromeTaskFactory.getInstance().newHolder(clazz, init_map);

				//提交任务
				((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

			} catch ( Exception e) {

				logger.error("error for submit ProjectTask.class", e);
			}
		}

		serviceProvider.project_num = workList.size();

		try{

			boolean status = false;

			if( serviceProvider.name != null && serviceProvider.name.length() > 1 ){

				if( serviceProvider.category != null ){
					serviceProvider.category.replace(" ", "");
				}
				status = serviceProvider.insert();
			}

			ScheduledChromeTask st = t.getScheduledChromeTask();

			// 第一次抓取生成定时任务
			if(st == null) {

				try {
					st = new ScheduledChromeTask(t.getHolder(), crons);
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
			logger.error("error for serviceProvider.insert()", e);
		}

	}
}