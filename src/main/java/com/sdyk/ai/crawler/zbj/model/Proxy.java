package com.sdyk.ai.crawler.zbj.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.zbj.proxy.AliyunHost;
import one.rewind.db.DaoManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import one.rewind.db.DBName;


@DBName(value = "crawler")
@DatabaseTable(tableName = "proxies")
public class Proxy extends one.rewind.io.requester.proxy.Proxy {

	private static final Logger logger = LogManager.getLogger(Proxy.class.getName());

	public enum Source {
		ALIYUN_HOST,
		OTHERS
	}

	@DatabaseField(dataType = DataType.ENUM_STRING, width = 32)
	public Source source = Source.OTHERS;

	public Proxy() {}

	/**
	 *
	 * @param group
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @param location
	 */
	public Proxy(String group, String host, int port, String username, String password, String location, int request_per_second_limit) {
		super(group, host, port, username, password, location, request_per_second_limit);
	}

	public void setAliyunHost() {
		source = Source.ALIYUN_HOST;
	}

	/**
	 * 根据ID获取Proxy
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public static Proxy getProxyById(String id) throws Exception{

		Dao<Proxy, String> dao = DaoManager.getDao(Proxy.class);
		return dao.queryForId(id);
	}


	/**
	 * 根据分组名获取Proxy
	 * @return
	 * @throws Exception
	 */
	public static Proxy getValidProxy(String group) throws Exception {

		Dao<Proxy, String> dao = DaoManager.getDao(Proxy.class);

		QueryBuilder<Proxy, String> queryBuilder = dao.queryBuilder();
		Proxy ac = queryBuilder.limit(1L).orderBy("use_cnt", true)
				.where().eq("group", group)
				.and().eq("enable", true)
				.and().eq("status", Status.NORMAL)
				.queryForFirst();

		if (ac == null) {
			throw new Exception("Proxy not available.");
		} else {
			ac.use_cnt ++;
			ac.status = Status.INVALID;
			ac.update(); // 并发错误
			return ac;
		}
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	@Override
	public boolean success() throws Exception {
		return true;
	}

	/**
	 * TODO
	 * 由于只采集一个目标网站
	 * 简化出错处理：当目标网站封禁IP后，回调failed()
	 *
	 */
	@Override
	public boolean failed() throws Exception {

		if(source == Source.ALIYUN_HOST) {

			AliyunHost aliyunHost = AliyunHost.getByHost(host);
			if(aliyunHost != null) {
				aliyunHost.stop();
			}
		}

		this.status = Status.INVALID;
		this.update();

		return true;
	}

	/**
	 * TODO
	 */
	@Override
	public boolean timeout() throws Exception {
		return true;
	}

}
