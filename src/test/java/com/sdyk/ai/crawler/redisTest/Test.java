package com.sdyk.ai.crawler.redisTest;

import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.model.Domain;
import com.sdyk.ai.crawler.model.Model;
import com.sdyk.ai.crawler.model.witkey.Project;
import com.sdyk.ai.crawler.model.witkey.ServiceProvider;
import com.sdyk.ai.crawler.model.witkey.Tenderer;
import one.rewind.db.DaoManager;
import one.rewind.db.RedissonAdapter;
import org.redisson.api.RBlockingQueue;

public class Test {

	@org.junit.Test
	public void testRedission() throws Exception {

		new Thread(new Runnable() {
			@Override
			public void run() {
				RedissionPub.pubTask();
			}
		}).start();

		RedissionSub.subTask();

		Thread.sleep(500000);

	}

	@org.junit.Test
	public void test() throws Exception {

		Dao dao = DaoManager.getDao(ServiceProvider.class);

		Model a = (Model) dao.queryForId("4118800812aa73e406398a5b451ad10a");

		System.out.println(a.getClass() == ServiceProvider.class);

	}

	@org.junit.Test
	public void testQueue() throws InterruptedException {

		new Thread(() ->{

			RBlockingQueue<String> queue = RedissonAdapter.redisson.getBlockingQueue("TEST");

			System.out.println("start");
			while( true ){
				try {
					System.out.println(queue.take());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}).start();

		Thread.sleep(3000);

		new Thread(() -> {
			BlockingQueue b = new BlockingQueue();
			b.test();
		}).start();


		Thread.sleep(100000000);

	}

}
