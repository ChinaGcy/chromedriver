package com.sdyk.ai.crawler.docker.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.docker.model.DockerHost;
import org.redisson.api.RLock;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static one.rewind.db.RedissonAdapter.redisson;

@DBName(value = "crawler")
@DatabaseTable(tableName = "docker_hosts")
public class DockerHostImpl extends DockerHost {

	@DatabaseField(dataType = DataType.SERIALIZABLE, width = 1024)
	public HashSet<Integer> occupiedPorts;

	public DockerHostImpl() {}

	public DockerHostImpl(String ip, int port, String username) {
		this.status = DockerHost.Status.RUNNING;
		this.insert_time = new Date();
		this.update_time = new Date();
		this.ip = ip;
		this.port = port;
		this.username = username;
	}

	/**
	 *
	 * @return
	 */
	public int getPort() throws Exception {

		RLock lock = redisson.getLock("Docker-Host-Access-Lock-" + ip);
		lock.lock();

		Dao dao = DaoManager.getDao(DockerHostImpl.class);
		dao.refresh(this);

		int port = -1;

		if (occupiedPorts == null) {
			occupiedPorts = new HashSet<>();
		}

		for(int i=0; i<1000; i++) {
			if(!occupiedPorts.contains(i)) {
				port = i;
				break;
			}
		}

		occupiedPorts.add(port);
		this.update();

		lock.unlock();
		return port;
	}

	/**
	 *
	 * @throws Exception
	 */
	public ChromeDriverDockerContainer createChromeDriverDockerContainer() throws Exception {

		ChromeDriverDockerContainer container = null;

		int port = getPort();

		logger.info("Use {} port: {}", ip, port);

		int seleniumPort = (ChromeDriverDockerContainer.SELENIUM_BEGIN_PORT + port);
		int vncPort = (ChromeDriverDockerContainer.VNC_BEGIN_PORT + port);
		String containerName = "ChromeContainer-" + this.ip + "-" + port;

		container = new ChromeDriverDockerContainerImpl(this, containerName, seleniumPort, vncPort);
		container.create();

		this.addContainerNum();

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
