package com.sdyk.ai.crawler.specific.company.itjuzi;

import com.google.common.collect.ImmutableMap;
import com.sdyk.ai.crawler.model.company.CompanyInformation;
import com.sdyk.ai.crawler.task.Task;
import one.rewind.io.requester.exception.ProxyException;
import org.jsoup.nodes.Document;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItjuziTask extends Task {

	public static long MIN_INTERVAL = 60 * 60 * 1000L;

	static {
		registerBuilder(
				ItjuziTask.class,
				"{{companyUrl}}?uId={{uId}}",
				ImmutableMap.of("companyUrl", String.class, "uId", String.class),
				ImmutableMap.of("companyUrl", "","uId","")
		);
	}

	public static String domain(){
		return "itjuzi";
	}

	public ItjuziTask(String url) throws MalformedURLException, URISyntaxException, ProxyException.Failed {

		super(url);

		this.setBuildDom();

		this.setNoFetchImages();

		this.setPriority(Priority.HIGH);

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			crawler(doc);

		});
	}

	public void crawler(Document doc){

		String uId = null;
		Pattern pattern_url = Pattern.compile("uId=(?<uId>.+?)");
		Matcher matcher_url = pattern_url.matcher(getUrl());
		if (matcher_url.find()) {
			uId = matcher_url.group("uId");
		}

		CompanyInformation companyInformation = CompanyInformation.getCompanyInformationById(uId);

		companyInformation.tags = Arrays.asList(
				doc.select("#home > div > div.rowfoot > div.tagset.dbi.c-gray-aset.tag-list.feedback-btn-parent > div").text(),
				",");

		try {
			companyInformation.update();
		} catch (Exception e) {
			logger.error("error for companyInformation.insert", e);
		}

	}

}
