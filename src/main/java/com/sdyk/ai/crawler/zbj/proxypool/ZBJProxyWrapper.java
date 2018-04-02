package com.sdyk.ai.crawler.zbj.proxypool;

import com.sdyk.ai.crawler.zbj.model.Proxy;
import org.tfelab.io.SshManager;

public class ZBJProxyWrapper {

	String serviceId;

	public static int tag = 0;

	public ZBJProxyWrapper() {
		// 创建一个阿里云服务器
		this.serviceId = AliyunService.getService();

	}

	public String [] getProxy(ZBJProxyWrapper proxyWapper) throws Exception {

		Thread.sleep(5*1000);
		// 开启服务器
		AliyunService.startService(proxyWapper.serviceId);
		try {
			Thread.sleep(70*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		// 分配IP
		String aliyunIP = AliyunService.getIP(proxyWapper.serviceId);

		Thread.sleep(10000);

		// 操作服务器
		proxyWapper.setService(aliyunIP, 22, "root", "SDYK315pr");

		// 入库
		Proxy proxy =  new Proxy("aliyun", aliyunIP, 59998, "tfelab", "TfeLAB2@15", "cn-shenzhen",1);

		proxy.insert();

		String [] ipAndId = {aliyunIP,serviceId};

		return ipAndId;
	}

	/**
	 * 操作服务器
	 * @param IP
	 * @param port
	 * @param user
	 * @param password
	 * @throws Exception
	 */
	public void setService(String IP, int port ,String user, String password) throws Exception {

		SshManager.Host host = new SshManager.Host(IP, port, user, password);

		try {
			host.connect();
		} catch (Exception e) {
			host.connect();
		}

		host.upload("squid.sh", "/root");

		host.exec("chmod +x squid.sh");

		// 先更新，再执行
		String out = host.exec("apt update");
		System.out.println(out);

		out = host.exec("./squid.sh ");
		System.out.println(out);

		out = host.exec("service squid restart");
		System.out.println(out);
	}

	public static void main(String[] args) throws InterruptedException {

		while(true) {
			if (tag == 1) {
				ZBJProxyWrapper proxyWapper = new ZBJProxyWrapper();
				Thread.sleep(5000);
				try {
					String[] a = proxyWapper.getProxy(proxyWapper);

					ProxyReplace.map.put(a[0], a[1]);
				} catch (Exception e) {
					e.printStackTrace();
				}
				tag = 0;
			}
		}
	}
}
