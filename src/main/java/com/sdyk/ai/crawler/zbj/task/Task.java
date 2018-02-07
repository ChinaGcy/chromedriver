package com.sdyk.ai.crawler.zbj.task;

import com.sdyk.ai.crawler.zbj.model.Account;
import org.openqa.selenium.WebDriver;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;

public class Task extends org.tfelab.io.requester.Task {

	public Task(String url) throws MalformedURLException, URISyntaxException {
		super(url);
	}

	public Task(String url, String post_data) throws MalformedURLException, URISyntaxException {
		super(url, post_data);
	}

	public Task(String url, String post_data, String cookies, String ref) throws MalformedURLException, URISyntaxException {
		super(url, post_data, cookies, ref);
	}

	public Task(String url, HashMap<String, String> headers, String post_data, String cookies, String ref) throws MalformedURLException, URISyntaxException {
		super(url, headers, post_data, cookies, ref);
	}

	public List<? extends Task> postProc(WebDriver driver) throws Exception {
		return null;
	}
}
