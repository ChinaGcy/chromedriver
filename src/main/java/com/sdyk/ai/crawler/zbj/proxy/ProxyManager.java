package com.sdyk.ai.crawler.zbj.proxy;

import one.rewind.io.requester.proxy.Proxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import one.rewind.db.RedissonAdapter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ProxyManager {

	public static final Logger logger = LogManager.getLogger(ProxyManager.class.getName());

	protected static ProxyManager instance;

	public static String aliyun_g = "aliyun";

	public static String abuyun_g = "abuyun";

	public static ProxyManager getInstance() {

		if (instance == null) {
			synchronized (ProxyManager.class) {
				if (instance == null) {
					instance = new ProxyManager();
				}
			}
		}

		return instance;
	}


	private ConcurrentHashMap<String, RAtomicLong> lastRequestTime = new ConcurrentHashMap<>();

	private ProxyManager() {}

	/**
	 *
	 * @param proxy
	 */
	public void waits(Proxy proxy) {

		RLock lock = RedissonAdapter.redisson.getLock(proxy.getInfo());
		lock.lock(10, TimeUnit.SECONDS);

		if(lastRequestTime.get(proxy.getId()) == null) {
			lastRequestTime.put(proxy.getId(), RedissonAdapter.redisson.getAtomicLong("proxy-" + proxy.getId() + "-last-request-time"));
			lastRequestTime.get(proxy.getId()).set(System.currentTimeMillis());
		}

		long wait_time = lastRequestTime.get(proxy.getId()).get() + (long) Math.ceil(1000D / (double) proxy
		.getRequestPerSecondLimit()) - System.currentTimeMillis();

		if(wait_time > 0) {
			logger.info("Wait {} ms.", wait_time);
			try {
				Thread.sleep(wait_time);
			} catch (InterruptedException e) {
				logger.error(e);
			}
		}

		lastRequestTime.get(proxy.getId()).set(System.currentTimeMillis());
		lock.unlock();
	}
}
