package com.sdyk.ai.crawler.zbj.docker;

import com.j256.ormlite.dao.Dao;
import com.sdyk.ai.crawler.zbj.model.DockerHost;
import one.rewind.db.DaoManager;
import one.rewind.io.SshManager;
import one.rewind.io.requester.Task;
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.io.requester.exception.ChromeDriverException;
import one.rewind.io.requester.proxy.Proxy;
import one.rewind.io.requester.proxy.ProxyImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;


public class DockerHostManager {

	public static final Logger logger = LogManager.getLogger(DockerHostManager.class.getName());

	protected static DockerHostManager instance;

	// 秘钥
	public static File PEM_FILE;

	public static DockerHostManager getInstance() throws Exception {

		if (instance == null) {
			synchronized (DockerHostManager.class) {
				if (instance == null) {
					instance = new DockerHostManager();
				}
			}
		}

		return instance;
	}

	static {
		PEM_FILE = new File("secret.pem");
	}

	private BlockingQueue<DockerContainer> containers = new LinkedBlockingDeque<>();

	/**
	 *
	 * @throws Exception
	 */
	public DockerHostManager() throws Exception {

		/*Dao<DockerHost, String> dao = DaoManager.getDao(DockerHost.class);
		List<DockerHost> hosts = dao.queryForEq("status", "RUNNING");
		for(DockerHost host : hosts) {
			host.initSshConn();
		}*/
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
	 * docker容器类
	 */
	public static class DockerContainer {

		public DockerHost dockerHost;
		public String name;
		int seleniumPort;
		public int vncPort;
		Status status = Status.STARTING;

		public void setIdle() {
			this.status = Status.IDLE;
		}

		public enum Status {
			STARTING, // 启动中
			IDLE, // 空闲
			OCCUPIED, // 占用
			FAILED, // 出错
			TERMINATED // 已删除
		}

		DockerContainer(DockerHost host, String name, int seleniumPort, int vncPort) {
			this.dockerHost = host;
			this.name = name;
			this.seleniumPort = seleniumPort;
			this.vncPort = vncPort;
		}

		/**
		 * 获取路由的地址
		 * @throws MalformedURLException
		 */
		public URL getRemoteAddress() throws MalformedURLException {
			return new URL("http://" +dockerHost.ip+  ":" + seleniumPort + "/wd/hub");
		}

		/**
		 * 删除容器
		 */
		public void delete() {

			String cmd = "docker rm -f " + name +  "\n";

			String output = null;
			try {

				output = dockerHost.exec(cmd);
				logger.info(output);
				status = Status.TERMINATED;

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 在指定的docker中，批量创建容器
	 * @param ip
	 * @param num
	 * @throws Exception
	 */
	public void createDockerContainers(String ip, int num) throws Exception {

		DockerHost host = getHostByIp(ip);
		if(host != null) {
			containers.addAll(createDockerContainers(host, num));
		}
	}

	/**
	 * 获取容器
	 * @return
	 * @throws InterruptedException
	 */
	public DockerContainer getContainer() throws InterruptedException {
		return containers.take();
	}

	/**
	 * 批量创建容器
	 * @param dockerHost
	 * @param num
	 * @throws Exception
	 */
	public List<DockerContainer> createDockerContainers(DockerHost dockerHost, int num) throws Exception {

		List<DockerContainer> containers = new ArrayList<>();

		CountDownLatch done = new CountDownLatch(num);

		for(int i_=0; i_<num; i_++){

			final int i = i_;

			new Thread(() -> {

				int seleniumPort = (31000 + i);
				int vncPort = (32000 + i);
				String containerName = "ChromeContainer-"+i;

				String cmd = "docker run -d --name " + containerName + " -p "+seleniumPort+":4444 -p "+vncPort+":5900 -e SCREEN_WIDTH=\"1360\" -e SCREEN_HEIGHT=\"768\" -e SCREEN_DEPTH=\"24\" selenium/standalone-chrome-debug";

				String output = null;

				try {

					// TODO 同时连接不能超过10个，否则会抛出异常    http://www.ganymed.ethz.ch/ssh2/FAQ.html
					// java.io.IOException: Could not open channel (The server refused to open the channel (SSH_OPEN_ADMINISTRATIVELY_PROHIBITED, 'open failed'))
					output = dockerHost.exec(cmd);
					logger.info(output);

					DockerContainer container = new DockerContainer(dockerHost, containerName, seleniumPort, vncPort);
					// 设置状态
					container.setIdle();
					containers.add(container);

				} catch (Exception e) {
					e.printStackTrace();
				}

				done.countDown();

			}).start();
		}

		done.await();
		return containers;
	}

	/**
	 * TODO 增加DockerContainer delete方法
	 * 删除所有容器
	 * @param dockerHost
	 * @throws Exception
	 */
	public void delAllDockerContainers(DockerHost dockerHost) throws Exception {

		String cmd = "docker stop $(docker ps -a -q) && docker rm $(docker ps -a -q)\n";

		String output = dockerHost.exec(cmd);

		logger.info(output);
	}
}
