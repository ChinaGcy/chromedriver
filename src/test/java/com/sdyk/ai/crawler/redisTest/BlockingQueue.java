package com.sdyk.ai.crawler.redisTest;

import one.rewind.db.RedissonAdapter;
import org.redisson.api.RBlockingQueue;

import java.util.Date;

public class BlockingQueue {


	public void test(){

		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		RBlockingQueue<String> queue = RedissonAdapter.redisson.getBlockingQueue("TEST");

		System.out.println(new Date() + "offer test1");

		queue.offer("test1");

		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println(new Date() + "offer test2");

		queue.offer("test2");

	}
}
