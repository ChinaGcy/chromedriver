package com.sdyk.ai.crawler.specific.proginn.task.modelTask;

import com.sdyk.ai.crawler.model.witkey.Tenderer;
import com.sdyk.ai.crawler.specific.proginn.task.Task;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class TendererTask extends Task {

	public static List<String> crons = Arrays.asList("0 0 0/1 * * ? ", "0 0 0 1/1 * ? *");

	Tenderer tenderer;

	public TendererTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setPriority(Priority.HIGH);

		this.setNoFetchImages();

		this.addDoneCallback((t) -> {

			Document doc = getResponse().getDoc();

			crawler(doc);

		});
	}

	public void crawler( Document doc ){

		tenderer = new Tenderer(getUrl());

		tenderer.origin_id = getUrl().split("u/")[1];

		tenderer.name = doc.getElementsByClass("header").text();

		tenderer.content = doc.select("#J_WoMainContent > div:nth-child(1) > div").text();


	}

}
