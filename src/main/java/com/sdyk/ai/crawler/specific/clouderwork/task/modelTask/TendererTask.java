package com.sdyk.ai.crawler.specific.clouderwork.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.witkey.TendererRating;
import com.sdyk.ai.crawler.specific.clouderwork.task.Task;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.model.witkey.Tenderer;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.ChromeTaskScheduler;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskHolder;
import one.rewind.io.requester.task.ScheduledChromeTask;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;

public class TendererTask extends Task {

	public static long MIN_INTERVAL = 24 * 60 * 60 * 1000;

	public static List<String> crons = Arrays.asList("0 0 0 1/1 * ? *");

	static {
		registerBuilder(
				TendererTask.class,
				"https://www.clouderwork.com{{tenderer_id}}",
				ImmutableMap.of("tenderer_id", String.class),
				ImmutableMap.of("tenderer_id", "")
		);
	}

    public TendererTask(String url) throws MalformedURLException, URISyntaxException {

        super(url);

        this.setPriority(Priority.HIGH);

	    this.setNoFetchImages();

        this.addDoneCallback((t)->{

            Document doc = getResponse().getDoc();

            //执行抓取任务
            try {
                crawlawJob(doc, (ChromeTask)t);
            } catch (MalformedURLException e) {
                logger.info("error on crawlawJob");
            } catch (URISyntaxException e) {
                logger.info("error on crawlawJob");
            }

        });

    }

	/**
	 * 抓取甲方数据
	 * @param doc
	 * @return
	 * @throws ChromeDriverException.IllegalStatusException
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	public void crawlawJob (Document doc, ChromeTask t) throws MalformedURLException, URISyntaxException {

		Tenderer tenderer = new Tenderer(getUrl());
		Pattern pattern = Pattern.compile("[0-9]*");

		//原网站ID
		String[] urlarray = getUrl().split("clients/");
		tenderer.origin_id = urlarray[1];

		//招标人名字
		String name = doc.select("#profile > div > div > section > section > div.prjectBox > p.name-p").text();
		if( name!=null && !"".equals(name) ){
			tenderer.name = name;
		}

		// 头像
		String imageUrl = doc.select("div.avatorBox > img").attr("src");
		Map<String, String> url_filename = new HashMap<>();
		url_filename.put(imageUrl, "head_portrait");
		tenderer.head_portrait = BinaryDownloader.download(getUrl(), url_filename);

		//招标人描述
		tenderer.content = "<p>" +
				doc.select("#profile > div > div > section > section > div.prjectBox > p.overview-p").text() +
				"</p>";

		//线上消费金额
		String totalSpendingt =doc.select("#profile > div > div > section > section > div.prjectBox > section > span:nth-child(1)").text().replaceAll("￥","");
		if( totalSpendingt!= null &&!"".equals(totalSpendingt) ){
			if( totalSpendingt.contains("万") ){
				totalSpendingt = totalSpendingt.replace("万","").replace(",","");
				tenderer.total_spending = Double.valueOf(totalSpendingt)*10000;
			}
			//单位为元
			else {
				tenderer.total_spending = Double.valueOf(totalSpendingt);
			}
		}

		//等级
		String gradeSrc = doc.select("#profile > div > div > section > section > div.avatorBox > span > img").attr("src");
		String grade = "0";
		if( gradeSrc!=null && !"".equals(gradeSrc) ){
			grade = gradeSrc.split("icon-lv")[1].split(".png")[0];
		}
		tenderer.grade = grade;

		//交易次数
		String trade = doc.select("#profile > div > div > div.evaluation > p.eva-desc > span:nth-child(1)").text();
		if( trade!=null && !"".equals(trade) ){
			tenderer.trade_num = Integer.valueOf(trade);
		}

		//雇佣人数
		String totalHires = doc.select("#profile > div > div > section > section > div.prjectBox > section > span:nth-child(3)").text();
		totalHires = CrawlerAction.getNumbers(totalHires);
		if( totalHires!=null && !"".equals(totalHires) && pattern.matcher(totalHires).matches() ){
			tenderer.total_employees = Integer.valueOf(totalHires);
		}

		//项目数
		String proUnm = doc.select("#profile > div > div > section > section > div.prjectBox > section > span:nth-child(2)").text();
		proUnm = CrawlerAction.getNumbers(proUnm);
		if( proUnm!=null && !"".equals(proUnm) ){
			tenderer.total_project_num = Integer.valueOf(proUnm);
		}

		//项目成功率
		String successRatio = doc.select("#profile > div > div > div.evaluation > p.eva-desc > span:nth-child(2)").text();
		successRatio = CrawlerAction.getNumbers(successRatio);
		if( successRatio != null && !"".equals(successRatio) ) {
			tenderer.success_ratio = Integer.valueOf(successRatio);
		}

		//评论
		org.jsoup.select.Elements elements = doc.getElementsByClass("company");
		if( elements!=null && elements.size()>0 ){

			for(Element element : elements){

				TendererRating tendererRating = new TendererRating(getUrl());

				//雇主URL
				tendererRating.user_id =
						one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(getUrl()));

				//服务商名称
				tendererRating.service_provider_name = element.getElementsByClass("comp-name").text();

				//评价内容
				tendererRating.content = element.getElementsByClass("comp-desc").text();

				//项目名称
				tendererRating.project_name = doc.getElementsByClass("pro-main").text();

				//打分
				String happy_num = element.getElementsByClass("score-num").text()
						.replace(".","").replace("0","");
				String happy = CrawlerAction.getNumbers(happy_num);
				tendererRating.rating = Integer.valueOf(happy);
				tendererRating.insert();
			}
		}

		//评价数
		String sRating = doc.getElementsByClass("only-sys").text();
		String sRatings = "0";
		if( sRating != null && !"".equals(sRating) ){
			sRatings = CrawlerAction.getNumbers(sRating);
		}
		tenderer.rating_num = Integer.valueOf(sRatings) + elements.size();

		//好评数
		tenderer.praise_num = Integer.valueOf(sRatings);

		//获取项目连接
		Elements jobs = doc.select("div.job-list > a");
		for(  Element element : jobs ){

			String jobId = element.attr("href").replace("/jobs/","");

			try {

				//设置参数
				Map<String, Object> init_map = new HashMap<>();
				init_map.put("project_id", jobId);

				//生成holder
				ChromeTaskHolder holder = ChromeTask.buildHolder(ProjectTask.class, init_map);

				//提交任务
				((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

			} catch ( Exception e) {

				logger.error("error for submit ProjectTask.class", e);
			}

		}

		tenderer.insert();

		// 注册定时任务
		if( !ChromeTaskScheduler.getInstance().registered(t._scheduledTaskId) ){
			try {
				ScheduledChromeTask scheduledTask = new ScheduledChromeTask(
						t.getHolder(this.getClass(), this.init_map),
						crons
				);
				ChromeTaskScheduler.getInstance().schedule(scheduledTask);
			} catch (Exception e) {
				logger.error("eror for creat ScheduledChromeTask", e);
			}
		}

	}

}
