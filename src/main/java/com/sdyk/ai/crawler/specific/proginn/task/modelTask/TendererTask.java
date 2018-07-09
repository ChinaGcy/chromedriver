package com.sdyk.ai.crawler.specific.proginn.task.modelTask;

import com.sdyk.ai.crawler.model.witkey.Tenderer;
import com.sdyk.ai.crawler.specific.proginn.task.Task;
import org.jsoup.nodes.Document;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class TendererTask extends Task {

	Tenderer tenderer;

	public TendererTask(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setPriority(Priority.HIGH);

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
