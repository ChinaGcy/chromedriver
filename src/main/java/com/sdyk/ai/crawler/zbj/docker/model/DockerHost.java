package com.sdyk.ai.crawler.zbj.docker.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.zbj.docker.DockerHostManager;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;
import one.rewind.io.SshManager;
import org.redisson.api.RLock;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static one.rewind.db.RedissonAdapter.logger;
import static one.rewind.db.RedissonAdapter.redisson;

@DBName(value = "crawler")
@DatabaseTable(tableName = "docker_hosts")
public class DockerHost {

	@DatabaseField(dataType = DataType.INTEGER, canBeNull = false, generatedId = true)
	public int id;

	@DatabaseField(dataType = DataType.STRING, width = 32, canBeNull = false)
	public String ip;

	@DatabaseField(dataType = DataType.INTEGER, width = 5, canBeNull = false)
	public int port;

	@DatabaseField(dataType = DataType.INTEGER, width = 5, canBeNull = false)
	private int container_num = 0;

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

	public DockerHost() {
	}

	public String exec(String cmd) {

		// 秘钥登录
		//SshManager.Host sshHost = new SshManager.Host(ip, port, "root", DockerHostManager.PEM_FILE);

		// 账号登录
		SshManager.Host sshHost = new SshManager.Host(ip, port, "root", "sdyk315pr");

		String output = null;

		try {

			// 同时连接不能超过10个，否则会抛出异常    http://www.ganymed.ethz.ch/ssh2/FAQ.html
			// java.io.IOException: Could not open channel (The server refused to open the channel (SSH_OPEN_ADMINISTRATIVELY_PROHIBITED, 'open failed'))
			sshHost.connect();
			output = sshHost.exec(cmd);

		} catch (Exception e) {

			logger.error(e);

		} finally {
			if(sshHost.conn != null) {
				sshHost.conn.close();
			}
		}

		return output;
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public boolean insert() throws Exception{

		Dao<DockerHost, String> dao = DaoManager.getDao(DockerHost.class);

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

		Dao<DockerHost, String> dao = DaoManager.getDao(DockerHost.class);

		if (dao.update(this) == 1) {
			return true;
		}

		return false;
	}

	/**
	 * 统计container数量
	 */
	public int addContainerNum() throws Exception {

		RLock lock = redisson.getLock("Docker-Host-Access-Lock-" + ip);

		lock.lock(10, TimeUnit.SECONDS);

		Dao<DockerHost, String> dao = DaoManager.getDao(DockerHost.class);
		// 刷新内存数据
		dao.refresh(this);

		this.container_num ++;
		this.update();

		lock.unlock();

		return this.container_num;

	}

	/*public int getContainerNum() throws Exception {

		lock.lock(10, TimeUnit.SECONDS);

		Dao<DockerHost, String> dao = DaoManager.getDao(DockerHost.class);
		dao.refresh(this);

		lock.unlock();

		return this.container_num;
	}*/
}
