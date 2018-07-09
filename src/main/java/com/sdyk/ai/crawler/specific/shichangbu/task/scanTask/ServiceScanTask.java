package com.sdyk.ai.crawler.specific.shichangbu.task.scanTask;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.HttpTaskPoster;
import com.sdyk.ai.crawler.specific.shichangbu.task.modelTask.ServiceProviderTask;
import one.rewind.io.requester.exception.ProxyException;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ServiceScanTask extends ScanTask {

	static {
		registerBuilder(
				ServiceScanTask.class,
				"http://www.shichangbu.com/portal.php?mod=provider&op=search&sort=2&page={{page}}",
				ImmutableMap.of("page", String.class),
				ImmutableMap.of("page", "")
		);
	}

	public ServiceScanTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			//获取当前页数
			int page = 0;
			Pattern pattern_url = Pattern.compile("page=(?<page>.+?)");
			Matcher matcher_url = pattern_url.matcher(url);
			if (matcher_url.find()) {
				page = Integer.parseInt(matcher_url.group("page"));
			}

			String src = getResponse().getDoc().html();

			Pattern pattern = Pattern.compile("<a href=\"agency-(?<username>.+?).html\" title");
			Matcher matcher = pattern.matcher(src);

			Set<String> usernames = new HashSet<>();

			while( matcher.find() ) {
				usernames.add(matcher.group("username"));
			}

			for(String user : usernames){

				if( backtrace == true ){

					try {
						HttpTaskPoster.getInstance().submit(ServiceProviderTask.class,
								ImmutableMap.of("servicer_id", user));
					} catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {

						logger.error("error fro HttpTaskPoster.submit ServiceProviderTask.class", e);
					}

				}

			}

			String next = getResponse().getDoc().getElementsByClass("nxt").text();
			if( next != null && !"".equals(next) ) {

				int nextPag = page + 1;

				try {
					HttpTaskPoster.getInstance().submit(ServiceScanTask.class,
							ImmutableMap.of("page", String.valueOf(nextPag)));
				} catch (ClassNotFoundException | MalformedURLException | URISyntaxException | UnsupportedEncodingException e) {

					logger.error("error fro HttpTaskPoster.submit ServiceScanTask.class", e);
				}

			}

		});
	}

}
