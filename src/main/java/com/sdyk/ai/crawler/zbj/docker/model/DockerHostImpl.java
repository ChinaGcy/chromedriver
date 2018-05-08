package com.sdyk.ai.crawler.zbj.docker.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.zbj.docker.DockerHostManager;
import com.typesafe.config.Config;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.docker.model.DockerHost;
import one.rewind.util.Configs;
import org.redisson.api.RLock;

import java.io.File;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static one.rewind.db.RedissonAdapter.logger;
import static one.rewind.db.RedissonAdapter.redisson;

@DBName(value = "crawler")
@DatabaseTable(tableName = "docker_hosts")
public class DockerHostImpl extends DockerHost {

	/**
	 *
	 * @throws Exception
	 */
	public ChromeDriverDockerContainer createChromeDriverDockerContainer() throws Exception {

		ChromeDriverDockerContainer container = null;

		int currentContainerNum = this.addContainerNum();

		int seleniumPort = (ChromeDriverDockerContainer.SELENIUM_BEGIN_PORT + currentContainerNum);
		int vncPort = (ChromeDriverDockerContainer.VNC_BEGIN_PORT + currentContainerNum);
		String containerName = "ChromeContainer-" + this.ip + "-" + currentContainerNum;

		container = new ChromeDriverDockerContainerImpl(this, containerName, seleniumPort, vncPort);
		container.create();

		return container;
	}

	/**
	 * 统计container数量
	 */
	public int addContainerNum() throws Exception {

		RLock lock = redisson.getLock("Docker-Host-Access-Lock-" + ip);

		lock.lock(10, TimeUnit.SECONDS);

		Dao<DockerHostImpl, String> dao = DaoManager.getDao(DockerHostImpl.class);
		// 刷新内存数据
		dao.refresh(this);

		this.container_num ++;
		this.update();

		lock.unlock();

		return this.container_num;
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public int minusContainerNum() throws Exception {

		RLock lock = redisson.getLock("Docker-Host-Access-Lock-" + ip);

		lock.lock(10, TimeUnit.SECONDS);

		Dao<DockerHostImpl, String> dao = DaoManager.getDao(DockerHostImpl.class);
		// 刷新内存数据
		dao.refresh(this);

		this.container_num --;
		this.update();

		lock.unlock();

		return this.container_num;
	}

}
