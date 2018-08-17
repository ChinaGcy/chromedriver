package com.sdyk.ai.crawler.specific.mihuashi.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.model.witkey.Project;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.mihuashi.task.Task;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.ChromeTaskScheduler;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import one.rewind.io.requester.task.ScheduledChromeTask;
import one.rewind.txt.DateFormatUtil;
import one.rewind.util.FileUtil;
import org.jsoup.nodes.Document;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 处理需求页面 解析 project
 * 示例URL:
 */
public class ProjectTask extends Task {

	public static long MIN_INTERVAL = 12 * 60 * 60 * 1000;

	public static List<String> crons = Arrays.asList("* * */1 * *");

	static {
		registerBuilder(
				ProjectTask.class,
				"https://www.mihuashi.com/projects/{{project_id}}/",
				ImmutableMap.of("project_id", String.class, "flage", String.class),
				ImmutableMap.of("project_id", "", "flage", "1")
		);
	}

	public ProjectTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		// 设置优先级
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

			//页面错误
			if ( src.contains("非常抱歉") || src.contains("权限不足") ) {

				return;
			}

			// 下载页面
			FileUtil.writeBytesToFile(src.getBytes(), "project.html");

			// 抓取页面
			Project project = new Project(getUrl());

			project.domain_id = 4;

			String authorUrl = null;

			//项目名
			String title = doc.select("#project-name").text();
			String renzheng = doc.select("#project-name > span").text();
			project.title = title.replace(renzheng,"");

			//原网站ID
			project.origin_id = getUrl().split("projects/")[1];

			//抓取发布时间
			String time = doc.select("#projects-show > div.container-fluid > div.project__main-section > div.project__info-section > section > section > div.pull-left > p")
					.text().replace("企划发布于","").replace("\"","");
			String pub = doc.select("#projects-show > div.container-fluid > div.project__main-section > div.project__info-section > section > section > div.pull-left > p > span").text();
			String pubTime = time.replace(pub,"");
			DateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");

			//设置发布时间
			try {
				project.pubdate = format1.parse(pubTime);
			} catch (ParseException e) {
				logger.error("error on String to data");
			}

			//类型
			project.category = pub.replace("·", ",");

			//预算
			String budget = doc.select("#aside-rail > div > aside > p:nth-child(4)").text().replace("￥","");
			if(budget!=null&&!"".equals(budget)){
				if(budget.contains("~")){
					String[] budgets = budget.split("~");
					String budget_lb = CrawlerAction.getNumbers(budgets[0]);
					String budget_uo = CrawlerAction.getNumbers(budgets[1]);
					project.budget_lb = Integer.valueOf(budget_lb);
					project.budget_ub = Integer.valueOf(budget_uo);
				}else{
					project.budget_ub=project.budget_lb=Integer.valueOf(budget);
				}
			}

			//截止时间
			String remainingTime = doc.select("#aside-rail > div > aside > p:nth-child(2)").text();
			try {
				project.due_time = DateFormatUtil.parseTime(remainingTime);
			} catch (ParseException e) {
				logger.error("error on String to date",e);
			}

			// 项目状态
			if( new Date().getTime() > project.due_time.getTime() ){

				project.status = "已截稿";
			}
			else {

				project.status = "未截稿";
			}

			//描述
			project.content = StringUtil.cleanContent(doc.select("#project-detail").html(), new HashSet<>());

			//资金分配
			project.delivery_steps = doc.select("#projects-show > div.container-fluid > div.project__main-section > div.project__info-section > section > div.deposit-phases__wrapper").text();

			//采集时刻以投标数
			String bidderNewNum = doc.select("#projects-show > div.container-fluid > div.project__main-section > div.project__application-section > section > h5 > div > span.applications-count")
					.text().replace("本企划共应征画师","").replace("名","");
			String num = CrawlerAction.getNumbers(bidderNewNum);
			if(num!=null&&!"".equals(num)){
				project.bids_num= Integer.valueOf(num);
			}

			//投标人姓名
			project.tenderer_name = doc.select("#projects-show > div.container-fluid > div.project__main-section > div.project__sidebar-container > aside > section > h5 > span").text();

			//投标人ID
			String tendererId = doc.select("#profile__avatar > a").attr("href").toString();
			Pattern pattern = Pattern.compile("/users/(?<username>.+?)\\?role=employer");
			Matcher matcher = pattern.matcher(tendererId);

			//抓取 tenderer_id , 并转换字符集
			String tenderer_id = null;
			while(matcher.find()) {
				try {
					tenderer_id = URLDecoder.decode(matcher.group("username"), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}

			//抓取 tenderer_name
			if( project.tenderer_name == null || "".equals(project.tenderer_name) ){
				project.tenderer_name = tenderer_id;
			}

			//采集招标人信息
			if(tenderer_id != null
					&& ! "".equals(tenderer_id)
					)
			{

				authorUrl = "https://www.mihuashi.com/users/" + tenderer_id + "?role=employer";
				project.tenderer_id = one.rewind.txt.StringUtil.byteArrayToHex(
						one.rewind.txt.StringUtil.uuid(authorUrl));

				String flage = (String) init_map.get("flage");

				if( flage.equals("1") ){

					//添加甲方任务
					try {

						//设置参数
						Map<String, Object> init_map = new HashMap<>();
						init_map.put("tenderer_id", tenderer_id);

						Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.TendererTask");

						//生成holder
						ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

						//提交任务
						((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);


					} catch (Exception e) {
						logger.error("error for submit TendererTask", e);
					}

					//添加甲方评论任务
					try {

						//设置参数
						Map<String, Object> init_map1 = new HashMap<>();
						init_map1.put("tenderer_id", tenderer_id);

						Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.TendererRatingTask");

						//生成holder
						ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map1);

						//提交任务
						ChromeDriverDistributor.getInstance().submit(holder);


					} catch (Exception e) {
						logger.error("error for submit TendererRatingTask", e);
					}
				}

			}

			try {
				if( project.title != null && project.title.length() > 1 ){

					project.category.replace(" ", "");
					project.insert();
				}
			} catch (Exception e) {
				logger.error("error on insert project", e);
			}

			ScheduledChromeTask st = t.getScheduledChromeTask();

			// 第一次抓取生成定时任务
			if(st == null) {

				st = new ScheduledChromeTask(t.getHolder(this.init_map), crons);
				st.start();
			}
			// 已完成项目停止定时任务
			if( project.status.contains("已截稿") ){
				st.stop();
			}

		});
	}


	public static void registerBuilder(Class<? extends ChromeTask> clazz, String url_template, Map<String, Class> init_map_class, Map<String, Object> init_map_defaults){
		ChromeTask.registerBuilder( clazz, url_template, init_map_class, init_map_defaults );
	}

}
