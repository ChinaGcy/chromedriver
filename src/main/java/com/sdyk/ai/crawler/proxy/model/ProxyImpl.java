package com.sdyk.ai.crawler.proxy.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.proxy.ProxyManager;
import one.rewind.db.DaoManager;
import one.rewind.io.requester.BasicRequester;
import one.rewind.io.requester.proxy.Proxy;
import one.rewind.io.requester.task.ChromeTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import one.rewind.db.DBName;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.List;


@DBName(value = "sdyk_raw")
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

	/**
	 *
	 * @param domain
	 * @return
	 * @throws Exception
	 */
	public boolean failed(String domain) throws Exception {
		ProxyManager.getInstance().addProxyBannedRecord(this, domain);
		return failed();
	}

	/**
	 *
	 */
	private transient Runnable failedCallback;

	/**
	 *
	 * @param callback
	 */
	public void setFailedCallback(Runnable callback) {
		this.failedCallback = callback;
	}

	/**
	 * 测试代理下载速度
	 * @param url
	 * @return
	 * @throws MalformedURLException
	 * @throws URISyntaxException
	 */
	public float testSpeed(String url) throws MalformedURLException, URISyntaxException {

		float speedAvg = 0;

		for(int i=0; i<40; i++) {

			ChromeTask t = new ChromeTask(url);
			BasicRequester.getInstance().submit(t);

			speedAvg += (double) t.getResponse().getSrc().length / ( (double) t.getDuration() );

		}

		return speedAvg / 20;
	}

	/**
	 * TODO
	 */
	@Override
	public boolean timeout() throws Exception {
		return true;
	}
}
