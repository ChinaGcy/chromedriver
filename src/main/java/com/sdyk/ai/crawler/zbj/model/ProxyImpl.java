package com.sdyk.ai.crawler.zbj.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.zbj.proxy.AliyunHost;
import com.sdyk.ai.crawler.zbj.proxy.ProxyManager;
import one.rewind.db.DaoManager;
import one.rewind.io.requester.proxy.Proxy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import one.rewind.db.DBName;

import java.util.concurrent.Callable;


@DBName(value = "crawler")
@DatabaseTable(tableName = "proxies")
public class ProxyImpl extends Proxy {

	private static final Logger logger = LogManager.getLogger(ProxyImpl.class.getName());

	public enum Source {
		ALIYUN_HOST,
		OTHERS
	}

	@DatabaseField(dataType = DataType.ENUM_STRING, width = 32)
	public Source source = Source.OTHERS;

	public ProxyImpl() {}

	/**
	 *
	 * @param group
	 * @param host
	 * @param port
	 * @param username
	 * @param password
	 * @param location
	 */
	public ProxyImpl(String group, String host, int port, String username, String password, String location, int request_per_second_limit) {
		super(group, host, port, username, password, location, request_per_second_limit);
	}

	public void setAliyunHost() {
		source = Source.ALIYUN_HOST;
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

		this.status = Status.INVALID;
		this.enable = false;
		this.update();
		ProxyManager.getInstance().submit(failedCallback);
		return true;
	}

	private Runnable failedCallback;

	public void setFailedCallback(Runnable callback) {
		this.failedCallback = callback;
	}

	/**
	 * TODO
	 */
	@Override
	public boolean timeout() throws Exception {
		return true;
	}

}
