package com.sdyk.ai.crawler.specific.zbj.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.model.TaskTrace;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.CaseTask;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.TendererOrderTask;
import com.sdyk.ai.crawler.specific.zbj.task.modelTask.TendererRatingTask;
import one.rewind.io.requester.chrome.ChromeDriverDistributor;
import one.rewind.io.requester.exception.ProxyException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 乙方服务列表
 * 1. 找到url
 * 2. 翻页
 */
public class CaseScanTask extends ScanTask {

	static {
		/*// init_map_class
		init_map_class = ImmutableMap.of("user_id", String.class,"page", String.class);
		// init_map_defaults
		init_map_defaults = ImmutableMap.of("user_id", "0", "page", "0");
		// url_template
		url_template = "http://shop.zbj.com/{{user_id}}/servicelist-p{{page}}.html";

		need_login = false;*/
		registerBuilder(
				CaseScanTask.class,
				"http://shop.zbj.com/{{user_id}}/servicelist-p{{page}}.html",
				ImmutableMap.of("user_id", String.class,"page", String.class),
				ImmutableMap.of("user_id", "0", "page", "0")
		);
	}

	public static List<String> list = new ArrayList<>();

	/*//   http://shop.zbj.com/7523816/
	public static CaseScanTask generateTask(String uid, int page) {

		String url = "http://shop.zbj.com/" + uid + "/servicelist-p" + page + ".html";

		try {
			CaseScanTask t = new CaseScanTask(url, uid, page);
			return t;
		} catch (MalformedURLException | URISyntaxException | ProxyException.Failed e) {
			e.printStackTrace();
		}
		return null;
	}*/

	/**
	 *
	 * @param url
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public CaseScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setBuildDom();

		// http://shop.zbj.com/17788555/servicelist-p1.html
		this.addDoneCallback((t) -> {

			String userId = null;
			int page = 0;
			Pattern pattern_url = Pattern.compile("http://shop.zbj.com/(?<userId>.+?)\\/servicelist-p(?<page>.+?).html");
			Matcher matcher_url = pattern_url.matcher(url);
			if (matcher_url.find()) {
				userId = matcher_url.group("userId");
				page = Integer.parseInt(matcher_url.group("page"));
			}

			try {

				String src = getResponse().getText();

				// 判断是否翻页
				if (src.contains("暂时还没有此类服务")) {
					return;
				}

				// 获取猪八戒， 天蓬网的服务地址
				Pattern pattern = Pattern.compile("http://shop.zbj.com/\\d+/sid-\\d+.html");
				Matcher matcher = pattern.matcher(src);
				Pattern pattern_tp = Pattern.compile("http://shop.tianpeng.com/\\d+/sid-\\d+.html");
				Matcher matcher_tp = pattern_tp.matcher(src);

				getCaseUrl(matcher, userId);

				getCaseUrl(matcher_tp, userId);

			} catch (Exception e) {
				logger.error(e);
			}

			if (pageTurning("#contentBox > div > div.pagination > ul > li", page)) {
				/*HttpTaskPoster.getInstance().submit(this.getClass(),
						ImmutableMap.of("user_id", userId, "page", String.valueOf(++page)));*/
				ChromeDriverDistributor.getInstance().submit(
						this.getHolder(
								this.getClass(),
								ImmutableMap.of("user_id", userId, "page", String.valueOf(++page))));
			}
		});
	}

	@Override
	public TaskTrace getTaskTrace() {
		return new TaskTrace(this.getClass(), this.getParamString("uid"), this.getParamString("page"));
	}

	/**
	 * 添加Case任务
	 * @param matcher
	 * @param userId
	 */
	public void getCaseUrl(Matcher matcher, String userId) {
		// 猪八戒url
		while (matcher.find()) {

			String new_url = matcher.group();

			String case_id = new_url.split("/")[4]
					.replace("sid-", "")
					.replace(".html", "");

			if (!list.contains(new_url)) {
				list.add(new_url);

				try {
					HttpTaskPoster.getInstance().submit(CaseTask.class,
							ImmutableMap.of("user_id", userId, "case_id", case_id));
				} catch (ClassNotFoundException | UnsupportedEncodingException | MalformedURLException | URISyntaxException e) {
					e.printStackTrace();
				}

			}
		}
	}
}
