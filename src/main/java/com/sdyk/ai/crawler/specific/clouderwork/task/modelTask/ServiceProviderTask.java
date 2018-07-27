package com.sdyk.ai.crawler.specific.clouderwork.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.witkey.Resume;
import com.sdyk.ai.crawler.model.witkey.ServiceProvider;
import com.sdyk.ai.crawler.model.witkey.ServiceProviderRating;
import com.sdyk.ai.crawler.model.witkey.Work;
import com.sdyk.ai.crawler.specific.clouderwork.task.Task;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

public class ServiceProviderTask extends Task {

	public static long MIN_INTERVAL = 24 * 60 * 60 * 1000L;

	static {
		registerBuilder(
				ServiceProviderTask.class,
				"https://www.clouderwork.com/freelancers/{{servicer_id}}",
				ImmutableMap.of("servicer_id", String.class),
				ImmutableMap.of("servicer_id", "")
		);
	}

    public ServiceProviderTask(String url) throws MalformedURLException, URISyntaxException {

    	super(url);

    	this.setPriority(Priority.HIGH);

    	this.addDoneCallback((t) -> {

            Document doc = getResponse().getDoc();

            try {
                crawlerJob(doc);
            } catch (ChromeDriverException.IllegalStatusException e) {
                logger.info("error on crawlerJob",e);
            }

        });

    }

	/**
	 * 抓取服务商信息
	 * @param doc
	 * @throws ChromeDriverException.IllegalStatusException
	 */
	public void crawlerJob(Document doc) throws ChromeDriverException.IllegalStatusException {

		ServiceProvider serviceProvider = new ServiceProvider(getUrl());

		String url = getUrl();
		Pattern pattern = Pattern.compile("[0-9]*");
		String[] urls = url.split("freelancers/");

		serviceProvider.origin_id = urls[1];

		// 描述
		serviceProvider.content = StringUtil.cleanContent(doc.select("p.overview").toString(), new HashSet<>());

		// 平台认证信息
		serviceProvider.platform_certification = doc.select("img.icon-rz").attr("title");

		// 头像
		String imageUrl = doc.select("img.avator").attr("src");
		Map<String, String> url_filename = new HashMap<>();
		url_filename.put(imageUrl, "head_portrait");
		serviceProvider.head_portrait = BinaryDownloader.download(getUrl(), url_filename);

		//类型
		String type = serviceProvider.platform_certification;
		if( type!=null && !"".equals(type) ){

			if( type.contains("企业") ){
				type = "团队-公司";
			}
			//不含企业认证
			else {
				//获取团队标记
				type = doc.getElementsByClass("team-title").text();
				if( type != null &&
						!"".equals(type)){
					type = "团队";
				}
				//不含所属团队字样
				else {
					type = "个人";
				}
			}

		}
		//没有认证信息
		else {
			//获取团队标记
			type = doc.getElementsByClass("team-title").text();
			if( type != null &&
					!"".equals(type)){
				type = "团队";
			}
			//不含所属团队字样
			else {
				type = "个人";
			}
		}
		serviceProvider.type = type;

		//名字及公司名称
		String name = doc.getElementsByClass("name-box").text();
		if( name!=null && !"".equals(name) ){

			//
			if( serviceProvider.type.equals("团队-公司") ){
				if( !name.contains("公司") ){
					String teamName = doc.getElementsByClass("team-s-title").text();

					//团队名称中不含有公司
					if( !teamName.contains("公司") ){
						serviceProvider.company_name = teamName + "有限公司";
					}
					//团队名称中含有公司
					else {
						serviceProvider.company_name = teamName;
					}

				}
				//名字中含公司
				else {
					serviceProvider.company_name = name;
				}
			}

			//名字
			serviceProvider.name = name;

		}



		String all =doc.select("#profile > div > div > section.basic > section > div:nth-child(8)").text().replace("擅长领域：","");
		if( all!=null && !"".equals(all) && all.contains("擅长技能") ){
			String[] all1 = all.split("擅长技能：");
			String expertise = all1[0];

			//擅长领域
			if( expertise!=null && !"".equals(expertise) ){
				serviceProvider.category = expertise;
			}
			String[] all2 = all1[1].split("所在城市：");

			//擅长技能
			String skills;
			if(all2[0].contains("预估时薪")){
				String[] all2try = all2[0].split("预估时薪");
				skills = all2try[0];
				String price_per_day = CrawlerAction.getNumbers(all2try[1]);
				if( price_per_day != null ){
					serviceProvider.price_per_day = Integer.valueOf(price_per_day) * 8;
				}
			}
			//不含预估时薪
			else {
				skills = all2[0];
			}
			if( skills!=null && !"".equals(skills) ){
				serviceProvider.tags = skills;
			}
			String[] all3 = all2[1].split("工作经验：");

			//地点
			String location = all3[0];
			if( location!=null && !"".equals(location) ){
				serviceProvider.location = location;
			}

			//工作经验
			if( all3.length > 1 ){
				String workExperience = CrawlerAction.getNumbers(all3[1]);
				if( workExperience != null &&
						!"".equals(workExperience)) {
					serviceProvider.work_experience = Integer.valueOf(workExperience);
				}
			}

		}
		//当行数据出现收藏标志时
		else if( all!=null && !"".equals(all) && all.contains("收藏")){
			String allOther = doc.select("#profile > div > div > section.basic > section > div:nth-child(7)").text();
			String[] all1 = allOther.split("擅长技能：");
			String expertise = all1[0];

			//擅长领域
			if(expertise!=null&&!"".equals(expertise)){
				serviceProvider.category = expertise;
			}
			String[] all2 = all1[1].split("所在城市：");

			//擅长技能
			String skills;
			if(all2[0].contains("预估时薪")){
				String[] all2try = all2[0].split("预估时薪");
				skills = all2try[0];

				String price_per_day = CrawlerAction.getNumbers(all2try[1]);
				if( price_per_day != null ){
					serviceProvider.price_per_day = Integer.valueOf(price_per_day) * 8;
				}

			}
			//不含预估时薪
			else {
				skills = all2[0];
			}
			if( skills!=null && !"".equals(skills) ){
				serviceProvider.tags = skills;
			}
			String[] all3 = all2[1].split("工作经验：");

			//地点
			String location = all3[0];
			if(location!=null&&!"".equals(location)){
				serviceProvider.location = location;
			}

			//工作经验
			if( all3.length > 1 ){
				String workExperience = CrawlerAction.getNumbers(all3[1]);
				if( workExperience != null &&
						!"".equals(workExperience)) {
					serviceProvider.work_experience = Integer.valueOf(workExperience);
				}
			}
		}

		//成员数量
		String memberNum = CrawlerAction.getNumbers(doc.getElementsByClass("members").text());
		if( memberNum!=null && !"".equals(memberNum) ){
			serviceProvider.team_size = Integer.valueOf(memberNum);
		}
		//当为非团队时，成员数量为1
		else {
			serviceProvider.team_size = Integer.valueOf(1);
		}

		//平台项目数
		String projectNum = doc.select("#profile > div > div > section.left > div.evaluation > p.eva-desc > span:nth-child(1)").text();
		if( projectNum!=null && !"".equals(projectNum) && pattern.matcher(projectNum).matches()){
			serviceProvider.project_num = Integer.valueOf(projectNum);
		}

		//平台成功率
		String successRatio = doc.select("#profile > div > div > section.left > div.evaluation > p.eva-desc > span:nth-child(2)").text().replace("%","");
		if( successRatio!=null && !"".equals(successRatio) ){
			serviceProvider.success_ratio = Float.valueOf(successRatio);
		}

		//顾客评分
		String rating = doc.select("#profile > div > div > section.left > div.evaluation > div.pingjia > span")
				.text().replace("分","");
		if( rating!=null && !"".equals(rating) ){
			serviceProvider.rating = Float.valueOf(rating);
		}

		//评价数
		Elements elementRatings = doc.getElementsByClass("company");
		String sRatings = "0";
		String sRating = doc.getElementsByClass("only-sys").text();
		if( sRating != null &&
				!"".equals(sRating)){
			sRatings = CrawlerAction.getNumbers(sRating);
		}
		serviceProvider.rating_num = elementRatings.size() + Integer.valueOf(sRatings);

		//好评数
		serviceProvider.praise_num = Integer.valueOf(sRatings);

		//项目成功率
		String success = doc.select("#profile > div > div > section.left > div.evaluation > p.eva-desc > span:nth-child(2)")
				.text().replaceAll("%","");
		if( success!=null && !"".equals(success) ){
			serviceProvider.success_ratio = Float.valueOf(success);
		}

		//评价
		if( elementRatings != null && elementRatings.size() > 0 ){
			int i =0;
			for(Element element : elementRatings){

				i++;
				ServiceProviderRating serviceProviderRating = new ServiceProviderRating(getUrl()+"?num="+i);

				//服务商ID
				serviceProviderRating.service_provider_id =
						one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(getUrl()));

				//雇主名称
				serviceProviderRating.tenderer_name = element.getElementsByClass("comp-name").text();

				//评价内容
				serviceProviderRating.content = element.getElementsByClass("comp-desc").text();

				//项目名称
				serviceProviderRating.project_name = element.getElementsByClass("pro-main").text();

				//打分
				serviceProviderRating.rating = Double.valueOf(
						element.getElementsByClass("score-num").text().toCharArray()[0]);

				serviceProviderRating.insert();
			}
		}

		//教育经历
		String eduAll = doc.getElementsByClass("main-content edu").text();
		if( eduAll!=null && !"".equals(eduAll) ){

			Elements elements = doc.select("div.main-content.edu");
			for( Element element : elements ){

				String eduSchool = element.getElementsByClass("edu-school").text();
				Resume resume = new Resume(getUrl() + eduSchool);

				resume.user_id = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(getUrl()));
				String eduTime = element.getElementsByClass("edu-time").text().replace("年","");
				if( eduTime!=null && !"".equals(eduTime) ){
					String[] times = eduTime.split("-");
					try {
						SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy");
						Date startdate=simpleDateFormat.parse(times[0]);
						Date enddate=simpleDateFormat.parse(times[1]);
						resume.sd =startdate;
						resume.ed = enddate;
					} catch(ParseException px) {
						px.printStackTrace();
					}
				}

				// 学校
				if( eduSchool!=null && !"".equals(eduSchool) ){
					resume.org = eduSchool;
				}

				// 院系
				String eduCity = element.getElementsByClass("edu-city").text();
				if( eduCity!=null && !"".equals(eduCity) ){
					resume.dep = eduCity;
				}

				// 职位
				resume.degree_occupation = element.select("span.edu-degree").text();

				try {
					resume.insert();
				} catch (Exception e) {
					logger.error("error on insert resume", e);
				}
			}

		}

		// 项目经验
		Elements elements1 = doc.select("#profile > div > div > section.left > div.p-item > div:nth-child(2) > section");
		for( Element element : elements1 ){

			String sp_title = element.select("p.sp-title").text();

			Work work = new Work(getUrl() + sp_title);

			work.user_id = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(getUrl()));

			work.title = sp_title;

			work.content = element.select("div.sp-desc.desc").text();

			work.category = element.select("div.sp-cont > span:nth-child(1)").text();

			work.tags = element.select("span.skill").text();

			work.external_url = element.select("p.show-link > a").attr("href");

			try {
				work.insert();
			}
			catch (Exception e) {
				logger.error("error for work.insert();", e);
			}

		}

		// 工作经验
		if( doc.select("#profile > div > div > section.left > div.p-item > div:nth-child(4)").text().contains("工作") ){
			Elements elements2 = doc.select("#profile > div > div > section.left > div.p-item > div:nth-child(4) > div");
			for( Element element : elements2 ){

				String sp_title = element.select("p.sp-title").text();

				Resume resume = new Resume(getUrl() + sp_title);

				resume.user_id = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(getUrl()));

				resume.degree_occupation = element.select("div.sp-cont > span:nth-child(1)").text();
				resume.content = element.select("div.sp-desc.desc").text();

				String time = element.select("div.sp-cont > span:nth-child(3)").text();

				if( time != null && time.contains("-") ){
					String times[] = time.split("-");
					try {
						resume.sd = DateFormatUtil.parseTime(times[1]);

						if(times[1].contains("至今")){
							resume.is_current = 1;
						}
						else {
							resume.ed = DateFormatUtil.parseTime(times[1]);
						}
					} catch (ParseException e) {
						logger.error("error for String to date", e);
					}
				}

				resume.insert();

			}
		}

		// 含有附件
		Elements fileelements = doc.select("p.file");
		if( fileelements!=null && fileelements.size()>0 ){

			Map<String, String> fileMap = new HashMap<>();

			for(Element element : fileelements){

				String fileurl = element.select("a:nth-child(1)").attr("href");
				String fileName = element.select("a:nth-child(1)").text();
				fileMap.put(fileurl, fileName);
			}
			serviceProvider.attachment_ids = BinaryDownloader.download(getUrl(), fileMap);
		}

		try {
			serviceProvider.insert();
		} catch (Exception e) {
			logger.error("error on insert serviceProvider", e);
		}

		//抓取乙方项目
		Elements elements = doc.getElementsByClass("case-item");

		if( elements.size()>0 ){
			for(Element element : elements){

				String workUrl = element.attr("href");

				try {

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					init_map.put("work_id", workUrl);

					Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.clouderwork.task.modelTask.WorkTask");

					//生成holder
					ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

					//提交任务
					ChromeDriverDistributor.getInstance().submit(holder);

				} catch ( Exception e) {

					logger.error("error for submit WorkTask.class", e);
				}

			}
		}

	}

}
