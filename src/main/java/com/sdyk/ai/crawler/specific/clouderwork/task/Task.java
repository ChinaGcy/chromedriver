package com.sdyk.ai.crawler.specific.clouderwork.task;

import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

public class Task extends com.sdyk.ai.crawler.task.Task {

	public Task(String url) throws MalformedURLException, URISyntaxException {
		super(url);
	}

	@Override
	public one.rewind.io.requester.Task validate() throws ProxyException.Failed, AccountException.Failed, AccountException.Frozen {
		return null;
	}
}
