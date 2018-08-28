package com.sdyk.ai.crawler.specific.jfh.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.witkey.Project;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.jfh.task.Task;
import com.sdyk.ai.crawler.util.LocationParser;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.chrome.ChromeTaskScheduler;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ScheduledChromeTask;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ProjectTask extends Task {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	public static List<String> crons = Arrays.asList("0 0 0/1 * * ? ", "0 0 0 1/1 * ? *");

	static {
		registerBuilder(
				ProjectTask.class,
				"https://www.jfh.com/jfportal/orders/jf{{project_id}}",
				ImmutableMap.of("project_id", String.class),
				ImmutableMap.of("project_id", "")
		);
	}

	public ProjectTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setValidator((a,t) -> {

			String src = getResponse().getText();
			if( src.contains("Log in JointForce") && src.contains("Don't have an account?") ){

				throw new AccountException.Failed(a.accounts.get("jfh.com"));

			}

		});

		this.setPriority(Priority.HIGH);

		this.setNoFetchImages();

		this.addDoneCallback((t) -> {

			String src = getResponse().getText();
			if( src.contains("错误了") || src.contains("登陆") ){
				return;
			}
			else{

				Document doc = getResponse().getDoc();

				crawler(doc, (ChromeTask)t);
			}

		});
	}

	public void crawler(Document doc, ChromeTask t) throws Exception {

		Project project = new Project(getUrl());

		project.origin_id = getUrl().split("orders/jf")[1];

		project.domain_id = Integer.valueOf(6);

		//标题
		project.title = doc.select("#wrap > div.orderexhead > div > div.orderextop_det > h3")
				.text().replace("关注","");

		//发布时间与截止时间
		try {
			SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy-MM-dd" );
			String pub = doc.select("#fabuTime").text();
			if( pub != null && pub.length() > 1 ){
				project.pubdate = sdf.parse(pub);
			}

			String due = doc.select("#wrap > div.left_wrap > div.lds_left_main > div.lds_list > ul > li:nth-child(3) > span").text();
			if( !due.contains("完成时间") ){
				due = doc.select("#wrap > div.left_wrap > div.lds_left_main > div.lds_list > ul > li:nth-child(4) > span").text();
			}
			if( due != null && due.length() > 0 ){
				project.due_time = DateFormatUtil.parseTime(due.replaceAll("预期完成时间：", ""));
			}
		} catch (ParseException e) {
			logger.error("error for String to Date", e);
		}

		if( project.due_time != null ){

			if( new Date().getTime() > project.due_time.getTime() ){

				project.status = "已截止";
			}
			else {

				project.status = "未截止";
			}
		}

		//价格
		String price = doc.select("#wrap > div.orderexhead > div > div.ordercstate_top_price > span.font18.b")
				.text().replace(",","");
		price = CrawlerAction.getNumbers(price);
		if( !"".equals(price) ){
			project.budget_ub = Double.valueOf(price);
			project.budget_lb = Double.valueOf(price);
		}

		Elements elements = doc.select("div.lds_list > ul > li");
		String tags = "";
		for(Element element : elements){
			String detail = element.text();

			//行业大类
			if( detail.contains("所属类目") ){
				project.category = detail.replace("所属类目：","").replace("-", ",");
			}
			//领业
			else if( detail.contains("领域") ){
				tags = tags + detail.replace("行业/领域：","").replace("/", ",") + ",";
			}
			//技能
			else if( detail.contains("技能") ) {
				tags = tags + detail.replace("技能要求：","").replace(" ", ",");
			}
		}
		if( tags.length() > 0 ){
			project.tags = new ArrayList<>();
			project.tags.addAll(Arrays.asList(tags.substring(0, tags.length()-1).split(",")));
		}

		//描述
		project.content = StringUtil.cleanContent(doc.select("div.lds_des_main").toString().replace("附件：", ""), new HashSet<>());

		// 地点
		String[] locations = doc.select("#wrap > div.left_wrap > div.lds_left_main > div.lds_list > ul > li:nth-child(4)")
				.text().split("，");
		if( locations != null && locations.length > 1 ){
			LocationParser parser = LocationParser.getInstance();
			project.location =
					parser.matchLocation(locations[locations.length - 1]).size() > 0 ?
							parser.matchLocation(locations[locations.length - 1]).get(0).toString() : null;
		}

		//以投标数
		String bisdNum = doc.select("#people_change").text();
		bisdNum = CrawlerAction.getNumbers(bisdNum);
		if( !"".equals(bisdNum) ){
			project.bids_num = Integer.valueOf(bisdNum);
		}

		try{
			if( project.title != null && project.title.length() > 1 ){

				if( project.category != null ){
					project.category.replace(" ", "");
				}
				project.insert();
			}
		} catch (Exception e) {
			logger.error("error for project.insert", e);
		}

	}

	public static void registerBuilder(Class<? extends ChromeTask> clazz, String url_template, Map<String, Class> init_map_class, Map<String, Object> init_map_defaults){
		ChromeTask.registerBuilder( clazz, url_template, init_map_class, init_map_defaults );
	}
}
