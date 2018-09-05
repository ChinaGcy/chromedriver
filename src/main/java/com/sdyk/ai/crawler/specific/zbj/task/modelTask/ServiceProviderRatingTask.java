package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.witkey.ServiceProviderRating;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.ScanTask;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ChromeTaskFactory;
import one.rewind.io.requester.task.TaskHolder;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import org.openqa.selenium.NoSuchElementException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceProviderRatingTask extends ScanTask {

	public static long MIN_INTERVAL = 60 * 60 * 1000;

	static {
		registerBuilder(
				ServiceProviderRatingTask.class,
				"http://shop.zbj.com/evaluation/evallist-uid-{{user_id}}-category-1-isLazyload-0-page-{{page}}.html",
				ImmutableMap.of("user_id", String.class, "page", String.class),
				ImmutableMap.of("user_id", "0", "page", ""),
				false,
				Priority.MEDIUM
		);
	}

	String userId_;
	String page_;

	ServiceProviderRating serviceProviderRating;

	// http://shop.zbj.com/evaluation/evallist-uid-7791034-type-1-isLazyload-0-page-1.html

	/**
	 *
	 * @param url
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 * @throws ProxyException.Failed
	 */
	public ServiceProviderRatingTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {
		super(url);
		this.setBuildDom();

		this.addDoneCallback((t) -> {

			try {

				int page = 0;
				String user_id = "";
				Pattern pattern_url = Pattern.compile("http://shop.zbj.com/evaluation/evallist-uid-(?<userid>\\d+)-category-1-isLazyload-0-page-(?<page>\\d+).html");
				Matcher matcher_url = pattern_url.matcher(url);
				if (matcher_url.find()) {
					page = Integer.parseInt(matcher_url.group("page"));
					user_id = matcher_url.group("page");
				}
				userId_ = user_id;
				page_ = String.valueOf(page);

				Document doc = getResponse().getDoc();

				// 判断当前页面有多少评论
				int size = 0;

				try {
					size = doc.select("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl.user-information.clearfix")
							.size();
				} catch (NoSuchElementException e) {
					// 页面为空，size = 0 ，不采集数据
				}

				for (int i = 1; i <= size; i++) {

					// 防止每个评论的url一样导致id相同
					serviceProviderRating = new ServiceProviderRating(getUrl() + "--number:" + i);

					// 每个评价
					ratingData(doc, i, user_id);

					try {
						serviceProviderRating.insert();
					} catch (Exception e) {
						logger.error("Error insert: {}, ", e);
					}
				}

				// 翻页 #userlist > div.pagination > ul > li.disabled
				if (pageTurning("#userlist > div.pagination > ul > li", page)) {

					try {

						//设置参数
						Map<String, Object> init_map = new HashMap<>();
						ImmutableMap.of("user_id", user_id, "page", String.valueOf(++page));

						Class<? extends ChromeTask> clazz = (Class<? extends ChromeTask>) Class.forName("com.sdyk.ai.crawler.specific.zbj.task.modelTask.ServiceProviderRatingTask");

						//生成holder
						TaskHolder holder = ChromeTaskFactory.getInstance().newHolder(clazz, init_map);

						//提交任务
						ChromeDriverDistributor.getInstance().submit(holder);

					} catch (Exception e) {

						logger.error("error for submit ServiceProviderRatingTask.class", e);
					}

				}

			} catch (Exception e) {
				logger.error("", e);
			}
		});
	}

	/**
	 * 获取数据
	 * @param i
	 */
	public void ratingData(Document doc, int i, String userId) {

		serviceProviderRating.service_provider_id = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid("https://shop.zbj.com/"+ userId +"/"));

		String[] ss = doc.select("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dt > img")
				.attr("src").split("/");

		String url = "https://home.zbj.com/" + ss[3].substring(1)+ss[4]+ss[5]+ss[6].split("_")[2].split(".jpg")[0];

		serviceProviderRating.tenderer_id = one.rewind.txt.StringUtil.byteArrayToHex(one.rewind.txt.StringUtil.uuid(url));

		// http://task.zbj.com/13420593/
		String proUrl = doc.select("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dd:nth-child(2) > p.name-tit > a")
				.attr("href");
		serviceProviderRating.project_id = one.rewind.txt.StringUtil.byteArrayToHex(
				one.rewind.txt.StringUtil.uuid(
						proUrl.substring(0, proUrl.length()-2)));

		serviceProviderRating.tenderer_name = doc.select("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dd:nth-child(2) > p.name-tit")
				.text().split("成交价格：")[0];

		serviceProviderRating.price = Double.parseDouble(doc.select("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dd:nth-child(2) > p.name-tit")
				.text().split("成交价格：")[1].replaceAll("元", ""));

		serviceProviderRating.content = doc.select("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dd:nth-child(2) > p:nth-child(2) > span")
				.text();

		String tags = doc.select("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dd:nth-child(2) > p.yingx")
				.text();
		if (tags != null && !tags.equals("")) {
			serviceProviderRating.addTag(tags
					.replace("印象：", "")
					.split(" "));

		}
		try {
			serviceProviderRating.pubdate = DateFormatUtil.parseTime(doc.select("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dd.mint > p").text());
		} catch (ParseException e) {
			logger.error("serviceProviderRating  pubdate {}", e);
		}

	}

	@Override
	public TaskTrace getTaskTrace() {
		return new TaskTrace(this.getClass(), userId_, page_);
	}

}
