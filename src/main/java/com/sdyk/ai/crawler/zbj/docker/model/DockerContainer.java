package com.sdyk.ai.crawler.zbj.docker.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.sdyk.ai.crawler.zbj.docker.DockerHostManager;
import one.rewind.db.DaoManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * 容器
 */
public class DockerContainer {

	public enum Status {
		STARTING, // 启动中
		IDLE, // 空闲
		OCCUPIED, // 占用
		FAILED, // 出错
		TERMINATED // 已删除
	}

	@DatabaseField(dataType = DataType.INTEGER, canBeNull = false, generatedId = true)
	public int id;

	@DatabaseField(dataType = DataType.STRING, width = 128, canBeNull = false)
	public String name;

	@DatabaseField(dataType = DataType.STRING, width = 32, canBeNull = false)
	public String ip;

	@DatabaseField(dataType = DataType.INTEGER, width = 5, canBeNull = false)
	public int seleniumPort;

	@DatabaseField(dataType = DataType.INTEGER, width = 5, canBeNull = false)
	public int vncPort;

	@DatabaseField(dataType = DataType.ENUM_STRING, width = 16, canBeNull = false)
	public Status status = Status.STARTING;

	// 插入时间
	@DatabaseField(dataType = DataType.DATE)
	public Date insert_time = new Date();

	// 更新时间
	@DatabaseField(dataType = DataType.DATE, index = true)
	public Date update_time = new Date();

	/**
	 *
	 * @param ip
	 * @param name
	 * @param seleniumPort
	 * @param vncPort
	 */
	public DockerContainer(String ip, String name, int seleniumPort, int vncPort) {
		this.ip = ip;
		this.name = name;
		this.seleniumPort = seleniumPort;
		this.vncPort = vncPort;
	}

	public void setIdle() throws Exception {
		this.status = Status.IDLE;
		this.update();
	}

	public void setOccupied() {
		this.status = Status.OCCUPIED;
	}

	/**
	 * 获取路由的地址
	 * @throws MalformedURLException
	 */
	public URL getRemoteAddress() throws MalformedURLException {
		return new URL("http://" +ip+  ":" + seleniumPort + "/wd/hub");
	}

	/**
	 * 删除容器
	 */
	public void rm() throws Exception {

		String cmd = "docker rm -f " + name +  "\n";

		DockerHost host = DockerHostManager.getInstance().getHostByIp(ip);

		if(host != null) {

			String output = host.exec(cmd);
			// TODO 根据output 判断是否执行成功

			DockerHostManager.logger.info(output);
			status = Status.TERMINATED;
		}
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public boolean insert() throws Exception{

		Dao<DockerContainer, String> dao = DaoManager.getDao(DockerContainer.class);

		if (dao.create(this) == 1) {
			return true;
		}

		return false;
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public boolean update() throws Exception {
		Dao<DockerContainer, String> dao = DaoManager.getDao(DockerContainer.class);

		if (dao.update(this) == 1) {
			return true;
		}

		return false;
	}
}