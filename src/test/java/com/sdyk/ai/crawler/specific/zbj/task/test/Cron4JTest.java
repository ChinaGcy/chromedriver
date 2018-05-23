package com.sdyk.ai.crawler.specific.zbj.task.test;

import it.sauronsoftware.cron4j.Scheduler;

public class Cron4JTest {

	public static void main(String[] args) {
		// Creates a Scheduler instance.
		Scheduler s = new Scheduler();
		// Schedule a once-a-minute task.
		// 分钟 小时 天 月 秒
		s.schedule("* * * * *", new Runnable() {
			public void run() {
				System.out.println("Another minute ticked away...");
			}
		});
		// Starts the scheduler.
		s.start();
		// Will run for ten minutes.
		try {
			Thread.sleep(1000L * 60L * 10L);
		} catch (InterruptedException e) {
			;
		}
		// Stops the scheduler.
		s.stop();
	}
}
