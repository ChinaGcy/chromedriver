package com.sdyk.ai.crawler.zbj.proxy;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.ecs.model.v20140526.*;
import com.aliyuncs.profile.DefaultProfile;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.sdyk.ai.crawler.zbj.proxy.model.ProxyImpl;
import com.typesafe.config.Config;
import one.rewind.db.DBName;
import one.rewind.db.DaoManager;
import one.rewind.io.SshManager;
import one.rewind.util.Configs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 *
 */
@DBName(value = "crawler")
@DatabaseTable(tableName = "aliyun_hosts")
public class AliyunHost {

	private static final Logger logger = LogManager.getLogger(AliyunHost.class.getName());

	public static String key;
	public static String secret;
	public static String regionId;

	public static DefaultProfile profile;
	public static IAcsClient client;
	public static String Proxy_Group_Name;

	public static enum Region {
		CN_SHENZHEN
	}

	// 初始化
	static {

		Config config = Configs.getConfig(AliyunHost.class);
		key = config.getString("key");
		secret = config.getString("secret");
		regionId = config.getString("regionId");

		Proxy_Group_Name = "aliyun-" + regionId + "-squid";

		profile = DefaultProfile.getProfile(regionId, key, secret);
		client = new DefaultAcsClient(profile);
	}

	public enum Status {
		NEW,
		STARTING,
		READY,
		STOPPING,
		STOPPED
	}

	public transient SshManager.Host ssh_host;

	@DatabaseField(dataType = DataType.STRING, width = 128, canBeNull = false, id = true)
	public String id;

	@DatabaseField(dataType = DataType.STRING, width = 128, canBeNull = false)
	public String host;

	@DatabaseField(dataType = DataType.INTEGER, width = 5, canBeNull = false)
	public int port;

	@DatabaseField(dataType = DataType.STRING, width = 128, canBeNull = false)
	private String user;

	@DatabaseField(dataType = DataType.STRING, width = 128, canBeNull = false)
	private String passwd;

	@DatabaseField(dataType = DataType.STRING, width = 64, canBeNull = false)
	public String region;

	@DatabaseField(dataType = DataType.ENUM_STRING, width = 16, canBeNull = false)
	public Status status = Status.NEW;

	public AliyunHost() {}

	/**
	 *
	 * @param service_id
	 * @param user
	 * @param passwd
	 * @param region
	 */
	public AliyunHost(String service_id, String user, String passwd, String region) {
		this.id = service_id;
		this.user = user;
		this.passwd = passwd;
		this.region = region;
	}

	/**
	 * 增加服务器
	 * @param region
	 * @return
	 */
	public static AliyunHost buildService(Region region) {

		String region_str = region.name().toLowerCase().replaceAll("_", "-");
		String user = "root";
		String passwd = "SdYK@315Fr##";

		//设置参数
		CreateInstanceRequest createInstance = new CreateInstanceRequest();
		// 地区
		// TODO 是否可以指定地区
		createInstance.setRegionId(region_str);
		// 操作系统
		createInstance.setImageId("ubuntu_16_0402_64_20G_alibase_20171227.vhd");

		// 服务器类型
		createInstance.setInstanceType("ecs.xn4.small");

		// 安全组 用于开放端口
		createInstance.setSecurityGroupId("sg-wz9ejq1i5n8kv5kp8sqo");

		// 主机名 密码
		createInstance.setHostName("aliyun-zbj");
		createInstance.setPassword("SdYK@315Fr##");

		// 按时间计费
		createInstance.setInternetChargeType("PayByTraffic");
		// 带宽
		createInstance.setInternetMaxBandwidthOut(100);

		// 发起请求
		try {

			CreateInstanceResponse response = client.getAcsResponse(createInstance);

			String serviceId = response.getInstanceId();

			AliyunHost aliyun_host = new AliyunHost(serviceId, user, passwd, region_str);

			logger.info("Wait 5s for remote sshHost creating...");
			Thread.sleep(10000);
			logger.info("Wait done.");

			aliyun_host.start();

			/*logger.info("Wait 120s for remote sshHost starting...");
			Thread.sleep(120000);
			logger.info("Wait done.");*/

			// TODO 添加个时间限制，如果时间过长提示
			while (!aliyun_host.getIpAndPort()) {}

			aliyun_host.buildSshHost(0);

			return aliyun_host;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 *
	 * @param count
	 * @throws InterruptedException
	 */
	public static void batchBuild(int count) throws InterruptedException {

		CountDownLatch doneSignal = new CountDownLatch(count);

		for(int i=0; i<count; i++) {

			new Thread(()->{

				try {

					AliyunHost aliyunHost = AliyunHost.buildService(Region.CN_SHENZHEN);

					if(aliyunHost != null) {
						aliyunHost.insert();

						ProxyImpl proxy = aliyunHost.createSquidProxy();
						proxy.insert();
					}

				} catch (Exception e) {

					e.printStackTrace();
				}

				doneSignal.countDown();

			}).start();
		}

		doneSignal.await();
	}

	/**
	 * 获取公网ip，运行中
	 * @return
	 */
	public boolean getIpAndPort() {

		AllocatePublicIpAddressRequest allocatePublicIpAddress = new AllocatePublicIpAddressRequest();
		allocatePublicIpAddress.setInstanceId(id);
		// 设置公网ip， 默认自动生成
		// allocatePublicIpAddress.setIpAddress("");

		// 发起请求
		try {
			AllocatePublicIpAddressResponse response = client.getAcsResponse(allocatePublicIpAddress);
			logger.info("Public ip: {}", response.getIpAddress());
			this.host = response.getIpAddress();
			this.port = 22;
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 *
	 * @throws IOException
	 */
	private void buildSshHost(int i) throws IOException {
		if (i > 4){
			throw new IOException();
		}
		try {
			ssh_host = new SshManager.Host(host, port, user, passwd);
			ssh_host.connect();
		} catch (IOException e) {

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			++i;
			this.buildSshHost(i);
		}

	}

	/**
	 * 启动服务器
	 */
	public boolean start() {

		// 设置参数
		StartInstanceRequest startInstance = new StartInstanceRequest();
		startInstance.setInstanceId(id);

		// 发起请求
		try {
			StartInstanceResponse response = client.getAcsResponse(startInstance);
			logger.info(response.getRequestId());
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 停止服务器
	 */
	public boolean stop() {

		// 设置参数
		StopInstanceRequest stopInstance = new StopInstanceRequest();
		stopInstance.setInstanceId(id);
		stopInstance.setConfirmStop(true);
		// stopInstance.setForceStop(true);

		// 发起请求
		try {
			StopInstanceResponse response = client.getAcsResponse(stopInstance);
			logger.info(response);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 删除主机，注意停止后删除
	 * @return
	 */
	private boolean deleteHost() {

		DeleteInstanceRequest deleteInstance = new DeleteInstanceRequest();
		deleteInstance.setInstanceId(id);

		// 发起请求
		try {
			DeleteInstanceResponse response = client.getAcsResponse(deleteInstance);
			logger.info(response);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 停止并删除一个主机
	 * @return
	 */
	public boolean stopAndDelete() {

		if (this.stop()) {
			try {
				Thread.sleep(50000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			this.deleteHost();
			return true;
		}
		else {
			return false;
		}
	}

	public static void stopAndDeleteAll() throws Exception {
		stopAndDelete(getAll());
	}

	/**
	 * 删除多个主机
	 * @return
	 */
	public static void stopAndDelete(List<AliyunHost> aliyunHosts) throws InterruptedException {

		// TODO 结束不了
		CountDownLatch doneSignal = new CountDownLatch(aliyunHosts.size());

		for (AliyunHost a : aliyunHosts) {

			new Thread(() -> {
				// 判断停止是否出错
				if (a.stop()) {

					// TODO 添加个时间限制，如果时间过长提示
					while (!a.deleteHost()) {}
					a.status = Status.STOPPING;
					try {
						a.update();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				doneSignal.countDown();

			}).start();
		}
		doneSignal.await();
		// TODO 轮询回调接口确认状态
	}

	/**
	 * 插入
	 * @return
	 * @throws Exception
	 */
	public boolean insert() throws Exception {

		Dao dao = DaoManager.getDao(this.getClass());

		try {
			dao.create(this);
			return true;
		} catch (SQLException e) {
			try {
				dao.update(this);
				return true;
			} catch (SQLException ex) {
				logger.error("insert update error {}", ex);
				return false;
			}
		}
	}

	/**
	 * 查询
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public static AliyunHost getById(String id) throws Exception{

		Dao<AliyunHost, String> dao = DaoManager.getDao(AliyunHost.class);
		return dao.queryForId(id);
	}

	/**
	 *
	 * @return
	 * @throws Exception
	 */
	public static List<AliyunHost> getAll() throws Exception {

		Dao<AliyunHost, String> dao = DaoManager.getDao(AliyunHost.class);
		return dao.queryForAll();
	}

	/**
	 *
	 * @param host
	 * @return
	 * @throws Exception
	 */
	public static AliyunHost getByHost(String host) throws Exception {
		Dao<AliyunHost, String> dao = DaoManager.getDao(AliyunHost.class);
		AliyunHost aliyunHost = dao.queryBuilder().where().eq("sshHost", host).queryForFirst();
		return aliyunHost;
	}

	private boolean update() throws Exception {
		Dao<AliyunHost, String> dao = DaoManager.getDao(AliyunHost.class);
		return dao.update(this) == 1;
	}

	/**
	 * 在数据库中删除一个主机
	 * @return
	 * @throws Exception
	 */
	private boolean delete() throws Exception {
		Dao<AliyunHost, String> dao = DaoManager.getDao(AliyunHost.class);
		int i = dao.deleteById(id);
		return i == 1;
	}

	/**
	 * 添加代理
	 * @return
	 */
	public ProxyImpl createSquidProxy() {

		String proxyUser = "tfelab";
		String proxyPassword = "TfeLAB2@15";
		int proxyPort = 59998;

		ProxyImpl proxy = null;

		try {
			//ssh_host.connect();

			ssh_host.upload("squid.sh", "/root");

			ssh_host.exec("chmod +x squid.sh");

			// 先更新，再执行
			String out = ssh_host.exec("apt update");
			//logger.info(out);

			out = ssh_host.exec("./squid.sh ");
			//logger.info(out);

			out = ssh_host.exec("service squid restart");
			//logger.info(out);
			// TODO 需要判定是否成功启动服务

			proxy = new ProxyImpl(Proxy_Group_Name, host, proxyPort, proxyUser, proxyPassword, region, 0);
			proxy.setAliyunHost();
			proxy.validate();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return proxy;
	}

}
