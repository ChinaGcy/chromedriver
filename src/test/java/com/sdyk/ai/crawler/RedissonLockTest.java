package com.sdyk.ai.crawler;

import one.rewind.db.RedissonAdapter;
import org.junit.Test;
import org.redisson.Redisson;
import org.redisson.api.RLock;

public class RedissonLockTest {

	@Test
	public void test() throws InterruptedException {

		new Thread(() -> {
			RLock lock = RedissonAdapter.redisson.getLock("AAA");
			lock.lock();
			System.err.println(Thread.currentThread().getName() + "/t locked.");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.err.println(Thread.currentThread().getName() + "/t unlocked.");
			lock.unlock();
		}).start();

		new Thread(() -> {
			RLock lock = RedissonAdapter.redisson.getLock("AAA");
			lock.lock();
			System.err.println(Thread.currentThread().getName() + "/t locked.");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.err.println(Thread.currentThread().getName() + "/t unlocked.");
			lock.unlock();
		}).start();


		Thread.sleep(10000);
	}
}
