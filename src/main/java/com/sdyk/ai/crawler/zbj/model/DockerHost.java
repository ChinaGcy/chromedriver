package com.sdyk.ai.crawler.zbj.model;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.zbj.docker.DockerHostManager;
import one.rewind.db.DBName;
import one.rewind.io.SshManager;

import java.io.IOException;
import java.util.Date;

@DBName(value = "crawler")
@DatabaseTable(tableName = "docker_hosts")
public class DockerHost {

	@DatabaseField(dataType = DataType.INTEGER, canBeNull = false, generatedId = true)
	public int id;

	@DatabaseField(dataType = DataType.STRING, width = 32, canBeNull = false)
	public String ip;

	@DatabaseField(dataType = DataType.INTEGER, width = 5, canBeNull = false)
	public int port;

	public static enum Status {
		RUNNING,
		STOPPED
	}

	@DatabaseField(dataType = DataType.ENUM_STRING, width = 16, canBeNull = false)
	public Status status = Status.RUNNING;

	// 插入时间
	@DatabaseField(dataType = DataType.DATE)
	public Date insert_time = new Date();

	// 更新时间
	@DatabaseField(dataType = DataType.DATE, index = true)
	public Date update_time = new Date();


	public String exec(String cmd) throws Exception {

		// 秘钥登录
		SshManager.Host sshHost = new SshManager.Host(ip, port, "root", DockerHostManager.PEM_FILE);

		// 账号登录
		//SshManager.Host sshHost = new SshManager.Host(ip, port, "root", "sdyk315pr");
		sshHost.connect();
		String output = sshHost.exec(cmd);
		sshHost.conn.close();

		return output;
	}
}
