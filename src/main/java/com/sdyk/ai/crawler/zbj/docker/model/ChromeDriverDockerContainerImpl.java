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
import one.rewind.io.requester.chrome.ChromeDriverAgent;
import one.rewind.io.requester.chrome.ChromeDriverRequester;
import one.rewind.util.Configs;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

/**
 * 容器
 */
@DBName(value = "crawler")
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
			host.occupiedPorts.add(getPort());
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

