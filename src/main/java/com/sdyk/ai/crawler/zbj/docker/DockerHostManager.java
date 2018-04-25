package com.sdyk.ai.crawler.zbj.docker;

import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.zbj.docker.model.DockerContainer;
import com.sdyk.ai.crawler.zbj.docker.model.DockerHost;
import com.typesafe.config.Config;
import one.rewind.db.DaoManager;
import one.rewind.util.Configs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.redisson.api.RLock;

import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static one.rewind.db.RedissonAdapter.redisson;


public class DockerHostManager {

	public static final Logger logger = LogManager.getLogger(DockerHostManager.class.getName());

	public static File PEM_FILE;
	public static int MAX_CONTAINER_NUM = 40;
	private static int SELENIUM_BEGIN_PORT = 31000;
	private static int VNC_BEGIN_PORT = 32000;

	static {
		Config config = Configs.getConfig(DockerHostManager.class);
		PEM_FILE = new File(config.getString("privateKey"));
		MAX_CONTAINER_NUM = config.getInt("maxContainerNum");
		SELENIUM_BEGIN_PORT = config.getInt("seleniumBeginPort");
		SELENIUM_BEGIN_PORT = config.getInt("vncBeginPort");
	}

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
	public DockerHost getHostByIp(String ip) throws Exception {

		Dao<DockerHost, String> dao = DaoManager.getDao(DockerHost.class);
		DockerHost host = dao.queryBuilder().where().eq("ip", ip).queryForFirst();
		return host;
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public DockerContainer getFreeContainer() throws Exception {

		lock.lock(10, TimeUnit.SECONDS);
		Dao<DockerContainer, String> dao = DaoManager.getDao(DockerContainer.class);
		DockerContainer container = dao.queryBuilder()
				.where().eq("status", DockerContainer.Status.IDLE.name())
				.queryForFirst();

		if(container != null) {
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
	private DockerHost getMinLoadHost() throws Exception {

		Dao<DockerHost, String> dao = DaoManager.getDao(DockerHost.class);
		DockerHost host = dao.queryBuilder()
				.orderBy("container_num", true)
				.where().eq("status", DockerHost.Status.RUNNING.name())
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

				DockerHost host = null;

				try {
					host = getMinLoadHost();
				} catch (Exception e) {
					e.printStackTrace();
				}

				if(host != null) {

					DockerContainer container = null;

					try {
						createDockerContainer(host);
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
	 *
	 * @param dockerHost
	 * @return
	 * @throws Exception
	 */
	private void createDockerContainer(DockerHost dockerHost) throws Exception {

		DockerContainer container = null;

		int currentContainerNum = dockerHost.addContinerNum();

		int seleniumPort = (SELENIUM_BEGIN_PORT + currentContainerNum);
		int vncPort = (VNC_BEGIN_PORT + currentContainerNum);
		String containerName = "ChromeContainer-" + dockerHost.ip + "-" + currentContainerNum;

		String cmd = "docker run -d --name " + containerName + " -p "+seleniumPort+":4444 -p "+vncPort+":5900 -e SCREEN_WIDTH=\"1360\" -e SCREEN_HEIGHT=\"768\" -e SCREEN_DEPTH=\"24\" selenium/standalone-chrome-debug";

		String output = null;

		output = dockerHost.exec(cmd);

		// TODO 检验output 确定container创建成功
		logger.info(output);
		container = new DockerContainer(dockerHost.ip, containerName, seleniumPort, vncPort);

		container.insert();
		// 设置状态
		container.setIdle();
	}

	/**
	 * TODO 增加DockerContainer delete方法
	 * 删除所有容器
	 * @param dockerHost
	 * @throws Exception
	 */
	public void delAllDockerContainers(DockerHost dockerHost) {

		String cmd = "docker stop $(docker ps -a -q) && docker rm $(docker ps -a -q)\n";

		String output = dockerHost.exec(cmd);

		logger.info(output);
	}

	/**
	 *
	 * @throws Exception
	 */
	public void delAllDockerContainers() throws Exception {

		Dao<DockerHost, String> dao = DaoManager.getDao(DockerHost.class);

		dao.queryForAll().stream().forEach(host -> {
			delAllDockerContainers(host);
		});
	}
}
