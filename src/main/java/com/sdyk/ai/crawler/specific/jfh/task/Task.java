package com.sdyk.ai.crawler.specific.jfh.task;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class Task extends com.sdyk.ai.crawler.specific.clouderwork.task.Task {

	public Task(String url) throws MalformedURLException, URISyntaxException {
		super(url);
	}

	public static String domain(){
		return "jfh";
	}

}
