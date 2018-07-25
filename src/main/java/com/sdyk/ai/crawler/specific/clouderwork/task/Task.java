package com.sdyk.ai.crawler.specific.clouderwork.task;

import com.j256.ormlite.table.DatabaseTable;
import one.rewind.db.DBName;
import one.rewind.io.requester.exception.AccountException;
import one.rewind.io.requester.exception.ProxyException;
import one.rewind.io.requester.task.ChromeTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URISyntaxException;


@DBName(value = "proc")
@DatabaseTable(tableName = "tasks")
public class Task extends ChromeTask {

	public static final Logger logger = LogManager.getLogger(com.sdyk.ai.crawler.specific.zbj.task.Task.class.getName());

	public Task(String url) throws MalformedURLException, URISyntaxException {

		super(url);

		this.setBuildDom();
	}

	/**
	 * 获取String型数据
	 * @param path
	 * @param clean
	 * @return
	 */
	public String getString(String path, String... clean) {

		String found = null;
		if(getResponse().getDoc() != null) {
			found = getResponse().getDoc().select(path).text();
			for(String c : clean) {
				found = found.replaceAll(c, "");
			}
		}

		return found;
	}

	/**
	 * 获取int型数据
	 * @param path
	 * @param clean
	 * @return
	 */
	public int getInt(String path, String... clean) {
		try {
			return Integer.parseInt(getString(path, clean));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	/**
	 * 获取float型数据
	 * @param path
	 * @param clean
	 * @return
	 */
	public float getFloat(String path, String... clean) {
		try {
			return Float.parseFloat(getString(path, clean));
		} catch (NumberFormatException e) {
			return 0.0f;
		}
	}

	/**
	 * 获取double型数据
	 * @param path
	 * @param clean
	 * @return
	 */
	public double getDouble(String path, String... clean) {
		try {
			return Double.parseDouble(getString(path, clean));
		} catch (NumberFormatException e) {
			return 0.0d;
		}
	}

}
