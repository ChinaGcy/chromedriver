package com.sdyk.ai.crawler.specific.jfh.task;

import one.rewind.io.requester.chrome.ChromeTaskScheduler;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ScheduledChromeTask;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class Task extends com.sdyk.ai.crawler.specific.clouderwork.task.Task {

	// 每天执行一次
	public static List<String> crons = Arrays.asList("0 0 0/1 * * ? ", "0 0 0 1/1 * ? *");

	public Task(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setBuildDom();
	}

	public static String domain(){
		return "jfh";
	}

	public void cornTask(ChromeTask t){

		ScheduledChromeTask st = t.getScheduledChromeTask();
		if(st == null) {

			try {
				st = new ScheduledChromeTask(t.getHolder(this.init_map), crons);
				st.start();
			} catch (Exception e) {
				logger.error("error for ScheduledChromeTask");
			}

		}

	}
}
