package com.sdyk.ai.crawler.specific.mihuashi.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.Distributor;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.witkey.ServiceProviderRating;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import com.sdyk.ai.crawler.specific.mihuashi.action.LoadMoreContentAction;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.chrome.action.ClickAction;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;

import one.rewind.txt.DateFormatUtil;
import one.rewind.txt.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceRatingTask extends Task {

	public static long MIN_INTERVAL = 12 * 60 * 60 * 1000L;

	static {
		registerBuilder(
				ServiceRatingTask.class,
				"https://www.mihuashi.com/users/{{service_id}}?role=painter&rating=true",
				ImmutableMap.of("service_id", String.class),
				ImmutableMap.of("service_id", "")
		);
	}


	public String workFilePath = "#users-show > div.container-fluid > div.profile__container > main > header > ul > li:nth-child(2) > a";

	public String morePath = "#vue-comments-app > div:nth-child(2) > a";

    public ServiceRatingTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

        super(url);

        this.setPriority(Priority.MEDIUM);

	    this.setNoFetchImages();

	    this.setValidator((a,t) -> {

		    String src = getResponse().getText();
		    if( src.contains("邮箱登陆") && src.contains("注册新账号") ){

			    throw new AccountException.Failed(a.accounts.get("mihuashi.com"));
		    }
	    });

	    this.addAction(new ClickAction( workFilePath ));
	    this.addAction(new LoadMoreContentAction(morePath));

        this.addDoneCallback((t)->{

            Document doc = getResponse().getDoc();

            //执行抓取任务
            crawlawJob(doc);
        });
    }

	public void crawlawJob(Document doc){

		Pattern pattern = Pattern.compile("/users/(?<username>.+?)\\?role=painter");
		Matcher matcher = pattern.matcher(getUrl());
		String web=null;
		while(matcher.find()) {
			try {
				web = URLDecoder.decode(matcher.group("username"), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		Elements elements = doc.getElementsByClass("profile__comment-cell");
		Pattern pattern1 = Pattern.compile("/users/(?<username>.+?)\\?role=employer");
		int i = 0;

		for(Element element : elements){
			ServiceProviderRating serviceProviderRating = new ServiceProviderRating(getUrl()+"&num="+i);
			i++;

			//服务商ID
			serviceProviderRating.service_provider_id = one.rewind.txt.StringUtil.byteArrayToHex(
					one.rewind.txt.StringUtil.uuid(getUrl()));

			// 评分
			Elements elements1 = element.select("div.rating > div:nth-child(1) > i");
			serviceProviderRating.rating = elements1.size();

			//甲方名字
			Elements name = element.getElementsByClass("name");
			serviceProviderRating.tenderer_name = name.get(1).text();
			try {
				String url = URLDecoder.decode(name.get(1).attr("href"), "UTF-8");

				//甲方ID
				serviceProviderRating.tenderer_id = one.rewind.txt.StringUtil.byteArrayToHex(
						one.rewind.txt.StringUtil.uuid(url));

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			//项目名称
			serviceProviderRating.project_name = name.get(0).text();

			//描述
			serviceProviderRating.content = element.getElementsByClass("content").text();

			// 价格
			String price = element.select("span.budget-interval").text();
			if( price.contains("-") ){
				price = price.split("-")[1];
			}
			price = CrawlerAction.getNumbers(price);
			if( price.length() > 1 ){
				serviceProviderRating.price = Double.valueOf(price);
			}

			//发布时间
			String time = element.getElementsByClass("commented-time").text();
			try {
				serviceProviderRating.pubdate = DateFormatUtil.parseTime(time.split("评论于")[1]);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			//项目URL
			String proUrl = element.toString();
			Pattern pattern2 = Pattern.compile("/projects/\\d+");
			Matcher matcher2 = pattern2.matcher(proUrl);
			while (matcher2.find()) {

				String project_id = matcher2.group().replace("/projects/","");

				try {

					//设置参数
					Map<String, Object> init_map = new HashMap<>();
					init_map.put("project_id", project_id);
					init_map.put("flage", "0");

					Class<? extends ChromeTask> clazz =  (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.mihuashi.task.modelTask.ProjectTask");

					//生成holder
					//ChromeTaskHolder holder = ChromeTask.buildHolder(clazz, init_map);

					//提交任务
					((Distributor)ChromeDriverDistributor.getInstance()).submit(holder);

				} catch ( Exception e) {

					logger.error("error for submit ProjectTask.class", e);
				}

			}

			serviceProviderRating.insert();

		}

	}

}