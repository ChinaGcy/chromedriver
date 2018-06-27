package com.sdyk.ai.crawler.docker.model;

import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.docker.DockerHostManager;
import one.rewind.db.DBName;
import one.rewind.io.docker.model.ChromeDriverDockerContainer;
import one.rewind.io.docker.model.DockerHost;

/**
 * 容器
 */
@DBName(value = "sdyk_raw")
@DatabaseTable(tableName = "docker_containers")
public class ChromeDriverDockerContainerImpl extends ChromeDriverDockerContainer{

	public ChromeDriverDockerContainerImpl() throws Exception {}

	public ChromeDriverDockerContainerImpl(DockerHost dockerHost, String containerName, int seleniumPort, int vncPort) {
		super(dockerHost, containerName, seleniumPort, vncPort);
	}

	public int getPort() {
		return this.seleniumPort - SELENIUM_BEGIN_PORT;
	}

	/**
	 *
	 * @throws Exception
	 */
	public void rm() throws Exception {

		String cmd = "docker rm -f " + name + "\n";

		if (host != null) {

			String output = host.exec(cmd);
			// TODO 根据output 判断是否执行成功

			DockerHost.logger.info(output);

			host.minusContainerNum();

			status = Status.TERMINATED;

			DockerHostImpl host = DockerHostManager.getInstance().getHostByIp(this.ip);
			if(host.occupiedPorts != null) {
				host.occupiedPorts.remove(getPort());
			}

			host.update();

		} else {
			throw new Exception("DockerHost is null");
		}
	}

	public String exec(String cmd) {

		String output = "";

		cmd = "docker exec " + name + " /bin/sh -c \"" + cmd + "\"";

		if(host != null) {
			output = host.exec(cmd);
		}
		return output;
	}
}

