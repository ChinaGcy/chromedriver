package com.sdyk.ai.crawler.specific.zbj.task;

import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.util.BinaryDownloader;
import com.sdyk.ai.crawler.util.StringUtil;
import one.rewind.db.DBName;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.*;

@DBName(value = "crawler")
@DatabaseTable(tableName = "tasks")
public class Task extends com.sdyk.ai.crawler.task.Task {

	public Task(String url) throws MalformedURLException, URISyntaxException {
		super(url);
	}

	public one.rewind.io.requester.Task validate() throws ProxyException.Failed, AccountException.Failed, AccountException.Frozen {

		// throw new ProxyException.Failed();
		// throw new UnreachableBrowserException("chromedriver is bad");
		if (getResponse().getText().contains("您的访问存在异常")) {
			throw new ProxyException.Failed();
		}

		return this;
	}
}
