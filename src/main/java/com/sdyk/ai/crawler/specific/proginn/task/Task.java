package com.sdyk.ai.crawler.specific.proginn.task;

import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class Task extends com.sdyk.ai.crawler.specific.clouderwork.task.Task {

	public Task(String url) throws MalformedURLException, URISyntaxException {
		super(url);
	}

	public static String domain(){
		return "projinn";
	}

}
