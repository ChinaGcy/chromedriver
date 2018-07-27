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
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.*;

public class ServiceProviderTask extends Task {

	public static long MIN_INTERVAL = 24 * 60 * 60 * 1000L;

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

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			crawlerJob(doc);

		});

	}

	public void crawlerJob(Document doc) {

		ServiceProvider serviceProvider = new ServiceProvider(getUrl());

		List<Task> task = new ArrayList<>();

		//原网站ID
		serviceProvider.origin_id = getUrl().split("wo/")[1];

		//乙方名称
		serviceProvider.name = doc.select("a.header").text();

		// 头像
		String imageUrl = doc.select("body > div.main > div > div.four.wide.column.side-profile > div.avatar > a > img")
				.attr("src");
		Map<String, String> url_filename = new HashMap<>();
		url_filename.put(imageUrl, "head_portrait");
		serviceProvider.head_portrait = BinaryDownloader.download(getUrl(), url_filename);

		//介绍
		String introduction = doc.select("div.introduction").text();

		String[] introductions = introduction.split(" ");
		if( introductions.length == 3 ){

			serviceProvider.location = introductions[0];

			if( !introductions[1].contains("远程") ){
				serviceProvider.company_name = introductions[1];
			}

			serviceProvider.position = introductions[2];
		}
		//信息不全时
		else if( introduction.length() ==2 ) {

			if( introductions[0].contains("公司") ){
				serviceProvider.company_name = introductions[0];
			}
			//不是公司信息
			else {
				serviceProvider.location = introductions[0];
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
		String zan = doc.select("#J_WoMainContent > div:nth-child(8) > a > em").text();
		zan = CrawlerAction.getNumbers(zan);
		if( !"".equals(zan) ){
			serviceProvider.like_num = Integer.valueOf(zan);
		}

		//浏览数
		String updateView = doc.select("#J_WoMainContent > div:nth-child(8) > div").text();
		String[] views = updateView.split("浏览");
		if( views.length > 1 ){
			String view = CrawlerAction.getNumbers(views[1]);
			if( !"".equals(view) ){
				serviceProvider.view_num = Integer.valueOf(view);
			}
		}

		//更新时间
		try {
			serviceProvider.update_time = DateFormatUtil.parseTime(updateView);
		} catch (ParseException e) {
			logger.error("error for String to Date", e);
		}

		//关注
		String favNum = doc.select("body > div.main > div > div.four.wide.column.side-profile > div.stats > dl:nth-child(2) > dd > a").text();
		favNum = CrawlerAction.getNumbers(favNum);
		if( !"".equals(favNum) ){
			serviceProvider.fav_num = Integer.valueOf(favNum);
		}

		//粉丝
		String fanNum = doc.select("body > div.main > div > div.four.wide.column.side-profile > div.stats > dl:nth-child(3) > dd > a").text();
		fanNum = CrawlerAction.getNumbers(fanNum);
		if( !"".equals(fanNum) ){
			serviceProvider.fan_num = Integer.valueOf(fanNum);
		}

		//小标签
		serviceProvider.tags = doc.select("div.skill-list").text().replace(" ", ",");

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
				serviceProvider.location = workLocation;
			}
		}

		//工作经验及教育经历
		Elements workException = doc.select("ul.J_Works > li");
		int i = 0;
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
					resume.sd = DateFormatUtil.parseTime(times[0]);
				} catch (ParseException e1) {
					logger.error("error for String to Date", e1);
				}

				//结束时间
				if( !times[1].contains("至今") ){

					resume.is_current = 0;

					try {
						resume.ed = DateFormatUtil.parseTime(times[1]);
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
			resume.content = e.getElementsByClass("summary").text();

			try{
				resume.insert();
			} catch (Exception e1) {
				logger.error("error for resume.insert()", e1);
			}

		}

		//评论内容
		Elements ratingContent = doc.select("div.comment");
		int j = 0;
		for( Element element : ratingContent ){

			j++;
			ServiceProviderRating serviceProviderRating = new ServiceProviderRating(getUrl() + "?rating=" + j);

			serviceProviderRating.tenderer_name = element.select("a.author").text();
			String tendererUrl = "https://www.proginn.com/"
					+ element.select("a.author").attr("href");

			serviceProviderRating.tenderer_id = one.rewind.txt.StringUtil.byteArrayToHex(
					one.rewind.txt.StringUtil.uuid(tendererUrl));

			serviceProviderRating.content = element.select("div.text").text();

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

			//甲方任务添加
			/*try {
				task.add(new TendererTask(tendererUrl));
			} catch (MalformedURLException e) {
				logger.error("error for task.add", e);
			} catch (URISyntaxException e) {
				logger.error("error for task.add", e);
			}*/

			try{
				serviceProviderRating.insert();
			} catch (Exception e) {
				logger.error("error for serviceProviderRating.insert()", e);
			}

		}

		//项目
		Elements workList = doc.select("div.work-list > ul > li");
		for( Element element : workList ){

			String workUrl = "https://www.proginn.com"
					+ element.select("a.media").attr("href");

			try {

				//设置参数
				Map<String, Object> init_map = new HashMap<>();
				init_map.put("work_id", workUrl);
				init_map.put("uId", getId());

				Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.proginn.task.modelTask.WorkTask");

				//生成holder
				ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

				//提交任务
				((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

			} catch ( Exception e) {

				logger.error("error for submit ProjectTask.class", e);
			}

		}

		try{
			serviceProvider.insert();
		} catch (Exception e) {
			logger.error("error for serviceProvider.insert()", e);
		}


	}
}