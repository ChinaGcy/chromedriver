package com.sdyk.ai.crawler.specific.mihuashi.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.witkey.Tenderer;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.mihuashi.action.LoadMoreContentAction;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TendererTask extends com.sdyk.ai.crawler.task.Task{

	public long MIN_INTERVAL = 60 * 60 * 1000;

	static {
		registerBuilder(
				TendererTask.class,
				"https://www.mihuashi.com/users/{{tenderer_id}}?role=employer",
				ImmutableMap.of("tenderer_id", String.class),
				ImmutableMap.of("tenderer_id", "")
		);
	}

    public Tenderer tenderer;

	public String moreProjectPath = "#artworks > div > section > div:nth-child(2) > a";

    public TendererTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

        super(url);

        this.setPriority(Priority.MEDIUM);

	    this.addAction(new LoadMoreContentAction(moreProjectPath));

        this.addDoneCallback((t)->{

            Document doc = getResponse().getDoc();

            //执行抓取任务
	        crawlawJob(doc);
        });
    }

	public void crawlawJob(Document doc){

		tenderer = new Tenderer(getUrl());

		Pattern pattern = Pattern.compile("/users/(?<username>.+?)\\?role=employer");
		Matcher matcher = pattern.matcher(getUrl());

		while(matcher.find()) {
			try {
				String web = URLDecoder.decode(matcher.group("username"), "UTF-8")+"?role=employer";
				tenderer.origin_id = URLDecoder.decode(matcher.group("username"), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		//公司名称
		String companyName = doc.select("#users-show > div.container-fluid > div.profile__container > aside > section.profile__avatar-wrapper > h5 > span").text();
		if( companyName!=null && !"".equals(companyName) ){

			if( companyName.contains("公司") || companyName.contains("工作室") ){
				tenderer.company_name = companyName;
				//名称
				tenderer.name = companyName;
				tenderer.tender_type = "团体-公司";
			}
			//不是公司
			else{
				tenderer.name = companyName;
				tenderer.tender_type = "个人";
			}

		}
		//不存在公司标签
		else {
			tenderer.name = doc.select("#users-show > div.container-fluid > div.profile__container > aside > section.profile__avatar-wrapper > h5").text();
			tenderer.tender_type = "个人";
		}


		//评价数
		String ratingNum = doc.select("#users-show > div.container-fluid > div.profile__container > main > header > ul > li.active > a > span").text();
		ratingNum = CrawlerAction.getNumbers(ratingNum);
		if(ratingNum!=null&&!"".equals(ratingNum)){
			tenderer.rating_num = Integer.valueOf(ratingNum);
		}

		//简介
		tenderer.content = doc.getElementsByClass("profile__summary-wrapper").html();

		//平台项目数
		String projecrs = doc.select("#users-show > div.container-fluid > div.profile__container > main > header > ul > li.active > a > span")
				.text();
		if( projecrs!=null && !"".equals(projecrs) ) {
			tenderer.total_project_num = Integer.valueOf(projecrs);
			tenderer.trade_num = tenderer.total_project_num;
		}

		Elements elements =doc.getElementsByClass("project-cell__title-link");
		for(Element element : elements){
			String project_id = element.attr("href").replace("/projects/","");

			try {

				//设置参数
				Map<String, Object> init_map1 = new HashMap<>();
				init_map1.put("project_id", project_id);

				//生成holder
				ChromeTaskHolder holder = ChromeTask.buildHolder(ProjectTask.class, init_map1);

				//提交任务
				ChromeDriverDistributor.getInstance().submit(holder);


			} catch (Exception e) {
				logger.error("error for submit TendererRatingTask", e);
			}

		}

		if( tenderer.tender_type==null || !tenderer.tender_type.equals("团体-公司") ) {
			tenderer.tender_type = "个人";
		}

		//企划完成率
		String successRatio = doc.select("#users-show > div.container-fluid > div.profile__container > aside > section.profile__avatar-wrapper > section.credit > div:nth-child(7) > span.percent")
				.text().replace("%","");
		successRatio = CrawlerAction.getNumbers(successRatio);
		if( successRatio != null && !"".equals(successRatio) ){
			tenderer.success_ratio = Integer.valueOf(successRatio);
		}

		//企划选定率
		String selectionRatio = doc.select("#users-show > div.container-fluid > div.profile__container > aside > section.profile__avatar-wrapper > section.credit > div:nth-child(8) > span.percent")
				.text().replace("%","");
		selectionRatio = CrawlerAction.getNumbers(selectionRatio);
		if( selectionRatio != null && !"".equals(selectionRatio) ){
			tenderer.selection_ratio = Integer.valueOf(selectionRatio);
		}

		//头像信息获取
		Set<String> fileUrl =new HashSet<>();
		List<String> fileName = new ArrayList<>();
		String image = doc.select("#profile__avatar > a > img").toString();
		String imageUrl = doc.select("img.profile__avatar-image").attr("src");
		fileUrl.add(imageUrl);

		//头像ID填写
		tenderer.head_portrait = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(imageUrl));

		//头像下载
		BinaryDownloader.download(image,fileUrl,getUrl(),fileName);

		tenderer.insert();
	}

	public static void registerBuilder(Class<? extends ChromeTask> clazz, String url_template, Map<String, Class> init_map_class, Map<String, Object> init_map_defaults){
		ChromeTask.registerBuilder( clazz, url_template, init_map_class, init_map_defaults );
	}

}
