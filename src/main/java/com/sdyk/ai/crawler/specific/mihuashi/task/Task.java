package com.sdyk.ai.crawler.specific.mihuashi.task;

import one.rewind.io.requester.chrome.ChromeTaskScheduler;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ScheduledChromeTask;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class Task extends com.sdyk.ai.crawler.specific.clouderwork.task.Task {

	public static List<String> crons = Arrays.asList("0 0 0/1 * * ? ", "0 0 0 1/1 * ? *");

	public Task(String url) throws MalformedURLException, URISyntaxException {
		super(url);
	}

	public void cronTask(ChromeTask t){

		ScheduledChromeTask st = t.getScheduledChromeTask();
		if(st == null) {

			try {
				st = new ScheduledChromeTask(t.getHolder(), crons);
				st.start();
			} catch (Exception e) {
				logger.error("error for ScheduledChromeTask");
			}

		}else{
			st.degenerate();
		}

	}

}
