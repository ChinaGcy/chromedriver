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

	public ChromeDriverDockerContainerImpl(DockerHost dockerHost, String containerName, int seleniumPort, int vncPort) {
		super(dockerHost, containerName, seleniumPort, vncPort);
	}
}

