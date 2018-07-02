package com.sdyk.ai.crawler.specific.zbj.task.modelTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.ServiceProviderRating;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.zbj.task.scanTask.ScanTask;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.txt.DateFormatUtil;
import org.jsoup.nodes.Document;
import org.openqa.selenium.NoSuchElementException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceProviderRatingTask extends ScanTask {

	static {
		// init_map_class
		init_map_class = ImmutableMap.of("user_id", String.class, "page", String.class);
		// init_map_defaults
		init_map_defaults = ImmutableMap.of("user_id", "0", "page", "0");
		// url_template
		url_template = "http://shop.zbj.com/evaluation/evallist-uid-{{user_id}}-category-1-isLazyload-0-page-{{page}}.html";

		need_login = false;
	}

	ServiceProviderRating serviceProviderRating;

	// http://shop.zbj.com/evaluation/evallist-uid-7791034-type-1-isLazyload-0-page-1.html

	public ServiceProviderRatingTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {
		super(url);
		this.setBuildDom();

		this.addDoneCallback((t) -> {

			try {

				String user_id = null;
				int page = 0;
				Pattern pattern = Pattern.compile("http://shop.zbj.com/evaluation/evallist-uid-(?<userId>.+?)-category-1-isLazyload-0-page-(?<page>.+?).html");
				Matcher matcher = pattern.matcher(url);
				if (matcher.find()) {
					user_id = matcher.group("userId");
					page = Integer.parseInt(matcher.group("page"));
				}

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

					HttpTaskPoster.getInstance().submit(this.getClass(),
							ImmutableMap.of("user_id", user_id, "page", String.valueOf(++page))
							);
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
			serviceProviderRating.tags = tags.split("印象：")[1];

		}
		try {
			serviceProviderRating.pubdate = DateFormatUtil.parseTime(doc.select("#userlist > div.moly-poc.user-fols.ml20.mr20 > dl:nth-child(" + i + ") > dd.mint > p").text());
		} catch (ParseException e) {
			logger.error("serviceProviderRating  pubdate {}", e);
		}

	}

	@Override
	public TaskTrace getTaskTrace() {
		return new TaskTrace(this.getClass(), this.getParamString("userId"), this.getParamString("page"));
	}

}
