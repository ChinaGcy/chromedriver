package com.sdyk.ai.crawler.specific.proginn.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.specific.proginn.task.modelTask.ServiceProviderTask;
import one.rewind.io.requester.exception.ProxyException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceScanTask extends ScanTask {

	static {
		registerBuilder(
				ServiceScanTask.class,
				"https://www.proginn.com/page/{{page}}",
				ImmutableMap.of("page", String.class),
				ImmutableMap.of("page", "")
		);
	}

	public ServiceScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setBuildDom();

		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			//获取当前页数
			int page = 0;
			Pattern pattern_url = Pattern.compile("page/(?<page>.+?)");
			Matcher matcher_url = pattern_url.matcher(url);
			if (matcher_url.find()) {
				page = Integer.parseInt(matcher_url.group("page"));
			}

			Document doc = getResponse().getDoc();

			Elements userList = doc.getElementsByClass("item J_user");

			for(Element element : userList){

				String uId = element.getElementsByClass("user-avatar").attr("userid");

				try {
					HttpTaskPoster.getInstance().submit(ServiceProviderTask.class,
							ImmutableMap.of("servicer_id", uId));

				} catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {
					logger.error("error for HttpTaskPoster.submit ServiceScanTask", e);
				}

			}

			Elements pageList= doc.getElementsByClass("item");
			String pageLast = pageList.get(pageList.size() - 1).text();

			if( !pageLast.equals("...") ){

				int next = page + 1;

				try {
					HttpTaskPoster.getInstance().submit(ServiceScanTask.class,
							ImmutableMap.of("page", String.valueOf(next)));

				} catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {
					logger.error("error for HttpTaskPoster.submit ServiceScanTask", e);
				}

			}

		});
	}
}
