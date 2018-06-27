package com.sdyk.ai.crawler.proxy;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.sdyk.ai.crawler.proxy.exception.NoAvailableProxyException;
import com.sdyk.ai.crawler.proxy.model.ProxyImpl;
import one.rewind.db.DaoManager;
import one.rewind.db.PooledDataSource;
import one.rewind.io.requester.proxy.Proxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import one.rewind.db.RedissonAdapter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.*;

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

	private ThreadPoolExecutor executor = new ThreadPoolExecutor(
			4,
			4,
			0, TimeUnit.MICROSECONDS,
			//new ArrayBlockingQueue<>(20)
			new LinkedBlockingQueue<>()
	);

	private ProxyManager() {

		executor.setThreadFactory(new ThreadFactoryBuilder()
				.setNameFormat("ProxyManager-%d").build());

		/*executor = Executors.newSingleThreadExecutor(
				new ThreadFactoryBuilder().setNameFormat("ProxyManager-%d").build());*/
	}

	/**
	 *
	 * @param callback
	 */
	public void submit(Runnable callback) {
		executor.submit(callback);
	}

	/**
	 * 根据ID获取Proxy
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public Proxy getProxyById(String id) throws Exception{

		Dao<ProxyImpl, String> dao = DaoManager.getDao(ProxyImpl.class);
		return dao.queryForId(id);
	}


	/**
	 * 根据分组名获取Proxy
	 * @return
	 * @throws Exception
	 */
	public synchronized ProxyImpl getValidProxy(String group) throws Exception {

		Dao<ProxyImpl, String> dao = DaoManager.getDao(ProxyImpl.class);

		QueryBuilder<ProxyImpl, String> queryBuilder = dao.queryBuilder();
		ProxyImpl proxy = queryBuilder.limit(1L).orderBy("use_cnt", true)
				.where().eq("group", group)
				.and().eq("enable", true)
				.and().eq("status", Proxy.Status.Free)
				.queryForFirst();

		if (proxy == null) {
			throw new NoAvailableProxyException();
		} else {
			proxy.use_cnt ++;
			proxy.status = Proxy.Status.Busy;
			proxy.update();
			return proxy;
		}
	}

	public void deleteProxyByGroup(String groupName) throws Exception {

		Dao<ProxyImpl, String> dao = DaoManager.getDao(ProxyImpl.class);

		DeleteBuilder<ProxyImpl, String> deleteBuilder = dao.deleteBuilder();
		deleteBuilder.where().eq("group", groupName);
		deleteBuilder.delete();
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public int getValidProxyNum() {

		/*Dao<ProxyImpl, String> dao = DaoManager.getDao(ProxyImpl.class);
		QueryBuilder<ProxyImpl, String> qb = dao.queryBuilder()
				.where().eq("status", "Free").and().eq("enable", 1);*/

		int num = 0;

		Connection conn = null;
		Statement stmt = null;

		try {

			String sql = "SELECT COUNT(*) as num FROM proxies WHERE status = 'Free' AND enable = 1;";

			conn = PooledDataSource.getDataSource("sdyk_raw").getConnection();
			stmt = conn.createStatement();
			ResultSet result = stmt.executeQuery(sql);

			if (result.next()) {
				num = result.getInt(1);
			}

		}
		catch (Exception e) {

			e.printStackTrace();

		}
		finally {

			try {

				stmt.close();
				conn.close(); // 把conn返回的方法

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		return num;
	}

	/**
	 * 多线程环境，处理代理调用间隔
	 * @param proxy
	 */
	public void waits(Proxy proxy) {

		if(proxy.getRequestPerSecondLimit() <= 0) return;

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
