package com.sdyk.ai.crawler.docker;

import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.docker.model.ChromeDriverDockerContainerImpl;
import com.sdyk.ai.crawler.docker.model.DockerHostImpl;
import one.rewind.db.DaoManager;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.docker.model.DockerContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.api.RLock;

import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static one.rewind.db.RedissonAdapter.redisson;


public class DockerHostManager {

	public static final Logger logger = LogManager.getLogger(DockerHostManager.class.getName());

	protected static DockerHostManager instance;

	public static DockerHostManager getInstance() {

		if (instance == null) {
			synchronized (DockerHostManager.class) {
				if (instance == null) {
					instance = new DockerHostManager();
				}
			}
		}

		return instance;
	}

	private transient RLock lock = redisson.getLock("Docker-Manager");

	/**
	 *
	 * @throws Exception
	 */
	public DockerHostManager() {

	}

	/**
	 * 通过ip查找dockerhost
	 * @param ip
	 * @return
	 * @throws Exception
	 */
	public DockerHostImpl getHostByIp(String ip) throws Exception {

		Dao<DockerHostImpl, String> dao = DaoManager.getDao(DockerHostImpl.class);
		DockerHostImpl host = dao.queryBuilder().where().eq("ip", ip).queryForFirst();
		return host;
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public ChromeDriverDockerContainerImpl getFreeContainer() throws Exception {

		lock.lock(10, TimeUnit.SECONDS);
		Dao<ChromeDriverDockerContainerImpl, String> dao = DaoManager.getDao(ChromeDriverDockerContainerImpl.class);
		ChromeDriverDockerContainerImpl container = dao.queryBuilder()
				.where().eq("status", ChromeDriverDockerContainerImpl.Status.IDLE)
				.queryForFirst();

		if(container != null) {
			// TODO 自定义 ChromeDriverDockerContainerImpl 的反序列化方法 初始化 DockerHostImpl
			container.host = getHostByIp(container.ip);
			container.setOccupied();
		}

		lock.unlock();
		return container;
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	private DockerHostImpl getMinLoadHost() throws Exception {

		Dao<DockerHostImpl, String> dao = DaoManager.getDao(DockerHostImpl.class);
		DockerHostImpl host = dao.queryBuilder()
				.orderBy("container_num", true)
				.where().eq("status", DockerHostImpl.Status.RUNNING)
				.queryForFirst();

		return host;
	}

	/**
	 * 在指定的docker中，批量创建容器
	 * @param num
	 * @throws Exception
	 */
	public void createDockerContainers(int num) throws Exception {

		CountDownLatch done = new CountDownLatch(num);

		for(int i=0; i<num; i++) {

			new Thread(()->{

				DockerHostImpl host = null;

				try {
					host = getMinLoadHost();
				} catch (Exception e) {
					e.printStackTrace();
				}

				if(host != null) {

					try {
						ChromeDriverDockerContainer container = host.createChromeDriverDockerContainer();
						container.insert();
						done.countDown();

					} catch (Exception e) {
						logger.error("Error create docker container, ", e);
					}
				}
				else {
					logger.error("No available host.");
				}

			}).start();
		}

		done.await();
	}

	/**
	 * TODO 增加DockerContainer delete方法
	 * 删除所有容器
	 * @param dockerHost
	 * @throws Exception
	 */
	public void delAllDockerContainers(DockerHostImpl dockerHost) {

		String cmd = "docker stop $(docker ps -a -q) && docker rm $(docker ps -a -q)\n";

		String output = dockerHost.exec(cmd);

		logger.info(output);
	}

	/**
	 *
	 * @throws Exception
	 */
	public void delAllDockerContainers() throws Exception {

		Dao<DockerHostImpl, String> dao = DaoManager.getDao(DockerHostImpl.class);

		dao.queryForAll().stream().forEach(host -> {

			delAllDockerContainers(host);

			try {
				host.container_num = 0;
				host.occupiedPorts = new HashSet<>();
				host.update();
			} catch (Exception e) {
				e.printStackTrace();
			}
		});

		DaoManager.getDao(ChromeDriverDockerContainerImpl.class).queryForAll().stream().forEach(c -> {
			try {
				c.status = DockerContainer.Status.TERMINATED;
				c.update();
				c.delete();

			} catch (Exception e) {
				logger.error(e);
			}
		});
	}
}
