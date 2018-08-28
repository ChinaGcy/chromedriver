package com.sdyk.ai.crawler.specific.jfh.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.model.witkey.ServiceProvider;
import com.sdyk.ai.crawler.model.witkey.ServiceProviderRating;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.jfh.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskFactory;
import one.rewind.io.requester.task.TaskHolder;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.*;

public class ServiceProviderRatingTask extends Task {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				ServiceProviderRatingTask.class,
				"http://shop.jfh.com/{{user_id}}",
				ImmutableMap.of("user_id", String.class),
				ImmutableMap.of("user_id", "")
		);
	}

	public ServiceProviderRatingTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setValidator((a,t) -> {

			String src = getResponse().getText();
			if( src.contains("Log in JointForce") && src.contains("Don't have an account?") ){

				throw new AccountException.Failed(a.accounts.get("jfh.com"));

			}

		});

		this.setPriority(Priority.MEDIUM);

		this.setNoFetchImages();

		this.addDoneCallback((t) -> {

			String src = getResponse().getText();

			Document doc = getResponse().getDoc();

			//页面发生错误
			if( src.contains("关闭中") || src.contains("稍后再试") || src.contains("404") ){
				return;
			}
			//页面正确
			else {
				crawler(doc);
			}

		});

	}

	public void crawler(Document doc){

		System.out.println("执行解放号乙方评论任务");

		String uId = one.rewind.txt.StringUtil.byteArrayToHex(
				one.rewind.txt.StringUtil.uuid(getUrl() + "/bu"));

		Elements ratingElement = doc.select("div.companyInfomation");

		ServiceProvider serviceProvider = ServiceProvider.selectById(uId);

		int ratings = ratingElement.size();
		int good_ratings = 0;

		for(Element rating : ratingElement){

			ServiceProviderRating serviceProviderRating = new ServiceProviderRating(getUrl() + "?rating=" + "i");

			serviceProviderRating.service_provider_id = uId;

			//项目名称
			serviceProviderRating.project_name = rating.select("div.companyInfomationCenter > a").text();

			//价格
			String price = CrawlerAction.getNumbers(
					rating.select("div.companyInfomationCenter > span").text().replaceAll(",", ""));

			if( !"".equals(price) ){
				serviceProviderRating.price = Double.valueOf(price);
			}

			if( rating.select("p.population").contains("好评") ){
				good_ratings = good_ratings + 1;
			}

			//内容
			serviceProviderRating.content = com.sdyk.ai.crawler.util.StringUtil.cleanContent(
					rating.select("div.companyInfomationCenter > p:nth-child(4) > span:nth-child(2)").toString(),
					new HashSet<>());

			//时间
			String time = rating.select("div.companyInfomationLeft > p:nth-child(2)").text();
			try {
				serviceProviderRating.pubdate = DateFormatUtil.parseTime(time);
			} catch (ParseException e) {
				logger.error("error for String to Date", e);
			}

			//甲方
			serviceProviderRating.tenderer_name = rating.select("div.companyInfomationLeft > p:nth-child(1)").text();

			try{
				serviceProviderRating.insert();
			} catch (Exception e) {
				logger.error("error for serviceProviderRating.insert", e);
			}

		}

		serviceProvider.rating_num = ratings;
		serviceProvider.praise_num = good_ratings;
		serviceProvider.update();

		//服务
		Elements cases = doc.select("#productServerContainer > a");
		for( Element element : cases ){

			String url = element.attr("href");
			url = url.replace("http://shop.jfh.com/","");

			try {

				//设置参数
				Map<String, Object> init_map = new HashMap<>();
				init_map.put("user_id", url);

				Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName(
						"com.sdyk.ai.crawler.specific.jfh.task.modelTask.CaseTask");

				//生成holder
				TaskHolder holder =  ChromeTaskFactory.getInstance().newHolder(clazz, init_map);

				//提交任务
				((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

			} catch ( Exception e) {

				logger.error("error for submit CaseTask.class", e);
			}

		}

	}

}
