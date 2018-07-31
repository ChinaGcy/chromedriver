package com.sdyk.ai.crawler.specific.oschina.task;

import one.rewind.io.requester.chrome.ChromeTaskScheduler;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import one.rewind.io.requester.task.ScheduledChromeTask;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

public class Task extends com.sdyk.ai.crawler.task.Task {

	public static List<String> crons = Arrays.asList("0 0 0/1 * * ? ", "0 0 0 1/1 * ? *");

	public Task(String url) throws MalformedURLException, URISyntaxException {
		super(url);
	}

	public void cornTask(ChromeTask t){
		// 此任务尚未注册
		if( !ChromeTaskScheduler.getInstance().registered(t._scheduledTaskId) ){
			try {
				ScheduledChromeTask scheduledTask = new ScheduledChromeTask(
						t.getHolder(this.getClass(), this.init_map),
						this.crons
				);
				ChromeTaskScheduler.getInstance().schedule(scheduledTask);
			} catch (Exception e) {
				logger.error("eror for creat ScheduledChromeTask", e);
			}
		}
		// 任务已经注册过
		else {
			try {
				// 增加延长时间
				ChromeTaskScheduler.getInstance().degenerate(t._scheduledTaskId);
			} catch (Exception e) {
				logger.error("eror for degenerate ScheduledChromeTask", e);
			}
		}
	}

}
