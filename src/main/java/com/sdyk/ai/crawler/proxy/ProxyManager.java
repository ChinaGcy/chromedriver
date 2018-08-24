package com.sdyk.ai.crawler.proxy;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.sdyk.ai.crawler.exception.NoAvailableProxyException;
import com.sdyk.ai.crawler.model.Domain;
import com.sdyk.ai.crawler.proxy.model.ProxyImpl;
import one.rewind.db.DaoManager;
import one.rewind.db.PooledDataSource;
import one.rewind.io.requester.proxy.Proxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RLock;
import one.rewind.db.RedissonAdapter;
import org.redisson.api.RMultimap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 代理管理器
 */
public class ProxyManager {

	public static final Logger logger = LogManager.getLogger(ProxyManager.class.getName());

	protected static ProxyManager instance;

	public static String aliyun_g = "aliyun";

	public static String abuyun_g = "abuyun";

	/**
	 * 单例方法
	 * @return
	 */
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

	// 代理服务器 上一次使用时间
	private ConcurrentHashMap<String, RAtomicLong> lastRequestTime = new ConcurrentHashMap<>();

	// 代理服务器 domain 封禁情况
	public RMultimap<String, String> proxyDomainBannedMap;

	// 线程池，用于代理验证、代理失效回掉方法（代理服务器对应的主机重启）
	private ThreadPoolExecutor executor = new ThreadPoolExecutor(
			4,
			4,
			0, TimeUnit.MICROSECONDS,
			//new ArrayBlockingQueue<>(20)
			new LinkedBlockingQueue<>()
	);

	/**
	 *
	 */
	private ProxyManager() {

		executor.setThreadFactory(new ThreadFactoryBuilder()
				.setNameFormat("ProxyManager-%d").build());

		/*executor = Executors.newSingleThreadExecutor(
				new ThreadFactoryBuilder().setNameFormat("ProxyManager-%d").build());*/

		proxyDomainBannedMap = RedissonAdapter.redisson.getListMultimap("proxy-domain-banned-map");
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

		long available_proxy_count = queryBuilder.where()
				.eq("group", group)
				.and().eq("enable", true)
				.and().eq("status", Proxy.Status.Free).countOf();

		if(available_proxy_count < 3) {

			if(group.equals(AliyunHost.Proxy_Group_Name)) {
				new Thread(
					() -> {
						try {
							AliyunHost.batchBuild(2);
						} catch (InterruptedException e) {
							logger.error("Error build AliyunHost.", e);
						}
					}
				).start();
			}
		}

		ProxyImpl proxy = queryBuilder.limit(1L).orderBy("use_cnt", true)
				.where().eq("group", group)
				.and().eq("enable", true)
				.and().eq("status", Proxy.Status.Free)
				.queryForFirst();


		if (proxy == null) {

			throw new NoAvailableProxyException();

		} else {
			proxy.status = Proxy.Status.Busy;
			proxy.update();
			return proxy;
		}
	}

	/**
	 * 删除特定分组的Proxy
	 * @param groupName
	 * @throws Exception
	 */
	public boolean deleteProxyByGroup(String groupName) throws Exception {

		Dao<ProxyImpl, String> dao = DaoManager.getDao(ProxyImpl.class);

		DeleteBuilder<ProxyImpl, String> deleteBuilder = dao.deleteBuilder();
		deleteBuilder.where().eq("group", groupName);
		return deleteBuilder.delete() > 0;
	}

	/**
	 * 根据ID 删除Proxy
	 * @param id
	 * @throws Exception
	 */
	public boolean deleteProxyById(int id) throws Exception {

		Dao<ProxyImpl, String> dao = DaoManager.getDao(ProxyImpl.class);

		DeleteBuilder<ProxyImpl, String> deleteBuilder = dao.deleteBuilder();
		deleteBuilder.where().eq("id", id);
		return deleteBuilder.delete() == 1;
	}

	/**
	 * 获取有效的代理数量
	 * @return
	 * @throws Exception
	 */
	public int getValidProxyNum() {

		/*Dao<ProxyImpl, String> dao = DaoManager.getDao(ProxyImpl.class);
		QueryBuilder<ProxyImpl, String> qb = dao.queryBuilder()
				.where().eq("status", "Free").and().eq("enable", 1);

		return qb.query().size();*/

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
	 * 使用代理发起Http请求前的等待方法
	 * 控制单个代理对特定Domain的平均访问间隔
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

		long wait_time = lastRequestTime.get(proxy.getId()).get()
				+ (long) Math.ceil(1000D / (double) proxy.getRequestPerSecondLimit())
				- System.currentTimeMillis();

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

	/**
	 * 添加proxy 被 domain 封禁的记录
	 * @param proxy
	 * @param domain
	 */
	public void addProxyBannedRecord(Proxy proxy, String domain) {

		proxyDomainBannedMap.put(proxy.getInfo(), domain);
	}

	/**
	 * 设置所有代理状态为Free
	 * @throws Exception
	 */
	public void setAllProxyFree() throws Exception {

		List<ProxyImpl> proxies = DaoManager.getDao(ProxyImpl.class).queryForAll();
		for(ProxyImpl proxy : proxies) {
			proxy.status = Proxy.Status.Free;
			proxy.update();
		}
	}

	/**
	 * 根据分组名，设置该分组下的代理状态为Free
	 * @param group
	 * @throws Exception
	 */
	public void setAllProxyFree(String group) throws Exception {

		List<ProxyImpl> proxies = DaoManager.getDao(ProxyImpl.class).queryForEq("group", group);
		for(ProxyImpl proxy : proxies) {
			proxy.status = Proxy.Status.Free;
			proxy.update();
		}
	}

	/**
	 * 判断代理是否被所有网站封禁
	 * @param proxy
	 * @return
	 */
	public boolean isProxyBannedByAllDomain(Proxy proxy) {

		List<String> domains = Domain.getAll().stream().map(d -> {
			return d.domain;
		}).collect(Collectors.toList());

		logger.info("All domain: {}", domains);

		logger.info("Proxy: {} banned by: {}", proxy.getInfo(), proxyDomainBannedMap.get(proxy.getInfo()));

		// TODO Check
		if ( proxyDomainBannedMap.get(proxy.getInfo()).containsAll(domains) ) {
			return true;
		}

		return false;
	}

	/**
	 * Proxy 是否被特定 domain 封禁
	 * @param proxy
	 * @param domain
	 * @return
	 */
	public boolean isProxyBannedByDomain(Proxy proxy, String domain) {

		try{
			proxyDomainBannedMap.get(proxy.getInfo());
		} catch (Exception e) {
			return false;
		}

		return proxyDomainBannedMap.get(proxy.getInfo()).contains(domain);

	}
}
