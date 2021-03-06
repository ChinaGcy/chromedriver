package com.sdyk.ai.crawler.specific.proLagou.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.witkey.ServiceProviderRating;
import com.sdyk.ai.crawler.specific.clouderwork.util.CrawlerAction;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;

public class ServiceProviderRatingTask extends com.sdyk.ai.crawler.task.Task {

	public static long MIN_INTERVAL = 24 * 60 * 60 * 1000L;

	static {
		registerBuilder(
				ServiceProviderRatingTask.class,
				"https://pro.lagou.com/user/{{user_id}}.html#bottom_comment",
				ImmutableMap.of("user_id", String.class),
				ImmutableMap.of("user_id","")
		);
	}

	public ServiceProviderRatingTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setNoFetchImages();

		this.setPriority(Priority.HIGH);

		// 检测异常
		this.setValidator((a,t) -> {

			String src = getResponse().getText();
			if( src.contains("登陆") && src.contains("没有账号") ){

				throw new AccountException.Failed(a.accounts.get(t.getDomain()));
			}
		});

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			String userUrl = getUrl().replace("#bottom_comment","");

			//页面正常
			crawlerJob(doc, userUrl);

		});
	}

	public void crawlerJob(Document doc, String userUrl){

		Elements elements = doc.getElementsByClass("comment");

		int i =0;
		for(Element element : elements){
			i++;

			ServiceProviderRating serviceProviderRating = new ServiceProviderRating(getUrl() + "&rating" + i);

			serviceProviderRating.project_id = one.rewind.txt.StringUtil.byteArrayToHex(
					one.rewind.txt.StringUtil.uuid(
							element.select(" div.title > h3 > a").attr("href")));

			serviceProviderRating.service_provider_id = one.rewind.txt.StringUtil.byteArrayToHex(
					one.rewind.txt.StringUtil.uuid(userUrl));

			serviceProviderRating.project_name = element.select(" div.title > h3 > a").text();

			String pubdate = element.getElementsByClass("time").text();
			try {

				serviceProviderRating.pubdate = DateFormatUtil.parseTime(pubdate);
			} catch (ParseException e) {
				logger.error("error for string to Date", e);
			}

			serviceProviderRating.tenderer_name = element.getElementsByClass("base_title")
					.text().replace(pubdate, "");

			serviceProviderRating.content = element.select("dl > dd > pre").text();

			String price = element.getElementsByClass("day_price").text();
			price = CrawlerAction.getNumbers(price);
			if( price != null && !"".equals(price) ){
				serviceProviderRating.price = Integer.valueOf(price);
			}

			String rating = element.getElementsByClass("grade").text();
			rating = CrawlerAction.getNumbers(rating);
			if( rating != null && !"".equals(rating) ){
				serviceProviderRating.rating = Integer.valueOf(rating);
			}

			serviceProviderRating.insert();

		}

	}

}
